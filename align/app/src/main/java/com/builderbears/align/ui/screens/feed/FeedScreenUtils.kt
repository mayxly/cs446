package com.builderbears.align.ui.screens.feed

import androidx.compose.ui.graphics.Color
import com.builderbears.align.data.model.Activity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val defaultReactions = listOf("❤️", "🔥", "💪", "👏", "👍")

fun Activity.getWorkoutEmoji(): String {
    return when (workoutType.lowercase()) {
        "run" -> "🏃"
        "gym" -> "🏋️"
        "yoga" -> "🧘"
        "cycle" -> "🚴"
        "swim" -> "🏊"
        "basketball" -> "🏀"
        "hiit" -> "🔥"
        else -> "✨"
    }
}

fun getColorForUserId(userId: String): Color {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFE91E63),
        Color(0xFFFFD700),
        Color(0xFFFF8A65),
    )
    return colors[kotlin.math.abs(userId.hashCode()) % colors.size]
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
