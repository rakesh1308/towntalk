package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        // Optionally, filter out the current user from the list if needed for UI
        // For now, returning all users including the current one.
        return authRepository.getAllUsers()
    }
} 