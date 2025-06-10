package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for deleting a user's account.
 */
class DeleteUserAccountUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to delete a user's account.
     * @param userId The ID of the user
     * @return Flow of Boolean indicating whether the deletion was successful
     */
    suspend operator fun invoke(userId: String): Flow<Boolean> {
        return repository.deleteUserAccount(userId)
    }
} 