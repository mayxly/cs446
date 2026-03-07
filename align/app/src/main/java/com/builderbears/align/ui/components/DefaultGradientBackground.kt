package com.builderbears.align.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import com.builderbears.align.ui.theme.GridTopLeft
import com.builderbears.align.ui.theme.GridTopRight
import com.builderbears.align.ui.theme.GridBottomLeft
import com.builderbears.align.ui.theme.GridBottomRight

@Composable
fun DefaultGradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val w = size.width
                val h = size.height
                val radius = maxOf(w, h) * 0.85f

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GridTopLeft, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = radius
                    ),
                    size = size
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GridTopRight, Color.Transparent),
                        center = Offset(w, 0f),
                        radius = radius
                    ),
                    size = size
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GridBottomLeft, Color.Transparent),
                        center = Offset(0f, h),
                        radius = radius
                    ),
                    size = size
                )

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(GridBottomRight, Color.Transparent),
                        center = Offset(w, h),
                        radius = radius
                    ),
                    size = size
                )
            }
    ) {
        content()
    }
}
