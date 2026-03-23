package com.builderbears.align.ui.screens.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.builderbears.align.ui.components.DefaultGradientBackground
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.TextPrimary

@Composable
fun LoadingScreen() {
    DefaultGradientBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "align",
                style = DisplayStyle.copy(fontSize = 48.sp),
                color = TextPrimary
            )
        }
    }
}
