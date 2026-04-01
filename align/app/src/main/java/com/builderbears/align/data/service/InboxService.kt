package com.builderbears.align.data.service

import com.builderbears.align.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InboxService {
    private val db = FirebaseFirestore.getInstance()

    private fun notificationsCollection(userId: String) =
        db.collection("users").document(userId).collection("notifications")

    fun getNotificationsFlow(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val registration = notificationsCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(AppNotification::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun getNotifications(userId: String): Result<List<AppNotification>> {
        return try {
            val snapshot = notificationsCollection(userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(AppNotification::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNotification(toUserId: String, notification: AppNotification): Result<Unit> {
        return try {
            val docRef = notificationsCollection(toUserId).document()
            val withId = notification.copy(id = docRef.id)
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> {
        return try {
            notificationsCollection(userId).document(notificationId)
                .update("read", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(userId: String, notificationId: String): Result<Unit> {
        return try {
            notificationsCollection(userId).document(notificationId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAll(userId: String): Result<Unit> {
        return try {
            val docs = notificationsCollection(userId).get().await()
            val batch = db.batch()
            docs.documents.forEach { batch.delete(it.reference) }
            if (docs.documents.isNotEmpty()) batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
