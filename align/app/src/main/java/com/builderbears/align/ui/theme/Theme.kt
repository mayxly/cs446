package com.builderbears.align.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AlignColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = PageBase,
    onBackground = TextPrimary,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = TextPrimary,
    secondary = GradientBlue,
    onSecondary = TextPrimary,
    outline = BorderMuted
)

@Composable
fun AlignTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlignColorScheme,
        content = content
    )
}
