package com.pixelsface.towntalk.features.auth.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateUserPresenceUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(isOnline: Boolean): Result<Unit> {
        // Get current user ID first. If not logged in, this operation is likely not needed or should error.
        val currentUserResult = authRepository.getCurrentUser() // Assuming this gives us the User object with ID
        return currentUserResult.fold(
            onSuccess = {
                val userId = it?.id
                if (userId != null) {
                    // For lastSeen, the repository handles setting FieldValue.serverTimestamp() when isOnline is false.
                    // The null for lastSeen in setOnlineStatus's signature is a default that repository implementation overrides.
                    authRepository.setOnlineStatus(userId, isOnline)
                } else {
                    // Wrap generic Exception in a TownTalkException subtype
                    Result.Error(UnknownException("User not logged in, cannot update presence."))
                }
            },
            onFailure = {
                // Ensure 'it' (the exception) is a TownTalkException or wrapped
                Result.Error(it)
            }
        )
    }
} 