package com.builderbears.align.ui.screens.schedule

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
import com.builderbears.align.ui.utils.userColorForId
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

	init {
		loadSchedule()
	}

	fun reload() {
		loadSchedule()
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
					initials = participant.name
						.split(" ")
						.filter { it.isNotBlank() }
						.take(2)
						.joinToString("") { it.first().uppercase() }
						.ifBlank { "U" },
					name = participant.name,
					color = userColorForId(participant.userId)
				)
			}

			val event = WorkoutEvent(
				id = activity.activityId.ifBlank { "${activity.name}-${date}" },
				title = activity.name.ifBlank { "Untitled workout" },
				time = activity.time.ifBlank { "Time TBD" },
				location = activity.location.ifBlank { "Location TBD" },
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

private data class TypeMeta(
	val emoji: String,
	val label: String,
	val color: androidx.compose.ui.graphics.Color
)