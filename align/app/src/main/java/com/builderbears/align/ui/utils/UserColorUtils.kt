package com.builderbears.align.ui.utils

import androidx.compose.ui.graphics.Color
import com.builderbears.align.ui.theme.AvatarGreen
import com.builderbears.align.ui.theme.AvatarGreen2
import com.builderbears.align.ui.theme.AvatarOrange
import com.builderbears.align.ui.theme.AvatarPink
import com.builderbears.align.ui.theme.AvatarYellow

private val userColorPalette: List<Color> = listOf(
    AvatarGreen,
    AvatarPink,
    AvatarYellow,
    AvatarGreen2,
    AvatarOrange
)

fun userColorForId(userId: String): Color {
    if (userId.isBlank()) return userColorPalette.first()
    return userColorPalette[kotlin.math.abs(userId.hashCode()) % userColorPalette.size]
}
