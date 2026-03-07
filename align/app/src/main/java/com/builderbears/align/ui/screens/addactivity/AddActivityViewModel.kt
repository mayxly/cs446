package com.builderbears.align.ui.screens.addactivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.service.ActivityService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddActivityViewModel : ViewModel() {
    private val activityService = ActivityService()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isSaving by mutableStateOf(false)
    var saveError by mutableStateOf<String?>(null)
    var saveSuccess by mutableStateOf(false)

    fun saveActivity(
        name: String,
        description: String,
        workoutType: String,
        location: String,
        date: String,
        time: String
    ) {
        if (currentUserId.isEmpty()) {
            saveError = "Not logged in"
            return
        }

        viewModelScope.launch {
            isSaving = true
            saveError = null

            val activity = Activity(
                name = name,
                description = description,
                workoutType = workoutType,
                location = location,
                date = date,
                time = time
            )

            activityService.createActivity(currentUserId, activity)
                .onSuccess { saveSuccess = true }
                .onFailure { saveError = it.message }

            isSaving = false
        }
    }
}
