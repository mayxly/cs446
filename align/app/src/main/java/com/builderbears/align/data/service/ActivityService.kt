package com.builderbears.align.data.service

import com.builderbears.align.data.model.Activity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ActivityService {
    private val db = FirebaseFirestore.getInstance()

    private fun activitiesCollection(userId: String) =
        db.collection("users").document(userId).collection("activities")

    suspend fun createActivity(userId: String, activity: Activity): Result<Unit> {
        return try {
            val docRef = activitiesCollection(userId).document()
            val withId = activity.copy(activityId = docRef.id)
            docRef.set(withId).await()
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

    suspend fun updateActivity(userId: String, activityId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            activitiesCollection(userId).document(activityId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteActivity(userId: String, activityId: String): Result<Unit> {
        return try {
            activitiesCollection(userId).document(activityId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
