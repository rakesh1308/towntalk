package com.pixelsface.towntalk.features.auth.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserPresenceUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(userId: String): Flow<Result<User?>> {
        return authRepository.getUserStream(userId)
    }
} 