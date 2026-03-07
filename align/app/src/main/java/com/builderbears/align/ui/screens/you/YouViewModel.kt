package com.builderbears.align.ui.screens.you

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class YouViewModel : ViewModel() {
    fun logout(onLoggedOut: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLoggedOut()
    }
}