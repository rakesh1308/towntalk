package com.pixelsface.towntalk.features.auth.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<User> {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            return Result.error(UnknownException("Email, password, and name cannot be empty"))
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.error(UnknownException("Invalid email format"))
        }
        
        if (password.length < 6) {
            return Result.error(UnknownException("Password must be at least 6 characters"))
        }
        
        return authRepository.registerWithEmail(email, password, name)
    }
} 