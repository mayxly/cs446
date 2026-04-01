package com.builderbears.align.data.service

import com.builderbears.align.data.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class InboxService {
    private val db = FirebaseFirestore.getInstance()

    private fun notificationsCollection(userId: String) =
        db.collection("users").document(userId).collection("notifications")

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
}
