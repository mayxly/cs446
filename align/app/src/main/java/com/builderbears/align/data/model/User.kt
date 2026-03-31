package com.builderbears.align.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val username: String = "",
    val profilePhotoUrl: String = "",
    val pushNotificationsEnabled: Boolean = false
)
