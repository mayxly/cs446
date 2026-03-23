package com.builderbears.align.data.service

import com.builderbears.align.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val usernamesCollection = db.collection("usernames")

    suspend fun createUser(
        name: String,
        email: String,
        password: String,
        username: String
    ): Result<Unit> {
        return try {
            val lowercaseUsername = username.lowercase()

            // Check username is not already taken
            val usernameDoc = usernamesCollection.document(lowercaseUsername).get().await()
            if (usernameDoc.exists()) {
                return Result.failure(Exception("Username already taken."))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("No user ID returned"))

            val batch = db.batch()
            batch.set(
                usersCollection.document(userId),
                User(userId = userId, name = name, email = email, username = lowercaseUsername)
            )
            batch.set(
                usernamesCollection.document(lowercaseUsername),
                mapOf("email" to email, "userId" to userId)
            )
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(emailOrUsername: String, password: String): Result<String> {
        return try {
            val email = if (emailOrUsername.contains("@") && emailOrUsername.contains(".")) {
                emailOrUsername
            } else {
                val doc = usernamesCollection
                    .document(emailOrUsername.lowercase())
                    .get().await()
                if (!doc.exists()) {
                    return Result.failure(Exception("No account found with that username."))
                }
                doc.getString("email")
                    ?: return Result.failure(Exception("Could not resolve username."))
            }

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid
                ?: return Result.failure(Exception("No user ID returned"))
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun migrateUsernameIfMissing(userId: String): Result<Unit> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java) ?: return Result.success(Unit)
            if (user.username.isBlank()) {
                val defaultUsername = user.email.substringBefore("@").lowercase()
                val batch = db.batch()
                batch.update(
                    usersCollection.document(userId),
                    "username", defaultUsername
                )
                batch.set(
                    usernamesCollection.document(defaultUsername),
                    mapOf("email" to user.email, "userId" to userId)
                )
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            Result.success(snapshot.toObject(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUsers(): Result<List<User>> {
        return try {
            val snapshot = usersCollection.get().await()
            Result.success(snapshot.toObjects(User::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        if (userIds.isEmpty()) return Result.success(emptyList())
        return try {
            val users = mutableListOf<User>()
            userIds.distinct().chunked(10).forEach { chunk ->
                val snapshot = usersCollection.whereIn("userId", chunk).get().await()
                users += snapshot.toObjects(User::class.java)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
