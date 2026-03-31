package com.builderbears.align.ui.screens.schedule

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.service.ActivityService
import com.builderbears.align.data.model.Attendee
import com.builderbears.align.data.model.MonthGroup
import com.builderbears.align.data.model.ScheduledDay
import com.builderbears.align.data.model.ScheduleUiState
import com.builderbears.align.data.model.WorkoutEvent
import com.builderbears.align.ui.theme.ScheduleChipAmber
import com.builderbears.align.ui.theme.ScheduleChipBlue
import com.builderbears.align.ui.theme.ScheduleChipGray
import com.builderbears.align.ui.theme.ScheduleChipGreen
import com.builderbears.align.ui.theme.ScheduleChipMint
import com.builderbears.align.ui.theme.ScheduleChipOrange
import com.builderbears.align.ui.theme.ScheduleChipPink
import com.builderbears.align.ui.theme.ScheduleChipPurple
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ScheduleViewModel : ViewModel() {

	private val activityService = ActivityService()
	private val auth: FirebaseAuth = FirebaseAuth.getInstance()

	var uiState by mutableStateOf(ScheduleUiState(isLoading = true))
		private set

	var actionError by mutableStateOf<String?>(null)
		private set

	var actionMessage by mutableStateOf<String?>(null)
		private set

	var isActionInProgress by mutableStateOf(false)
		private set

	var editableWorkout by mutableStateOf<EditableWorkout?>(null)
		private set

	var isLoadingEditableWorkout by mutableStateOf(false)
		private set

	init {
		loadSchedule()
	}

	fun reload() {
		loadSchedule()
	}

	fun clearActionError() {
		actionError = null
	}

	fun consumeActionMessage() {
		actionMessage = null
	}

	fun dismissEditWorkout() {
		editableWorkout = null
	}

	fun beginEditWorkout(activityId: String) {
		val userId = auth.currentUser?.uid
		if (userId.isNullOrBlank()) {
			actionError = "Not logged in"
			return
		}

		viewModelScope.launch {
			isLoadingEditableWorkout = true
			actionError = null

			activityService.getActivity(userId, activityId)
				.onSuccess { activity ->
					if (activity == null) {
						actionError = "Workout not found"
						editableWorkout = null
					} else {
						editableWorkout = EditableWorkout(
							activityId = activity.activityId,
							name = activity.name,
							description = activity.description,
							workoutType = activity.workoutType,
							date = activity.date,
							time = activity.time
						)
					}
				}
				.onFailure { e ->
					actionError = e.message ?: "Failed to load workout"
					editableWorkout = null
				}

			isLoadingEditableWorkout = false
		}
	}

	fun leaveWorkout(activityId: String) {
		val userId = auth.currentUser?.uid
		if (userId.isNullOrBlank()) {
			actionError = "Not logged in"
			return
		}

		viewModelScope.launch {
			isActionInProgress = true
			actionError = null

			activityService.leaveActivity(activityId, userId)
				.onSuccess {
					actionMessage = "Left workout"
					loadSchedule()
				}
				.onFailure { e ->
					actionError = e.message ?: "Failed to leave workout"
				}

			isActionInProgress = false
		}
	}

	fun saveWorkoutEdits(draft: EditableWorkout) {
		val userId = auth.currentUser?.uid
		if (userId.isNullOrBlank()) {
			actionError = "Not logged in"
			return
		}

		val trimmedName = draft.name.trim()
		if (trimmedName.isBlank()) {
			actionError = "Workout name is required"
			return
		}

		val updates = mapOf(
			"name" to trimmedName,
			"description" to draft.description.trim(),
			"workoutType" to draft.workoutType.trim(),
			"date" to draft.date.trim(),
			"time" to draft.time.trim()
		)

		viewModelScope.launch {
			isActionInProgress = true
			actionError = null

			activityService.editActivity(draft.activityId, userId, updates)
				.onSuccess {
					actionMessage = "Workout updated"
					editableWorkout = null
					loadSchedule()
				}
				.onFailure { e ->
					actionError = e.message ?: "Failed to update workout"
				}

			isActionInProgress = false
		}
	}

	private fun loadSchedule() {
		val userId = auth.currentUser?.uid
		if (userId.isNullOrBlank()) {
			uiState = ScheduleUiState(isLoading = false, error = "Not logged in")
			return
		}

		viewModelScope.launch {
			uiState = ScheduleUiState(isLoading = true)
			activityService.getActivities(userId)
				.onSuccess { activities ->
					val groups = mapActivitiesToMonthGroups(activities)
					uiState = ScheduleUiState(isLoading = false, groups = groups)
				}
				.onFailure { e ->
					uiState = ScheduleUiState(
						isLoading = false,
						error = e.message ?: "Failed to load schedule"
					)
				}
		}
	}

	private fun mapActivitiesToMonthGroups(activities: List<Activity>): List<MonthGroup> {
		val today = LocalDate.now()
		val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
		val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

		val mappedDays = activities.mapNotNull { activity ->
			val date = runCatching { LocalDate.parse(activity.date, dateFormatter) }.getOrNull()
				?: return@mapNotNull null
			val timeParsed = parseTime(activity.time, timeFormatter)

			val typeMeta = workoutTypeMeta(activity.workoutType)

			val attendees = activity.participants.map { participant ->
				Attendee(
					name = participant.name,
					userId = participant.userId,
					profilePhotoUrl = participant.profilePhotoUrl
				)
			}

			val event = WorkoutEvent(
				id = activity.activityId,
				title = activity.name.ifBlank { "Untitled workout" },
				time = activity.time.ifBlank { "Time TBD" },
				location = activity.location.ifBlank { "Location TBD" },
				locationDisplayName = activity.locationDisplayName.ifBlank {
					activity.location.ifBlank { "Location TBD" }
				},
				locationDisplayAddress = activity.locationDisplayAddress,
				locationDirectionsUrl = buildDirectionsUrl(activity),
				type = typeMeta.label,
				typeEmoji = typeMeta.emoji,
				typeColor = typeMeta.color,
				attendees = attendees
			)

			ScheduledDay(
				date = date,
				sortTime = timeParsed,
				isHighlighted = date == today,
				event = event
			)
		}

		return mappedDays
			.groupBy { it.date.withDayOfMonth(1) }
			.toSortedMap()
			.map { (monthStart, days) ->
				MonthGroup(
					month = monthStart.month.getDisplayName(TextStyle.FULL, Locale.US).uppercase(Locale.US),
					days = days.sortedWith(compareBy<ScheduledDay> { it.date }.thenBy { it.sortTime ?: LocalTime.MAX })
				)
			}
	}

	private fun parseTime(raw: String, formatter: DateTimeFormatter): LocalTime? {
		if (raw.isBlank()) return null
		val normalized = raw.trim().uppercase(Locale.US)
		return runCatching { LocalTime.parse(normalized, formatter) }.getOrNull()
	}

	private fun buildDirectionsUrl(activity: Activity): String? {
		val lat = activity.locationLat
		val lng = activity.locationLng
		if (lat != null && lng != null) {
			return Uri.Builder()
				.scheme("https")
				.authority("www.google.com")
				.path("maps/dir/")
				.appendQueryParameter("api", "1")
				.appendQueryParameter("destination", "$lat,$lng")
				.build()
				.toString()
		}

		val placeId = activity.locationPlaceId.trim()
		if (placeId.isNotEmpty()) {
			return Uri.Builder()
				.scheme("https")
				.authority("www.google.com")
				.path("maps/dir/")
				.appendQueryParameter("api", "1")
				.appendQueryParameter("destination_place_id", placeId)
				.build()
				.toString()
		}

		val locationText = activity.location.trim()
		if (locationText.isNotEmpty()) {
			return Uri.Builder()
				.scheme("https")
				.authority("www.google.com")
				.path("maps/dir/")
				.appendQueryParameter("api", "1")
				.appendQueryParameter("destination", locationText)
				.build()
				.toString()
		}

		return null
	}

	private fun workoutTypeMeta(type: String): TypeMeta {
		val normalized = type.trim().lowercase(Locale.US)
		return when (normalized) {
			"run" -> TypeMeta("🏃", "Run", ScheduleChipGreen)
			"gym" -> TypeMeta("🏋️", "Gym", ScheduleChipPurple)
			"yoga" -> TypeMeta("🧘", "Yoga", ScheduleChipPink)
			"cycle", "cycling" -> TypeMeta("🚴", "Cycle", ScheduleChipMint)
			"swim", "swimming" -> TypeMeta("🏊", "Swim", ScheduleChipBlue)
			"basketball" -> TypeMeta("🏀", "Basketball", ScheduleChipOrange)
			"hiit" -> TypeMeta("🔥", "HIIT", ScheduleChipAmber)
			else -> TypeMeta("✨", type.ifBlank { "Other" }.replaceFirstChar { it.titlecase(Locale.US) }, ScheduleChipGray)
		}
	}
}

data class EditableWorkout(
	val activityId: String,
	val name: String,
	val description: String,
	val workoutType: String,
	val date: String,
	val time: String
)

private data class TypeMeta(
	val emoji: String,
	val label: String,
	val color: androidx.compose.ui.graphics.Color
)