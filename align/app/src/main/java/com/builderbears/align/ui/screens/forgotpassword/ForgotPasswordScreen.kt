package com.builderbears.align.ui.screens.forgotpassword

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.components.DefaultGradientBackground
import com.builderbears.align.ui.components.LoginTextField
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.HeadingStyle2
import com.builderbears.align.ui.theme.Indigo
import com.builderbears.align.ui.theme.LabelLarge
import com.builderbears.align.ui.theme.LabelMedium
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    DefaultGradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Forgot password?",
                    style = DisplayStyle,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "No worries - enter your email and we'll\nsend you a reset link.",
                    style = HeadingStyle2,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(60.dp))

                LoginTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    leadingIcon = Icons.Outlined.Email
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    viewModel.errorMessage?.let {
                        Text(it, color = Color.Red, style = LabelMedium)
                    }
                    viewModel.successMessage?.let {
                        Text(it, color = Indigo, style = LabelMedium)
                    }
                }

                Button(
                    onClick = { viewModel.onSendReset() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Send reset link",
                            style = LabelMedium,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Back to login",
                    style = LabelMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { onBack() }
                )
            }
        }
    }
}
