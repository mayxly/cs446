package com.builderbears.align.data.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.builderbears.align.data.model.AppNotification
import com.builderbears.align.data.model.NotificationType

class ActivityReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString(KEY_USER_ID).orEmpty()
        val workoutName = inputData.getString(KEY_WORKOUT_NAME).orEmpty().ifBlank { "Workout" }
        val reminderKind = inputData.getString(KEY_REMINDER_KIND).orEmpty()

        if (userId.isBlank() || reminderKind.isBlank()) {
            return Result.failure()
        }

        val (notificationType, message) = when (reminderKind) {
            REMINDER_KIND_THIRTY_MIN -> {
                NotificationType.ACTIVITY_REMINDER_30M to "$workoutName starts in 30 min"
            }

            REMINDER_KIND_START_TIME -> {
                NotificationType.ACTIVITY_REMINDER_START to "$workoutName is starting now"
            }

            else -> return Result.failure()
        }

        val writeResult = InboxService().createNotification(
            toUserId = userId,
            notification = AppNotification(
                type = notificationType,
                fromUserId = SYSTEM_FROM_USER_ID,
                fromUserName = SYSTEM_FROM_USER_NAME,
                message = message,
                timestamp = System.currentTimeMillis()
            )
        )

        return if (writeResult.isSuccess) Result.success() else Result.retry()
    }

    companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_WORKOUT_NAME = "workout_name"
        const val KEY_REMINDER_KIND = "reminder_kind"

        const val REMINDER_KIND_THIRTY_MIN = "thirty_minutes"
        const val REMINDER_KIND_START_TIME = "start_time"

        private const val SYSTEM_FROM_USER_ID = "system"
        private const val SYSTEM_FROM_USER_NAME = "ALIGN"
    }
}
