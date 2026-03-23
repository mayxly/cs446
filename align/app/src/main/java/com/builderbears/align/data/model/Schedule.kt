package com.builderbears.align.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

data class WorkoutEvent(
    val id: String,
    val title: String,
    val time: String,
    val duration: String? = null,
    val location: String,
    val type: String,
    val typeEmoji: String,
    val typeColor: Color,
    val attendees: List<Attendee> = emptyList(),
    val extraCount: Int = 0
)

data class Attendee(
    val initials: String,
    val name: String,
    val color: Color
)

data class ScheduledDay(
    val date: LocalDate,
    val sortTime: LocalTime? = null,
    val isHighlighted: Boolean = false,
    val event: WorkoutEvent
) {
    val dayOfWeekLabel: String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US).uppercase(Locale.US)
    val dayNumber: Int = date.dayOfMonth
}

data class MonthGroup(
    val month: String,
    val days: List<ScheduledDay>
)

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val groups: List<MonthGroup> = emptyList(),
    val error: String? = null
)
