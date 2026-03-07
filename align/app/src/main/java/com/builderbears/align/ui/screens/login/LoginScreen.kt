package com.builderbears.align.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.components.DefaultGradientBackground
import com.builderbears.align.ui.components.LoginMode
import com.builderbears.align.ui.components.LoginTextField
import com.builderbears.align.ui.components.LoginToggle
import com.builderbears.align.ui.theme.DisplayStyle
import com.builderbears.align.ui.theme.HeadingStyle2
import com.builderbears.align.ui.theme.Indigo
import com.builderbears.align.ui.theme.LabelLarge
import com.builderbears.align.ui.theme.LabelMedium
import com.builderbears.align.ui.theme.TextMuted
import com.builderbears.align.ui.theme.TextPrimary

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    DefaultGradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            // Title centered to the FULL screen
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top)
                    .padding(top = LocalConfiguration.current.screenHeightDp.dp * 0.35f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "align",
                    style = DisplayStyle.copy(fontSize = 48.sp),
                    color = TextPrimary
                )
                Text(
                    text = "workout together.",
                    style = HeadingStyle2,
                    color = TextMuted
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoginToggle(
                        selected = viewModel.authMode,
                        onToggle = { viewModel.onAuthModeChange(it) }
                    )

                    if (viewModel.authMode == LoginMode.SIGNUP) {
                        LoginTextField(
                            value = viewModel.displayName,
                            onValueChange = { viewModel.displayName = it },
                            label = "DISPLAY NAME",
                            placeholder = "Your name",
                            leadingIcon = Icons.Outlined.Person
                        )
                    }

                    LoginTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        leadingIcon = Icons.Outlined.Email
                    )

                    LoginTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = "PASSWORD",
                        placeholder = if (viewModel.authMode == LoginMode.SIGNUP) "Min. 8 characters" else "Enter your password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true
                    )

                    if (viewModel.authMode == LoginMode.LOGIN) {
                        Text(
                            "Forgot password?",
                            color = Indigo,
                            style = LabelMedium,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { /* TODO */ }
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                        viewModel.errorMessage?.let {
                            Text(it, color = Color.Red, style = LabelMedium)
                        }
                    }

                    Button(
                        onClick = {
                            if (viewModel.authMode == LoginMode.LOGIN)
                                viewModel.onLogin(onLoginSuccess)
                            else
                                viewModel.onSignUp(onLoginSuccess)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                    ) {
                        if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text(
                            text = if (viewModel.authMode == LoginMode.LOGIN) "Log in" else "Create Account",
                            style = LabelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
