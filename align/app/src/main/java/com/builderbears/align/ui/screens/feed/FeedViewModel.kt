package com.builderbears.align.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.service.ActivityService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "FeedViewModel"

class FeedViewModel : ViewModel() {
    private val activityService = ActivityService()
    private val auth = FirebaseAuth.getInstance()

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val result = activityService.getActivities(userId)
                result.onSuccess { activities ->
                    val sortedActivities = activities.sortedByDescending { it.date }
                    _activities.value = sortedActivities
                    Log.d(TAG, "Activities loaded successfully. Count: ${sortedActivities.size}")
                    sortedActivities.forEach { activity ->
                        Log.d(TAG, "Activity: name=${activity.name}, type=${activity.workoutType}, " +
                            "location=${activity.location}, date=${activity.date}, time=${activity.time}, " +
                            "participants=${activity.participants.size}, reactions=${activity.reactions.size}")
                    }
                }
                result.onFailure { exception ->
                    _error.value = exception.message
                    Log.e(TAG, "Error loading activities: ${exception.message}", exception)
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
                    _activities.value = currentActivities.sortedByDescending { it.date }
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
                _activities.value = currentActivities.sortedByDescending { it.date }
                activityService.updateActivity(activityId, activity.participantIds, mapOf("reactions" to reactions))
            }
        }
    }
}