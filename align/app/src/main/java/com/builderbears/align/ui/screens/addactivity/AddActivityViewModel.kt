package com.builderbears.align.ui.screens.addactivity

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.model.ActivityParticipant
import com.builderbears.align.data.model.User
import com.builderbears.align.data.service.ActivityService
import com.builderbears.align.data.service.UserService
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class LocationSuggestion(
    val placeId: String,
    val primaryText: String,
    val fullText: String
)

data class ResolvedLocation(
    val location: String,
    val displayName: String,
    val displayAddress: String,
    val placeId: String,
    val lat: Double?,
    val lng: Double?
)

class AddActivityViewModel : ViewModel() {
    private val activityService = ActivityService()
    private val userService = UserService()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isSaving by mutableStateOf(false)
    var saveError by mutableStateOf<String?>(null)
    var saveSuccess by mutableStateOf(false)
    var editingActivity by mutableStateOf<Activity?>(null)
        private set
    var isLoadingEditingActivity by mutableStateOf(false)
        private set
    var isLoadingUsers by mutableStateOf(false)
        private set
    var availableUsers by mutableStateOf<List<User>>(emptyList())
        private set
    var usersLoadError by mutableStateOf<String?>(null)
        private set

    var locationSuggestions by mutableStateOf<List<LocationSuggestion>>(emptyList())
        private set
    var isLoadingLocationSuggestions by mutableStateOf(false)
        private set
    var isResolvingLocation by mutableStateOf(false)
        private set
    var locationConfigError by mutableStateOf<String?>(null)
        private set

    // Validation error states
    var nameError by mutableStateOf<String?>(null)
    var dateError by mutableStateOf<String?>(null)
    var timeError by mutableStateOf<String?>(null)
    var locationError by mutableStateOf<String?>(null)

    private var placesClient: PlacesClient? = null
    private var locationSessionToken: AutocompleteSessionToken? = null
    private var searchJob: Job? = null

    private val placeFields = listOf(
        Place.Field.ID,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.LAT_LNG
    )

    init {
        loadUsers()
    }

    fun onLocationInputChanged(
        context: Context,
        query: String,
        selectedLocationLabel: String,
        selectedLocationPlaceId: String
    ) {
        val normalizedQuery = query.trim()
        searchJob?.cancel()

        if (selectedLocationPlaceId.isNotBlank() && normalizedQuery == selectedLocationLabel) {
            locationSuggestions = emptyList()
            isLoadingLocationSuggestions = false
            return
        }

        if (normalizedQuery.length < 3) {
            locationSuggestions = emptyList()
            isLoadingLocationSuggestions = false
            locationSessionToken = null
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            val client = ensurePlacesClient(context)
            if (client == null) {
                locationSuggestions = emptyList()
                isLoadingLocationSuggestions = false
                return@launch
            }

            val token = locationSessionToken ?: AutocompleteSessionToken.newInstance().also {
                locationSessionToken = it
            }

            isLoadingLocationSuggestions = true
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(normalizedQuery)
                .setCountries(listOf("CA"))
                .build()

            runCatching { client.findAutocompletePredictions(request).await() }
                .onSuccess { response ->
                    locationSuggestions = response.autocompletePredictions
                        .filter { it.placeId.isNotBlank() }
                        .take(6)
                        .map { prediction ->
                            LocationSuggestion(
                                placeId = prediction.placeId,
                                primaryText = prediction.getPrimaryText(null).toString(),
                                fullText = prediction.getFullText(null).toString()
                            )
                        }
                    locationConfigError = null
                }
                .onFailure {
                    locationSuggestions = emptyList()
                }

            isLoadingLocationSuggestions = false
        }
    }

    fun hideLocationSuggestions() {
        locationSuggestions = emptyList()
    }

    fun resetLocationSearchSession(clearSuggestions: Boolean = true) {
        searchJob?.cancel()
        locationSessionToken = null
        isLoadingLocationSuggestions = false
        if (clearSuggestions) {
            locationSuggestions = emptyList()
        }
    }

