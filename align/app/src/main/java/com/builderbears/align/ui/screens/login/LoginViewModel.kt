package com.builderbears.align.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.service.UserService
import com.builderbears.align.ui.components.LoginMode
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userService = UserService()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var displayName by mutableStateOf("")
    var authMode by mutableStateOf(LoginMode.LOGIN)

    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    private fun clearFields() {
        email = ""
        password = ""
        displayName = ""
        errorMessage = null
    }

    fun onAuthModeChange(mode: LoginMode) {
        authMode = mode
        clearFields()
    }

    fun onLogin(onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields."
            return
        }
        viewModelScope.launch {
            isLoading = true
            userService.loginUser(email, password)
                .onSuccess {
                    clearFields()
                    onSuccess()
                }
                .onFailure { errorMessage = "Incorrect email or password. Please try again." }
            isLoading = false
        }
    }

    fun onSignUp(onSuccess: () -> Unit) {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields."
            return
        }
        if (password.length < 8) {
            errorMessage = "Password must have minimum 8 characters."
            return
        }
        viewModelScope.launch {
            isLoading = true
            userService.createUser(displayName, email, password)
                .onSuccess {
                    clearFields()
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = when {
                        e.message?.contains("email address is already in use") == true ->
                            "An account with this email already exists."
                        else -> e.message
                    }
                }
            isLoading = false
        }
    }

    fun clearError() { errorMessage = null }
}
