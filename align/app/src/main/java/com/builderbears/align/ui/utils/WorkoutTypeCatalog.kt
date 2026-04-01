package com.builderbears.align.ui.utils

import androidx.compose.ui.graphics.Color
import com.builderbears.align.ui.theme.ScheduleChipAmber
import com.builderbears.align.ui.theme.ScheduleChipBlue
import com.builderbears.align.ui.theme.ScheduleChipGray
import com.builderbears.align.ui.theme.ScheduleChipGreen
import com.builderbears.align.ui.theme.ScheduleChipMint
import com.builderbears.align.ui.theme.ScheduleChipOrange
import com.builderbears.align.ui.theme.ScheduleChipPink
import com.builderbears.align.ui.theme.ScheduleChipPurple
import java.util.Locale

data class WorkoutTypeOption(
    val key: String,
    val label: String,
    val emoji: String,
    val chipColor: Color,
    val estimatedMinutes: Int
)

object WorkoutTypeCatalog {
    private val options = listOf(
        WorkoutTypeOption("gym", "Gym", "🏋️", ScheduleChipPurple, 120),
        WorkoutTypeOption("yoga", "Yoga", "🧘", ScheduleChipPink, 60),
        WorkoutTypeOption("run", "Run", "🏃", ScheduleChipGreen, 30),
        WorkoutTypeOption("sports", "Sports", "🏀", ScheduleChipOrange, 120),
        WorkoutTypeOption("swim", "Swim", "🏊", ScheduleChipBlue, 60),
        WorkoutTypeOption("hitt", "HITT", "🔥", ScheduleChipAmber, 60),
        WorkoutTypeOption("climbing", "Climbing", "🧗", ScheduleChipMint, 90),
        WorkoutTypeOption("other", "Other", "✨", ScheduleChipGray, 60)
    )

    private val byKey = options.associateBy { it.key }

    private val aliases = mapOf(
        "sport" to "sports",
        "swimming" to "swim",
        "hiit" to "hitt",
        "climb" to "climbing",
        // Legacy values mapped to new taxonomy
        "cycle" to "other",
        "cycling" to "other",
        "basketball" to "sports"
    )

    val pickerOptions: List<WorkoutTypeOption>
        get() = options

    fun key(rawType: String): String {
        val normalized = rawType.trim().lowercase(Locale.US)
        return when {
            byKey.containsKey(normalized) -> normalized
            else -> aliases[normalized] ?: "other"
        }
    }

    fun option(rawType: String): WorkoutTypeOption {
        return byKey[key(rawType)] ?: byKey.getValue("other")
    }

    fun displayLabel(rawType: String): String = option(rawType).label

    fun emoji(rawType: String): String = option(rawType).emoji

    fun chipColor(rawType: String): Color = option(rawType).chipColor

    fun estimatedMinutes(rawType: String): Int = option(rawType).estimatedMinutes
}
