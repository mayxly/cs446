package com.builderbears.align.data.service

import com.builderbears.align.data.model.Activity
import com.builderbears.align.data.model.ActivityParticipant
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ActivityService {
    private val db = FirebaseFirestore.getInstance()
    private val maxBatchWrites = 400

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
}
