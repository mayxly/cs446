package com.builderbears.align.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AlignFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        val type = message.data["type"]

        when (type) {
            "friend_request" -> NotificationService.showFriendRequestNotification(this, title)
            "workout_invite" -> NotificationService.showWorkoutInviteNotification(this, fromName = title)
            "workout_reminder" -> NotificationService.showWorkoutReminderNotification(this)
            "app_reminder" -> NotificationService.showAppReminderNotification(this)
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
