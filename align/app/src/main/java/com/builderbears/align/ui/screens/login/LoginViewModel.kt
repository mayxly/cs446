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

    // Login fields
    var emailOrUsername by mutableStateOf("")

    // Signup fields
    var email by mutableStateOf("")
    var username by mutableStateOf("")
    var displayName by mutableStateOf("")

    // Shared
    var password by mutableStateOf("")
    var authMode by mutableStateOf(LoginMode.LOGIN)
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    private fun clearFields() {
        emailOrUsername = ""
        email = ""
        password = ""
        displayName = ""
        username = ""
        errorMessage = null
    }

    fun onAuthModeChange(mode: LoginMode) {
        authMode = mode
        clearFields()
    }

    fun onLogin(onSuccess: () -> Unit) {
        if (emailOrUsername.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields."
            return
        }
        viewModelScope.launch {
            isLoading = true
            userService.loginUser(emailOrUsername, password)
                .onSuccess { userId ->
                    userService.migrateUsernameIfMissing(userId)
                    clearFields()
                    onSuccess()
                }
                .onFailure {
                    errorMessage = "Incorrect email/username or password. Please try again."
                }
            isLoading = false
        }
    }

    fun onSignUp(onSuccess: () -> Unit) {
        if (displayName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all fields."
            return
        }
        if (password.length < 8) {
            errorMessage = "Password must have minimum 8 characters."
            return
        }
        if (username.contains(" ")) {
            errorMessage = "Username cannot contain spaces."
            return
        }
        viewModelScope.launch {
            isLoading = true
            userService.createUser(displayName, email, password, username)
                .onSuccess {
                    clearFields()
                    onSuccess()
                }
                .onFailure { e ->
                    errorMessage = when {
                        e.message?.contains("email address is already in use") == true ->
                            "An account with this email already exists."
                        e.message?.contains("Username already taken") == true ->
                            "That username is already taken."
                        else -> e.message
                    }
                }
            isLoading = false
        }
    }
}