    suspend fun resolveLocationForSubmit(
        context: Context,
        selectedLocationPlaceId: String,
        selectedLocationDisplayName: String,
        selectedLocationFullText: String,
        fallbackLocation: String
    ): Result<ResolvedLocation> {
        val placeId = selectedLocationPlaceId.trim()
        if (placeId.isEmpty()) {
            return Result.failure(IllegalArgumentException("Missing place id"))
        }

        val client = ensurePlacesClient(context)
            ?: return Result.failure(IllegalStateException("Missing MAPS_API_KEY configuration"))

        return try {
            isResolvingLocation = true

            val request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(locationSessionToken)
                .build()

            val place = client.fetchPlace(request).await().place
            val resolvedLocation = place.address?.takeIf { it.isNotBlank() }
                ?: selectedLocationFullText.ifBlank { place.name ?: fallbackLocation }
            val resolvedDisplayName = place.name
                ?.takeIf { it.isNotBlank() }
                ?: selectedLocationDisplayName
            val resolvedDisplayAddress = parseDisplayAddress(
                fullText = place.address.orEmpty().ifBlank { selectedLocationFullText },
                primaryText = resolvedDisplayName
            )

            locationSessionToken = null
            locationConfigError = null

            Result.success(
                ResolvedLocation(
                    location = resolvedLocation,
                    displayName = resolvedDisplayName,
                    displayAddress = resolvedDisplayAddress,
                    placeId = place.id.orEmpty().ifBlank { placeId },
                    lat = place.latLng?.latitude,
                    lng = place.latLng?.longitude
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            isResolvingLocation = false
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoadingUsers = true
            usersLoadError = null
            userService.getAllUsers()
                .onSuccess { users ->
                    availableUsers = users
                        .filter { it.userId.isNotBlank() }
                        .sortedBy { it.name.lowercase() }
                }
                .onFailure {
                    availableUsers = emptyList()
                    usersLoadError = it.message ?: "Failed to load users"
                }
            isLoadingUsers = false
        }
    }

    fun validateFields(
        name: String,
        selectedDate: LocalDate?,
        selectedHour: Int,
        selectedMinute: Int,
        isPm: Boolean,
        location: String,
        locationPlaceId: String
    ): Boolean {
        nameError = null
        dateError = null
        timeError = null
        locationError = null

        var isValid = true

        if (name.isBlank()) {
            nameError = "Activity name is required"
            isValid = false
        }

        if (selectedDate == null) {
            dateError = "Please select a date"
            isValid = false
        }

        if (selectedDate != null) {
            val hour24 = if (isPm && selectedHour != 12) selectedHour + 12 else if (!isPm && selectedHour == 12) 0 else selectedHour
            val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hour24, selectedMinute))
            val now = LocalDateTime.now()

            if (selectedDateTime.isBefore(now)) {
                if (dateError == null) {
                    dateError = "Date and time cannot be in the past"
                    timeError = "Date and time cannot be in the past"
                }
                isValid = false
            }
        }

        // if (location.isBlank()) {
        //     locationError = "Location is required"
        //     isValid = false
        // } else if (locationPlaceId.isBlank()) {
        //     locationError = "Please select a location from suggestions"
        //     isValid = false
        // }

        return isValid
    }

    fun saveActivity(
        name: String,
        description: String,
        workoutType: String,
        location: String,
        locationDisplayName: String,
        locationDisplayAddress: String,
        locationPlaceId: String,
        locationLat: Double?,
        locationLng: Double?,
        date: String,
        time: String,
        invitedUserIds: List<String> = emptyList()
    ) {
        if (currentUserId.isEmpty()) {
            saveError = "Not logged in"
            return
        }

        viewModelScope.launch {
            isSaving = true
            saveError = null

            val selectedInvitees = availableUsers
                .filter { it.userId in invitedUserIds }
                .map { ActivityParticipant(userId = it.userId, name = it.name, profilePhotoUrl = it.profilePhotoUrl) }

            val currentUser = availableUsers.firstOrNull { it.userId == currentUserId }
                ?: userService.getUser(currentUserId).getOrNull()

            val currentParticipant = ActivityParticipant(
                userId = currentUserId,
                name = currentUser?.name ?: "You",
                profilePhotoUrl = currentUser?.profilePhotoUrl ?: ""
            )

            val participants = (selectedInvitees + currentParticipant)
                .distinctBy { it.userId }
            val participantIds = participants.map { it.userId }

            val activity = Activity(
                name = name,
                description = description,
                workoutType = workoutType,
                location = location,
                locationDisplayName = locationDisplayName,
                locationDisplayAddress = locationDisplayAddress,
                locationPlaceId = locationPlaceId,
                locationLat = locationLat,
                locationLng = locationLng,
                date = date,
                time = time,
                participantIds = participantIds,
                participants = participants,
                isPosted = false
            )

            activityService.createActivity(activity)
                .onSuccess { saveSuccess = true }
                .onFailure { saveError = it.message }

            isSaving = false
        }
    }

    fun clearEditingActivity() {
        editingActivity = null
        isLoadingEditingActivity = false
    }

