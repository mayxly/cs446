package com.builderbears.align.data.model

data class AppNotification(
    val id: String = "",
    val type: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)
