package com.builderbears.align.ui.screens.feed

import androidx.compose.ui.graphics.Color
import com.builderbears.align.data.model.Activity
import com.builderbears.align.ui.utils.WorkoutTypeCatalog
import com.builderbears.align.ui.utils.userColorForId
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val defaultReactions = listOf("❤️", "🔥", "💪", "👏", "👍")

fun Activity.getWorkoutEmoji(): String {
    return WorkoutTypeCatalog.emoji(workoutType)
}

fun Activity.getWorkoutLabel(): String = WorkoutTypeCatalog.displayLabel(workoutType)

fun getColorForUserId(userId: String): Color {
    return userColorForId(userId)
}

fun formatActivityDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        val daysAgo = java.time.temporal.ChronoUnit.DAYS.between(date, today)

        when {
            daysAgo <= 7 && daysAgo >= 0 -> {
                // Within past week: show day of the week
                date.format(DateTimeFormatter.ofPattern("EEEE"))
            }
            else -> {
                date.format(DateTimeFormatter.ofPattern("MMMM d"))
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