    fun loadActivityForEdit(activityId: String) {
        if (activityId.isBlank()) {
            saveError = "Invalid activity"
            return
        }

        if (currentUserId.isEmpty()) {
            saveError = "Not logged in"
            return
        }

        if (editingActivity?.activityId == activityId) {
            return
        }

        viewModelScope.launch {
            isLoadingEditingActivity = true
            saveError = null

            activityService.getActivity(currentUserId, activityId)
                .onSuccess { activity ->
                    if (activity == null) {
                        saveError = "Activity not found"
                        editingActivity = null
                    } else {
                        editingActivity = activity
                    }
                }
                .onFailure { e ->
                    saveError = e.message ?: "Failed to load activity"
                    editingActivity = null
                }

            isLoadingEditingActivity = false
        }
    }

    fun updateActivity(
        activityId: String,
        name: String,
        description: String,
        workoutType: String,
        location: String,
        locationDisplayName: String,
        locationDisplayAddress: String,
        locationPlaceId: String,
        locationLat: Double?,
        locationLng: Double?,
        date: String,
        time: String,
        invitedUserIds: List<String> = emptyList()
    ) {
        if (currentUserId.isEmpty()) {
            saveError = "Not logged in"
            return
        }

        if (activityId.isBlank()) {
            saveError = "Invalid activity"
            return
        }

        viewModelScope.launch {
            isSaving = true
            saveError = null

            val existing = activityService.getActivity(currentUserId, activityId).getOrNull()
            if (existing == null) {
                saveError = "Activity not found"
                isSaving = false
                return@launch
            }

            val selectedInvitees = availableUsers
                .filter { it.userId in invitedUserIds }
                .map { ActivityParticipant(userId = it.userId, name = it.name, profilePhotoUrl = it.profilePhotoUrl) }

            val currentUser = availableUsers.firstOrNull { it.userId == currentUserId }
                ?: userService.getUser(currentUserId).getOrNull()

            val currentParticipant = ActivityParticipant(
                userId = currentUserId,
                name = currentUser?.name ?: "You",
                profilePhotoUrl = currentUser?.profilePhotoUrl ?: ""
            )

            val participants = (selectedInvitees + currentParticipant)
                .distinctBy { it.userId }
            val participantIds = participants.map { it.userId }

            val participantUpdateResult = activityService.updateParticipants(
                activityId = activityId,
                oldParticipantIds = existing.participantIds,
                newParticipants = participants
            )

            if (participantUpdateResult.isFailure) {
                saveError = participantUpdateResult.exceptionOrNull()?.message ?: "Failed to update participants"
                isSaving = false
                return@launch
            }

            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "description" to description,
                "workoutType" to workoutType,
                "location" to location,
                "locationDisplayName" to locationDisplayName,
                "locationDisplayAddress" to locationDisplayAddress,
                "locationPlaceId" to locationPlaceId,
                "date" to date,
                "time" to time
            )

            locationLat?.let { updates["locationLat"] = it }
            locationLng?.let { updates["locationLng"] = it }

            activityService.updateActivity(activityId, participantIds, updates)
                .onSuccess {
                    saveSuccess = true
                    editingActivity = existing.copy(
                        name = name,
                        description = description,
                        workoutType = workoutType,
                        location = location,
                        locationDisplayName = locationDisplayName,
                        locationDisplayAddress = locationDisplayAddress,
                        locationPlaceId = locationPlaceId,
                        locationLat = locationLat,
                        locationLng = locationLng,
                        date = date,
                        time = time,
                        participantIds = participantIds,
                        participants = participants
                    )
                }
                .onFailure {
                    saveError = it.message ?: "Failed to update activity"
                }

            isSaving = false
        }
    }

    private fun ensurePlacesClient(context: Context): PlacesClient? {
        if (placesClient != null) return placesClient

        val apiKey = readMapsApiKey(context)
        if (apiKey.isBlank()) {
            locationConfigError = "Missing MAPS_API_KEY configuration"
            return null
        }

        if (!Places.isInitialized()) {
            Places.initialize(context.applicationContext, apiKey)
        }

        placesClient = Places.createClient(context.applicationContext)
        locationConfigError = null
        return placesClient
    }

    private fun readMapsApiKey(context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty()
        } catch (_: Exception) {
            ""
        }
    }

    private fun parseDisplayAddress(fullText: String, primaryText: String): String {
        val normalizedFull = fullText.trim()
        if (normalizedFull.isEmpty()) return ""

        val normalizedPrimary = primaryText.trim()
        if (normalizedPrimary.isEmpty()) return normalizedFull

        return normalizedFull
            .removePrefix(normalizedPrimary)
            .removePrefix(",")
            .trim()
    }
}
