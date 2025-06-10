package com.pixelsface.towntalk.features.auth.domain.usecase

import android.content.Intent
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.error(UnknownException("Email and password cannot be empty"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.error(UnknownException("Invalid email format"))
        }
        
        return authRepository.signInWithEmail(email, password)
    }
    
    suspend fun handleGoogleSignIn(data: Intent?): Result<User> {
        if (data == null) {
            return Result.error(UnknownException("Google sign-in data is null"))
        }
        
        return authRepository.handleGoogleSignInResult(data)
    }
    
    suspend fun sendPhoneVerificationCode(phoneNumber: String): Result<String> {
        if (phoneNumber.isBlank()) {
            return Result.error(UnknownException("Phone number cannot be empty"))
        }
        
        return authRepository.sendPhoneVerificationCode(phoneNumber)
    }
} 