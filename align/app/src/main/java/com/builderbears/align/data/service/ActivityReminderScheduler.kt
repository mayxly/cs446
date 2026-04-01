package com.builderbears.align.data.service

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.builderbears.align.data.model.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

object ActivityReminderScheduler {
    private const val TAG = "ActivityReminderScheduler"
    private const val REMINDER_TAG_PREFIX = "activity-reminders-"

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    suspend fun syncForCurrentUser(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (userId.isBlank()) return

        val manager = WorkManager.getInstance(context)
        val userTag = "$REMINDER_TAG_PREFIX$userId"

        manager.cancelAllWorkByTag(userTag)

        val activities = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("activities")
            .get()
            .await()
            .toObjects(Activity::class.java)
            .distinctBy { it.activityId }
            .filter { !it.isPosted }

        val now = System.currentTimeMillis()

        activities.forEach { activity ->
            val startAtMillis = activityStartTimeMillis(activity) ?: return@forEach

            enqueueReminder(
                manager = manager,
                userTag = userTag,
                userId = userId,
                activity = activity,
                reminderKind = ActivityReminderWorker.REMINDER_KIND_THIRTY_MIN,
                triggerAtMillis = startAtMillis - TimeUnit.MINUTES.toMillis(30),
                nowMillis = now
            )

            enqueueReminder(
                manager = manager,
                userTag = userTag,
                userId = userId,
                activity = activity,
                reminderKind = ActivityReminderWorker.REMINDER_KIND_START_TIME,
                triggerAtMillis = startAtMillis,
                nowMillis = now
            )
        }
    }

    private fun enqueueReminder(
        manager: WorkManager,
        userTag: String,
        userId: String,
        activity: Activity,
        reminderKind: String,
        triggerAtMillis: Long,
        nowMillis: Long
    ) {
        if (activity.activityId.isBlank()) return

        val delayMillis = triggerAtMillis - nowMillis
        // Skip 30-min reminder if its window has already passed
        if (delayMillis <= 0L && reminderKind == ActivityReminderWorker.REMINDER_KIND_THIRTY_MIN) return
        val effectiveDelay = delayMillis.coerceAtLeast(0L)
        val uniqueName = listOf(
            "activity-reminder",
            userId,
            activity.activityId,
            activity.date,
            activity.time,
            reminderKind
        ).joinToString("-")

        val request = OneTimeWorkRequestBuilder<ActivityReminderWorker>()
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ActivityReminderWorker.KEY_USER_ID to userId,
                    ActivityReminderWorker.KEY_WORKOUT_NAME to activity.name,
                    ActivityReminderWorker.KEY_REMINDER_KIND to reminderKind
                )
            )
            .addTag(userTag)
            .build()

        manager.enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, request)
    }

    private fun activityStartTimeMillis(activity: Activity): Long? {
        return runCatching {
            val date = LocalDate.parse(activity.date, dateFormatter)
            val time = LocalTime.parse(activity.time.trim().uppercase(Locale.US), timeFormatter)
            LocalDateTime.of(date, time)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.onFailure {
            Log.w(TAG, "Unable to parse activity time for ${activity.activityId}", it)
        }.getOrNull()
    }
}
