package com.builderbears.align.data.service

import com.builderbears.align.data.model.AppNotification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendService {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val inboxService = InboxService()

    suspend fun sendRequest(fromUserId: String, toUserId: String, fromUserName: String): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(usersCollection.document(fromUserId), "friends.$toUserId", "SENT")
            batch.update(usersCollection.document(toUserId), "friends.$fromUserId", "PENDING")
            batch.commit().await()

            inboxService.createNotification(
                toUserId,
                AppNotification(
                    type = "friend_request",
                    fromUserId = fromUserId,
                    fromUserName = fromUserName,
                    message = "requested to follow you",
                    timestamp = System.currentTimeMillis()
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptRequest(currentUserId: String, requesterUserId: String): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(usersCollection.document(currentUserId), "friends.$requesterUserId", "ACCEPTED")
            batch.update(usersCollection.document(requesterUserId), "friends.$currentUserId", "ACCEPTED")
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelRequest(currentUserId: String, otherUserId: String): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(usersCollection.document(currentUserId), "friends.$otherUserId", FieldValue.delete())
            batch.update(usersCollection.document(otherUserId), "friends.$currentUserId", FieldValue.delete())
            batch.commit().await()

            // Best-effort: delete the friend_request notification from the receiver's inbox
            try {
                val notifications = usersCollection.document(otherUserId)
                    .collection("notifications")
                    .whereEqualTo("type", "friend_request")
                    .whereEqualTo("fromUserId", currentUserId)
                    .get().await()
                for (doc in notifications.documents) {
                    doc.reference.delete().await()
                }
            } catch (_: Exception) { }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriend(currentUserId: String, otherUserId: String): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(usersCollection.document(currentUserId), "friends.$otherUserId", FieldValue.delete())
            batch.update(usersCollection.document(otherUserId), "friends.$currentUserId", FieldValue.delete())
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
