package com.builderbears.align.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.builderbears.align.ui.components.LoginToggle
import com.builderbears.align.ui.components.LoginMode
import com.builderbears.align.ui.components.LoginTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE8D5F5), Color(0xFFD5E8F5), Color(0xFFD5F5E3))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text("align", fontSize = 48.sp, fontWeight = FontWeight.Bold)
            Text("workout together.", fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoginToggle(
                        selected = viewModel.authMode,
                        onToggle = {
                            viewModel.authMode = it
                            viewModel.clearError()
                        }
                    )

                    if (viewModel.authMode == LoginMode.SIGNUP) {
                        LoginTextField(
                            value = viewModel.displayName,
                            onValueChange = { viewModel.displayName = it },
                            label = "DISPLAY NAME",
                            placeholder = "Your name",
                            leadingIcon = Icons.Default.Person
                        )
                    }

                    LoginTextField(
                        value = viewModel.email,
                        onValueChange = { viewModel.email = it },
                        label = "EMAIL",
                        placeholder = "you@example.com",
                        leadingIcon = Icons.Default.Email
                    )

                    LoginTextField(
                        value = viewModel.password,
                        onValueChange = { viewModel.password = it },
                        label = "PASSWORD",
                        placeholder = if (viewModel.authMode == LoginMode.SIGNUP) "Min. 8 characters" else "Enter your password",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true
                    )

                    if (viewModel.authMode == LoginMode.LOGIN) {
                        Text(
                            "Forgot password?",
                            color = Color.Blue,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable { /* TODO */ }
                        )
                    }

                    viewModel.errorMessage?.let {
                        Text(it, color = Color.Red, fontSize = 12.sp)
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
                        shape = RoundedCornerShape(50)
                    ) {
                        if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text(if (viewModel.authMode == LoginMode.LOGIN) "Log in" else "Create Account")
                    }
                }
            }
        }
    }
}
