package com.builderbears.align.data.service

import android.net.Uri
import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.model.ActivityParticipant
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActivityService {
    private val db = FirebaseFirestore.getInstance()
    private val maxBatchWrites = 400
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    private fun activitiesCollection(userId: String) =
        db.collection("users").document(userId).collection("activities")

    private fun activityDocument(userId: String, activityId: String) =
        activitiesCollection(userId).document(activityId)

    suspend fun createActivity(activity: Activity): Result<Unit> {
        return try {
            val participantIds = activity.participantIds.distinct().filter { it.isNotBlank() }
            if (participantIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Activity must include at least one participant"))
            }

            val normalizedParticipants = activity.participants
                .filter { it.userId.isNotBlank() }
                .distinctBy { it.userId }

            val activityId = if (activity.activityId.isBlank()) db.collection("activity_ids").document().id else activity.activityId
            val withId = activity.copy(
                activityId = activityId,
                participantIds = participantIds,
                participants = normalizedParticipants
            )

            participantIds.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { participantId ->
                    batch.set(activityDocument(participantId, activityId), withId)
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivities(userId: String): Result<List<Activity>> {
        return try {
            val snapshot = activitiesCollection(userId).get().await()
            Result.success(snapshot.toObjects(Activity::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActivity(userId: String, activityId: String): Result<Activity?> {
        return try {
            val snapshot = activitiesCollection(userId).document(activityId).get().await()
            Result.success(snapshot.toObject(Activity::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPostedStatusForUser(userId: String): Result<Unit> {
        return try {
            if (userId.isBlank()) return Result.success(Unit)

            val snapshot = activitiesCollection(userId).get().await()
            val now = LocalDateTime.now()

            val pendingWrites = snapshot.documents.mapNotNull { document ->
                val activity = document.toObject(Activity::class.java) ?: return@mapNotNull null
                if (activity.activityId.isBlank() || activity.participantIds.isEmpty()) return@mapNotNull null

                val dueByTime = activity.scheduledDateTimeOrNull()?.let { !it.isAfter(now) } == true
                val shouldBePosted = activity.isPosted || dueByTime

                val needsUpdate = shouldBePosted != activity.isPosted
                if (!needsUpdate) return@mapNotNull null

                val updates = mapOf<String, Any>(
                    "isPosted" to shouldBePosted
                )

                activity.activityId to (activity.participantIds.distinct().filter { it.isNotBlank() } to updates)
            }

            pendingWrites.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { (activityId, payload) ->
                    val (participantIds, updates) = payload
                    participantIds.forEach { participantId ->
                        batch.set(activityDocument(participantId, activityId), updates, SetOptions.merge())
                    }
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addActivityPhoto(activityId: String, participantIds: List<String>, imageUri: Uri): Result<String> {
        return try {
            val path = StorageService.newActivityPhotoPath(activityId)
            val url = StorageService.uploadPhoto(path, imageUri).getOrThrow()
            val targets = participantIds.distinct().filter { it.isNotBlank() }
            targets.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { participantId ->
                    batch.update(
                        activityDocument(participantId, activityId),
                        "imageUrls",
                        FieldValue.arrayUnion(url)
                    )
                }
                batch.commit().await()
            }
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivityPhoto(activityId: String, participantIds: List<String>, photoUrl: String): Result<Unit> {
        return try {
            val targets = participantIds.distinct().filter { it.isNotBlank() }
            if (targets.isEmpty()) return Result.success(Unit)

            targets.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { participantId ->
                    batch.update(
                        activityDocument(participantId, activityId),
                        "imageUrls",
                        FieldValue.arrayRemove(photoUrl)
                    )
                }
                batch.commit().await()
            }

            StorageService.deleteFileByUrl(photoUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addParticipantNote(
        activityId: String,
        participantIds: List<String>,
        userId: String,
        note: String
    ): Result<Unit> {
        return try {
            if (userId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid user"))
            }

            val trimmedNote = note.trim()
            if (trimmedNote.isEmpty()) {
                return Result.failure(IllegalArgumentException("Note cannot be empty"))
            }

            val activity = getActivity(userId, activityId).getOrNull()
                ?: return Result.failure(IllegalStateException("Activity not found"))

            if (!activity.participantIds.contains(userId)) {
                return Result.failure(IllegalStateException("Only participants can add notes"))
            }

            if (!activity.participantNotes[userId].isNullOrBlank()) {
                return Result.failure(IllegalStateException("Note already submitted"))
            }

            updateActivity(
                activityId = activityId,
                participantIds = participantIds,
                updates = mapOf("participantNotes.$userId" to trimmedNote)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateActivity(activityId: String, participantIds: List<String>, updates: Map<String, Any>): Result<Unit> {
        return try {
            val targets = participantIds.distinct().filter { it.isNotBlank() }
            if (targets.isEmpty()) return Result.success(Unit)

            targets.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { participantId ->
                    batch.update(activityDocument(participantId, activityId), updates)
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateParticipants(
        activityId: String,
        oldParticipantIds: List<String>,
        newParticipants: List<ActivityParticipant>
    ): Result<Unit> {
        return try {
            val oldIds = oldParticipantIds.distinct().filter { it.isNotBlank() }
            val normalizedParticipants = newParticipants
                .filter { it.userId.isNotBlank() }
                .distinctBy { it.userId }
            val newIds = normalizedParticipants.map { it.userId }

            val seedUserId = (oldIds + newIds).firstOrNull()
                ?: return Result.failure(IllegalArgumentException("No participants provided"))

            val existingActivity = getActivity(seedUserId, activityId).getOrNull()
                ?: return Result.failure(IllegalStateException("Activity not found"))

            val updatedActivity = existingActivity.copy(
                participantIds = newIds,
                participants = normalizedParticipants
            )

            // Upsert for remaining and newly invited participants.
            newIds.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { userId ->
                    batch.set(activityDocument(userId, activityId), updatedActivity, SetOptions.merge())
                }
                batch.commit().await()
            }

            // Delete from users that were removed.
            val removed = oldIds - newIds.toSet()
            removed.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { userId ->
                    batch.delete(activityDocument(userId, activityId))
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveActivity(activityId: String, userId: String): Result<Unit> {
        return try {
            if (userId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid user"))
            }

            val activity = getActivity(userId, activityId).getOrNull()
                ?: return Result.failure(IllegalStateException("Activity not found"))

            val oldIds = activity.participantIds.distinct().filter { it.isNotBlank() }
            if (!oldIds.contains(userId)) {
                return Result.success(Unit)
            }

            val remainingParticipants = activity.participants
                .filter { it.userId != userId }
                .filter { it.userId.isNotBlank() }
                .distinctBy { it.userId }

            updateParticipants(
                activityId = activityId,
                oldParticipantIds = oldIds,
                newParticipants = remainingParticipants
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editActivity(activityId: String, editorUserId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            if (editorUserId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid user"))
            }

            if (updates.isEmpty()) {
                return Result.success(Unit)
            }

            val activity = getActivity(editorUserId, activityId).getOrNull()
                ?: return Result.failure(IllegalStateException("Activity not found"))

            updateActivity(
                activityId = activityId,
                participantIds = activity.participantIds,
                updates = updates
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(activityId: String, participantIds: List<String>): Result<Unit> {
        return try {
            val targets = participantIds.distinct().filter { it.isNotBlank() }
            if (targets.isEmpty()) return Result.success(Unit)

            targets.chunked(maxBatchWrites).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { participantId ->
                    batch.delete(activityDocument(participantId, activityId))
                }
                batch.commit().await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Activity.scheduledDateTimeOrNull(): LocalDateTime? {
        val parsedDate = runCatching { LocalDate.parse(date, dateFormatter) }.getOrNull() ?: return null
        val parsedTime = runCatching { LocalTime.parse(time.trim().uppercase(Locale.US), timeFormatter) }.getOrNull()
            ?: return null
        return LocalDateTime.of(parsedDate, parsedTime)
    }
}
