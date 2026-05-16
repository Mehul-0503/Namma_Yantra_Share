package com.nammayantra.share.data.repository

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nammayantra.share.data.model.User
import com.nammayantra.share.util.Constants
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

object AuthRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    // mockUser = null means NOT logged in (DEMO_MODE)
    private var mockUser: User? = null

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        if (Constants.DEMO_MODE) {
            mockUser = User("demo_uid", "Demo User", email, "farmer")
            return@runCatching "demo_uid"
        }
        auth.signInWithEmailAndPassword(email, password).await()
        auth.currentUser?.uid ?: throw IllegalStateException("Unable to get user")
    }

    suspend fun signup(name: String, email: String, password: String, role: String): Result<String> = runCatching {
        if (Constants.DEMO_MODE) {
            mockUser = User("demo_uid", name, email, role)
            return@runCatching "demo_uid"
        }
        // Step 1: Create the Firebase Auth account (fast, ~1-2 sec)
        auth.createUserWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Unable to get user")

        // Step 2: Save profile to Firestore — best-effort with 8s timeout.
        // If Firestore is slow or rules block it, signup still succeeds.
        withTimeoutOrNull(8_000L) {
            runCatching {
                db.collection("users").document(uid)
                    .set(User(uid, name, email, role), SetOptions.merge()).await()
            }
        }
        uid
    }

    suspend fun getUser(uid: String): Result<User> = runCatching {
        if (Constants.DEMO_MODE) {
            return@runCatching mockUser
                ?: throw IllegalStateException("No user logged in")
        }
        db.collection("users").document(uid).get().await().toObject<User>()
            ?: throw IllegalStateException("User not found")
    }

    /**
     * Returns the current user's UID, or null if not logged in.
     * In DEMO_MODE this correctly returns null after logout() is called.
     */
    fun currentUserId(): String? {
        if (Constants.DEMO_MODE) return mockUser?.id  // null after logout
        return auth.currentUser?.uid
    }

    fun logout() {
        if (Constants.DEMO_MODE) {
            mockUser = null   // This makes currentUserId() return null → app treats as logged out
            return
        }
        auth.signOut()
    }
}
