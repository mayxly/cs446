package com.builderbears.align.ui.screens.feed

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.service.ActivityService
import com.builderbears.align.data.service.UserService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "FeedViewModel"

class FeedViewModel : ViewModel() {
    private val activityService = ActivityService()
    private val userService = UserService()
    private val auth = FirebaseAuth.getInstance()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _uploadingActivityIds = MutableStateFlow<Set<String>>(emptySet())
    val uploadingActivityIds: StateFlow<Set<String>> = _uploadingActivityIds

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val visibleUserIds = userService.getAllUsers()
                    .getOrDefault(emptyList())
                    .map { it.userId }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .let { ids -> if (ids.contains(userId)) ids else ids + userId }

                if (visibleUserIds.isEmpty()) {
                    _activities.value = emptyList()
                } else {
                    visibleUserIds.forEach { visibleId ->
                        activityService.syncPostedStatusForUser(visibleId)
                            .onFailure { exception ->
                                Log.e(TAG, "Error syncing posted status for $visibleId: ${exception.message}", exception)
                            }
                    }

                    val byActivityId = linkedMapOf<String, Activity>()
                    visibleUserIds.forEach { visibleId ->
                        activityService.getActivities(visibleId)
                            .onFailure { exception ->
                                Log.e(TAG, "Error loading activities for $visibleId: ${exception.message}", exception)
                            }
                            .onSuccess { activities ->
                                activities
                                    .asSequence()
                                    .filter { it.isPosted }
                                    .forEach { activity ->
                                        byActivityId[activity.activityId] = activity
                                    }
                            }
                    }

                    val postedActivities = byActivityId.values
                        .sortedByDescending { it.scheduledDateTimeOrNull() ?: LocalDateTime.MIN }

                    _activities.value = postedActivities
                    Log.d(TAG, "Posted activities loaded successfully. Count: ${postedActivities.size}")
                }
            } else {
                Log.w(TAG, "User ID is null, cannot load activities")
            }

            _isLoading.value = false
        }
    }

    fun refreshActivities() {
        loadActivities()
    }

    fun uploadActivityPhoto(activityId: String, imageUri: Uri) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val activity = _activities.value.find { it.activityId == activityId } ?: return@launch
            if (!activity.participantIds.contains(userId)) return@launch

            _uploadingActivityIds.value = _uploadingActivityIds.value + activityId
            activityService.addActivityPhoto(activityId, activity.participantIds, imageUri)
                .onSuccess { url ->
                    val list = _activities.value.toMutableList()
                    val idx = list.indexOfFirst { it.activityId == activityId }
                    if (idx != -1) {
                        list[idx] = list[idx].copy(imageUrls = list[idx].imageUrls + url)
                        _activities.value = list
                    }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to upload photo for activity $activityId", exception)
                }
            _uploadingActivityIds.value = _uploadingActivityIds.value - activityId
        }
    }

    fun deleteActivityPhoto(activityId: String, photoUrl: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val activity = _activities.value.find { it.activityId == activityId } ?: return@launch
            if (!activity.participantIds.contains(userId)) return@launch

            activityService.deleteActivityPhoto(activityId, activity.participantIds, photoUrl)
                .onSuccess {
                    val updated = _activities.value.map { existing ->
                        if (existing.activityId == activityId) {
                            existing.copy(imageUrls = existing.imageUrls.filterNot { it == photoUrl })
                        } else {
                            existing
                        }
                    }
                    _activities.value = updated
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to delete photo for activity $activityId", exception)
                }
        }
    }

    fun leaveActivity(activityId: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            activityService.leaveActivity(activityId, userId)
                .onSuccess {
                    _activities.value = _activities.value.filterNot { it.activityId == activityId }
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to leave activity $activityId", exception)
                }
        }
    }

    fun submitParticipantNote(activityId: String, note: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val trimmedNote = note.trim()
            if (trimmedNote.isEmpty()) return@launch

            val activity = _activities.value.find { it.activityId == activityId } ?: return@launch
            if (!activity.participantIds.contains(userId)) return@launch
            if (!activity.participantNotes[userId].isNullOrBlank()) return@launch

            activityService.addParticipantNote(activityId, activity.participantIds, userId, trimmedNote)
                .onSuccess {
                    val updated = _activities.value.map { existing ->
                        if (existing.activityId == activityId) {
                            existing.copy(participantNotes = existing.participantNotes + (userId to trimmedNote))
                        } else {
                            existing
                        }
                    }
                    _activities.value = updated
                }
                .onFailure { exception ->
                    Log.e(TAG, "Failed to submit note for activity $activityId", exception)
                }
        }
    }

    fun addReaction(activityId: String, emoji: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val currentActivities = _activities.value.toMutableList()
            val index = currentActivities.indexOfFirst { it.activityId == activityId }
            if (index != -1) {
                val activity = currentActivities[index]
                val reactions = activity.reactions.toMutableMap()
                val userList = reactions.getOrDefault(emoji, emptyList()).toMutableList()
                if (!userList.contains(userId)) {
                    userList.add(userId)
                    reactions[emoji] = userList
                    val updatedActivity = activity.copy(reactions = reactions)
                    currentActivities[index] = updatedActivity
                    _activities.value = currentActivities.sortedByDescending {
                        it.scheduledDateTimeOrNull() ?: LocalDateTime.MIN
                    }
                    activityService.updateActivity(activityId, activity.participantIds, mapOf("reactions" to reactions))
                }
            }
        }
    }

    fun removeReaction(activityId: String, emoji: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val currentActivities = _activities.value.toMutableList()
            val index = currentActivities.indexOfFirst { it.activityId == activityId }
            if (index != -1) {
                val activity = currentActivities[index]
                val reactions = activity.reactions.toMutableMap()
                val userList = reactions.getOrDefault(emoji, emptyList()).toMutableList()
                userList.remove(userId)
                if (userList.isEmpty()) {
                    reactions.remove(emoji)
                } else {
                    reactions[emoji] = userList
                }
                val updatedActivity = activity.copy(reactions = reactions)
                currentActivities[index] = updatedActivity
                _activities.value = currentActivities.sortedByDescending {
                    it.scheduledDateTimeOrNull() ?: LocalDateTime.MIN
                }
                activityService.updateActivity(activityId, activity.participantIds, mapOf("reactions" to reactions))
            }
        }
    }

    private fun Activity.scheduledDateTimeOrNull(): LocalDateTime? {
        val parsedDate = runCatching { LocalDate.parse(date, dateFormatter) }.getOrNull() ?: return null
        val parsedTime = runCatching { LocalTime.parse(time.trim().uppercase(Locale.US), timeFormatter) }.getOrNull()
            ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
    }

}