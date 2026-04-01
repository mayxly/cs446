package com.builderbears.align.data.service

import com.builderbears.align.data.model.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AlignFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title.orEmpty()
        val body = message.notification?.body.orEmpty()
        val type = message.data["type"].orEmpty()

        when (type) {
            NotificationType.FRIEND_REQUEST -> NotificationService.showFriendRequestNotification(this, fromName = title)
            NotificationType.WORKOUT_INVITE -> NotificationService.showWorkoutInviteNotification(this, fromName = title)
            NotificationType.WORKOUT_REMINDER,
            NotificationType.ACTIVITY_REMINDER_30M,
            NotificationType.ACTIVITY_REMINDER_START -> {
                NotificationService.showWorkoutReminderNotification(
                    this,
                    workoutName = title.ifBlank { "Workout" },
                    messageOverride = body.ifBlank { null }
                )
            }
            NotificationType.REACTION,
            NotificationType.PARTICIPANT_NOTE,
            NotificationType.PARTICIPANT_IMAGE -> {
                NotificationService.showActivityUpdateNotification(
                    this,
                    title = title.ifBlank { "Activity update" },
                    body = body.ifBlank { "There is a new update on your workout." }
                )
            }
            NotificationType.APP_REMINDER -> NotificationService.showAppReminderNotification(this)
            else -> NotificationService.showAppReminderNotification(this)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users").document(userId)
            .update("fcmToken", token)
    }
}
