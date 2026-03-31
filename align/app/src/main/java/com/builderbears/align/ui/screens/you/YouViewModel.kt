package com.builderbears.align.ui.screens.you

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.User
import com.builderbears.align.data.service.ActivityService
import com.builderbears.align.data.service.FriendService
import com.builderbears.align.data.service.UserService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class YouViewModel : ViewModel() {
    private val userService = UserService()
    private val activityService = ActivityService()
    private val friendService = FriendService()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    private val _totalWorkouts = MutableStateFlow(0)
    val totalWorkouts: StateFlow<Int> = _totalWorkouts

    private val _thisMonthWorkouts = MutableStateFlow(0)
    val thisMonthWorkouts: StateFlow<Int> = _thisMonthWorkouts

    // Weekly activity minutes: index 0 = Monday, 6 = Sunday
    private val _weeklyMinutes = MutableStateFlow(List(7) { 0 })
    val weeklyMinutes: StateFlow<List<Int>> = _weeklyMinutes

    // Top activity type of all time (e.g. "gym", "run")
    private val _topActivity = MutableStateFlow<String?>(null)
    val topActivity: StateFlow<String?> = _topActivity

    private val _pushNotificationsEnabled = MutableStateFlow(false)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled

    // Friends
    private val _friendStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val friendStatuses: StateFlow<Map<String, String>> = _friendStatuses

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends: StateFlow<List<User>> = _friends

    private val _allOtherUsers = MutableStateFlow<List<User>>(emptyList())
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            userService.getUser(userId)
                .onSuccess { user ->
                    _user.value = user
                    _profilePhotoUrl.value = user?.profilePhotoUrl?.takeIf { it.isNotBlank() }
                    _pushNotificationsEnabled.value = user?.pushNotificationsEnabled ?: false
                    _friendStatuses.value = user?.friends ?: emptyMap()
                    loadFriends(user?.friends ?: emptyMap())
                }
            loadAllOtherUsers(userId)
            loadActivityCounts(userId)
        }
    }

    fun refreshFriends() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            userService.getUser(userId)
                .onSuccess { user ->
                    _friendStatuses.value = user?.friends ?: emptyMap()
                    loadFriends(user?.friends ?: emptyMap())
                }
        }
    }

    private suspend fun loadActivityCounts(userId: String) {
        activityService.getActivities(userId)
            .onSuccess { activities ->
                val today = LocalDate.now()
                val currentMonth = YearMonth.now()
                val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

                val completedActivities = activities.filter { activity ->
                    val parsedDate = runCatching { LocalDate.parse(activity.date, dateFormatter) }.getOrNull()
                    parsedDate != null && !parsedDate.isAfter(today)
                }

                _totalWorkouts.value = completedActivities.size
                _thisMonthWorkouts.value = completedActivities.count { activity ->
                    val parsedDate = runCatching { LocalDate.parse(activity.date, dateFormatter) }.getOrNull()
                    parsedDate != null && YearMonth.from(parsedDate) == currentMonth
                }

                // Weekly activity minutes (Mon–Sun of the current week)
                val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val weekEnd = weekStart.plusDays(6)
                val dailyMinutes = IntArray(7)
                for (activity in activities) {
                    val parsedDate = runCatching { LocalDate.parse(activity.date, dateFormatter) }.getOrNull()
                        ?: continue
                    if (parsedDate in weekStart..weekEnd && !parsedDate.isAfter(today)) {
                        val dayIndex = parsedDate.dayOfWeek.value - 1 // Monday=0 .. Sunday=6
                        dailyMinutes[dayIndex] += workoutMinutes(activity.workoutType)
                    }
                }
                _weeklyMinutes.value = dailyMinutes.toList()

                // Top activity type of all time
                _topActivity.value = completedActivities
                    .groupingBy { it.workoutType.lowercase() }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key
            }
    }

    companion object {
        fun workoutMinutes(workoutType: String): Int = when (workoutType.lowercase()) {
            "run" -> 30
            "gym" -> 120
            "yoga" -> 60
            "cycle" -> 90
            "swim" -> 60
            "basketball" -> 120
            "hiit" -> 60
            else -> 60
        }

        fun workoutEmoji(workoutType: String): String = when (workoutType.lowercase()) {
            "run" -> "\uD83C\uDFC3"
            "gym" -> "\uD83C\uDFCB\uFE0F"
            "yoga" -> "\uD83E\uDDD8"
            "cycle" -> "\uD83D\uDEB4"
            "swim" -> "\uD83C\uDFCA"
            "basketball" -> "\uD83C\uDFC0"
            "hiit" -> "\uD83D\uDD25"
            else -> "\u2728"
        }

        fun workoutLabel(workoutType: String): String = when (workoutType.lowercase()) {
            "run" -> "Run"
            "gym" -> "Gym"
            "yoga" -> "Yoga"
            "cycle" -> "Cycle"
            "swim" -> "Swim"
            "basketball" -> "Basketball"
            "hiit" -> "HIIT"
            else -> workoutType.replaceFirstChar { it.uppercase() }
        }
    }

    private suspend fun loadFriends(friendsMap: Map<String, String>) {
        val acceptedIds = friendsMap.filter { it.value == "ACCEPTED" }.keys.toList()
        if (acceptedIds.isEmpty()) {
            _friends.value = emptyList()
            return
        }
        userService.getUsersByIds(acceptedIds)
            .onSuccess { _friends.value = it }
    }

    private suspend fun loadAllOtherUsers(currentUserId: String) {
        userService.getAllUsers()
            .onSuccess { allUsers ->
                val others = allUsers.filter { it.userId != currentUserId }
                _allOtherUsers.value = others
                _searchResults.value = others
            }
    }

    fun searchUsers(query: String) {
        val all = _allOtherUsers.value
        _searchResults.value = if (query.isBlank()) {
            all
        } else {
            all.filter { user ->
                user.username.contains(query.lowercase()) ||
                    user.name.lowercase().contains(query.lowercase())
            }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = _user.value?.name ?: ""
        viewModelScope.launch {
            friendService.sendRequest(currentUserId, toUserId, currentUserName)
                .onSuccess {
                    _friendStatuses.value = _friendStatuses.value + (toUserId to "SENT")
                }
        }
    }

    fun acceptFriendRequest(fromUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            friendService.acceptRequest(currentUserId, fromUserId)
                .onSuccess {
                    _friendStatuses.value = _friendStatuses.value + (fromUserId to "ACCEPTED")
                    loadFriends(_friendStatuses.value)
                }
        }
    }

    fun cancelFriendRequest(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            friendService.cancelRequest(currentUserId, otherUserId)
                .onSuccess {
                    _friendStatuses.value = _friendStatuses.value - otherUserId
                }
        }
    }

    fun removeFriend(otherUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            friendService.removeFriend(currentUserId, otherUserId)
                .onSuccess {
                    _friendStatuses.value = _friendStatuses.value - otherUserId
                    _friends.value = _friends.value.filter { it.userId != otherUserId }
                }
        }
    }

    fun updateName(newName: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userService.updateUser(userId, mapOf("name" to newName))
                .onSuccess {
                    _user.value = _user.value?.copy(name = newName)
                }
        }
    }

    fun setPushNotificationsEnabled(enabled: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        _pushNotificationsEnabled.value = enabled
        viewModelScope.launch {
            userService.updateUser(userId, mapOf("pushNotificationsEnabled" to enabled))
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            userService.uploadProfilePhoto(userId, imageUri)
                .onSuccess { downloadUrl ->
                    _profilePhotoUrl.value = downloadUrl
                    _user.value = _user.value?.copy(profilePhotoUrl = downloadUrl)
                }
            _isUploadingPhoto.value = false
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        auth.signOut()
        _user.value = null
        _profilePhotoUrl.value = null
        onLoggedOut()
    }
}
