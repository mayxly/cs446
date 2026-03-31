package com.builderbears.align.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AlignColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = CardWhite,
    background = PageBase,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    secondary = GradientBlue,
    onSecondary = TextPrimary,
    outline = BorderMuted
)

private val AlignTypography = Typography(
    displayLarge = DisplayStyle,
    displayMedium = DisplayStyle,
    displaySmall = DisplayStyle,
    headlineLarge = HeadingStyle1,
    headlineMedium = HeadingStyle1,
    headlineSmall = HeadingStyle2,
    titleLarge = HeadingStyle2,
    titleMedium = LabelLarge,
    titleSmall = LabelMedium,
    bodyLarge = LabelLarge,
    bodyMedium = LabelMedium,
    bodySmall = LabelSmall,
    labelLarge = LabelLarge,
    labelMedium = LabelMedium,
    labelSmall = Caption
)

@Composable
fun AlignTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlignColorScheme,
        typography = AlignTypography,
        content = content
    )
}
