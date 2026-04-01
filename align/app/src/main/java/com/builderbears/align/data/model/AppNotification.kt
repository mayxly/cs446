package com.builderbears.align.data.model

object NotificationType {
    const val FRIEND_REQUEST = "friend_request"
    const val WORKOUT_INVITE = "workout_invite"
    const val WORKOUT_REMINDER = "workout_reminder"
    const val APP_REMINDER = "app_reminder"
    const val REACTION = "reaction"
    const val PARTICIPANT_NOTE = "participant_note"
    const val PARTICIPANT_IMAGE = "participant_image"
    const val ACTIVITY_REMINDER_30M = "activity_reminder_30m"
    const val ACTIVITY_REMINDER_START = "activity_reminder_start"
}

fun AppNotification.isInboxVisible(): Boolean {
    // no workout invites cuz thats what we decided
    return type != NotificationType.WORKOUT_INVITE
}

data class AppNotification(
    val id: String = "",
    val type: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false
)
