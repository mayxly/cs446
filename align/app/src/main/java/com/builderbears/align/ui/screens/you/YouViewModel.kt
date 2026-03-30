package com.builderbears.align.ui.screens.you

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.builderbears.align.data.model.User
import com.builderbears.align.data.service.UserService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class YouViewModel : ViewModel() {
    private val userService = UserService()
    private val auth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _profilePhotoUrl = MutableStateFlow<String?>(null)
    val profilePhotoUrl: StateFlow<String?> = _profilePhotoUrl

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            userService.getUser(userId)
                .onSuccess { user ->
                    _user.value = user
                    _profilePhotoUrl.value = user?.profilePhotoUrl?.takeIf { it.isNotBlank() }
                }
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            userService.uploadProfilePhoto(userId, imageUri)
                .onSuccess { downloadUrl ->
                    _profilePhotoUrl.value = downloadUrl
                    _user.value = _user.value?.copy(profilePhotoUrl = downloadUrl)
                }
            _isUploadingPhoto.value = false
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        auth.signOut()
        _user.value = null
        _profilePhotoUrl.value = null
        onLoggedOut()
    }
}
