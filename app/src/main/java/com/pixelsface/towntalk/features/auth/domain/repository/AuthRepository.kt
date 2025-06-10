package com.pixelsface.towntalk.features.auth.domain.repository

import android.content.Intent
import com.google.firebase.auth.FirebaseUser
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: FirebaseUser?

    suspend fun registerWithEmail(email: String, password: String, name: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithPhone(verificationId: String, code: String): Result<User>
    suspend fun signOut()
    fun getAuthState(): Flow<Boolean>

    suspend fun getGoogleSignInIntent(): Result<Intent>
    suspend fun handleGoogleSignInResult(data: Intent): Result<User>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateProfile(name: String, photoUrl: String?): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    // Phone Authentication
    suspend fun sendPhoneVerificationCode(phoneNumber: String): Result<String>
    suspend fun verifyPhoneNumber(verificationId: String, code: String): Result<User>

    /**
     * Retrieves a user's details by their ID.
     * @param userId The ID of the user to fetch.
     * @return A Result containing the User object or null if not found, or an error.
     */
    suspend fun getUserById(userId: String): Result<User?>

    /**
     * Retrieves a list of all users.
     * (Note: For large user bases, pagination/search would be necessary here)
     * @return A Result containing a list of all User objects or an error.
     */
    suspend fun getAllUsers(): Result<List<User>>

    // User Presence
    fun getUserStream(userId: String): Flow<Result<User?>>
    suspend fun setOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long? = null): Result<Unit>
} 