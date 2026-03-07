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

enum class LoginMode { LOGIN, SIGNUP }

@Composable
fun LoginToggle(
    selected: LoginMode,
    onToggle: (LoginMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(if (selected == LoginMode.LOGIN) Color.White else Color.Transparent)
                .clickable { onToggle(LoginMode.LOGIN) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Log in", fontWeight = if (selected == LoginMode.LOGIN) FontWeight.Bold else FontWeight.Normal)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(if (selected == LoginMode.SIGNUP) Color.White else Color.Transparent)
                .clickable { onToggle(LoginMode.SIGNUP) }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Sign up", fontWeight = if (selected == LoginMode.SIGNUP) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
