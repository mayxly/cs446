package com.builderbears.align.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.builderbears.align.ui.theme.LabelMedium
import com.builderbears.align.ui.theme.TextPrimary
import com.builderbears.align.ui.theme.TextMuted

enum class LoginMode { LOGIN, SIGNUP }

@Composable
fun LoginToggle(
    selected: LoginMode,
    onToggle: (LoginMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0EEF8))
            .padding(4.dp)
    ) {
        LoginMode.entries.forEach { mode ->
            val isSelected = selected == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .clickable { onToggle(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (mode == LoginMode.LOGIN) "Log in" else "Sign up",
                    style = LabelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextMuted
                )
            }
        }
    }
}
