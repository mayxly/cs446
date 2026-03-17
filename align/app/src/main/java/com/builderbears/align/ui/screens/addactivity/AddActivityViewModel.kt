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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AddActivityViewModel : ViewModel() {
    private val activityService = ActivityService()

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var isSaving by mutableStateOf(false)
    var saveError by mutableStateOf<String?>(null)
    var saveSuccess by mutableStateOf(false)

    // Validation error states
    var nameError by mutableStateOf<String?>(null)
    var dateError by mutableStateOf<String?>(null)
    var timeError by mutableStateOf<String?>(null)
    var locationError by mutableStateOf<String?>(null)

    fun validateFields(
        name: String,
        selectedDate: LocalDate?,
        selectedHour: Int,
        selectedMinute: Int,
        isPm: Boolean,
        location: String
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

        if (location.isBlank()) {
            locationError = "Location is required"
            isValid = false
        }

        return isValid
    }

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
