package com.builderbears.align.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.builderbears.align.R

object NotificationService {

    private const val CHANNEL_FRIEND_REQUESTS = "friend_requests"
    private const val CHANNEL_WORKOUT_INVITES = "workout_invites"
    private const val CHANNEL_WORKOUT_REMINDERS = "workout_reminders"
    private const val CHANNEL_APP_REMINDERS = "app_reminders"
    private const val CHANNEL_ACTIVITY_UPDATES = "activity_updates"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_FRIEND_REQUESTS,
                "Friend Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications when someone sends you a friend request" },

            NotificationChannel(
                CHANNEL_WORKOUT_INVITES,
                "Workout Invites",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications when you're invited to a workout" },

            NotificationChannel(
                CHANNEL_WORKOUT_REMINDERS,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders for upcoming workouts" },

            NotificationChannel(
                CHANNEL_APP_REMINDERS,
                "App Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders to log your activity" },

            NotificationChannel(
                CHANNEL_ACTIVITY_UPDATES,
                "Activity Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifications for reactions, notes, and photos on your workouts" }
        )

        manager.createNotificationChannels(channels)
    }

    fun showFriendRequestNotification(context: Context, fromName: String = "Alex") {
        show(
            context,
            id = 1001,
            channel = CHANNEL_FRIEND_REQUESTS,
            title = "New Friend Request \uD83D\uDC4B",
            body = "$fromName sent you a friend request!"
        )
    }

    fun showWorkoutInviteNotification(context: Context, fromName: String = "Alex", workoutName: String = "Morning Run") {
        show(
            context,
            id = 1002,
            channel = CHANNEL_WORKOUT_INVITES,
            title = "Workout Invite",
            body = "$fromName invited you to $workoutName"
        )
    }

    fun showWorkoutReminderNotification(
        context: Context,
        workoutName: String = "Yoga",
        minutesUntil: Int = 30,
        messageOverride: String? = null
    ) {
        show(
            context,
            id = 1003,
            channel = CHANNEL_WORKOUT_REMINDERS,
            title = "Upcoming Workout",
            body = messageOverride ?: "$workoutName starts in $minutesUntil minutes"
        )
    }

    fun showActivityUpdateNotification(context: Context, title: String, body: String) {
        show(
            context,
            id = 1005,
            channel = CHANNEL_ACTIVITY_UPDATES,
            title = title,
            body = body
        )
    }

    fun showAppReminderNotification(context: Context) {
        show(
            context,
            id = 1004,
            channel = CHANNEL_APP_REMINDERS,
            title = "Don't forget!",
            body = "Don't forget to log your workout today!"
        )
    }

    private fun show(context: Context, id: Int, channel: String, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}
