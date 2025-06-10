package com.pixelsface.towntalk.core.domain.repository

import com.pixelsface.towntalk.core.domain.model.User

interface AuthRepository {
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithPhone(phoneNumber: String, verificationId: String, code: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun updateProfile(name: String, photoUrl: String?): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
} 