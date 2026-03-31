package com.builderbears.align.data.service

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object StorageService {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(storagePath: String, imageUri: Uri): Result<String> = try {
        val ref = storage.reference.child(storagePath)
        ref.putFile(imageUri).await()
        Result.success(ref.downloadUrl.await().toString())
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun newActivityPhotoPath(activityId: String): String =
        "activity_photos/$activityId/${UUID.randomUUID()}.jpg"

    fun profilePhotoPath(userId: String): String =
        "profile_photos/$userId.jpg"
}
