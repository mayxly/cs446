package com.builderbears.align.ui.screens.forgotpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.service.UserService
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val userService = UserService()

    var email by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun onSendReset() {
        if (email.isBlank()) {
            errorMessage = "Please enter your email."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            userService.sendPasswordReset(email)
                .onSuccess {
                    successMessage = "Reset link sent! Check your inbox."
                }
                .onFailure {
                    errorMessage = when {
                        it.message?.contains("no user record") == true ->
                            "No account found with that email."
                        it.message?.contains("badly formatted") == true ->
                            "Please enter a valid email address."
                        else -> "Something went wrong. Please try again."
                    }
                }
            isLoading = false
        }
    }
}
