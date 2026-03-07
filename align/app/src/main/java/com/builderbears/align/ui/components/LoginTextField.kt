package com.builderbears.align.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.builderbears.align.ui.theme.BorderMuted
import com.builderbears.align.ui.theme.InputBackground
import com.builderbears.align.ui.theme.Micro
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(InputBackground, RoundedCornerShape(12.dp))
            .border(1.dp, BorderMuted.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = Micro,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(2.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = Micro.copy(color = TextPrimary),
                    visualTransformation = if (isPassword && !passwordVisible)
                        PasswordVisualTransformation() else VisualTransformation.None,
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(text = placeholder, style = Micro, color = TextMuted)
                        }
                        innerTextField()
                    }
                )
            }

            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
