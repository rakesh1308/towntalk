package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user's profile by ID.
 */
class GetUserByIdUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get a user's profile by ID.
     * @param userId The ID of the user to get
     * @return Flow of ProfileUser object
     */
    operator fun invoke(userId: String): Flow<ProfileUser> {
        return repository.getUserById(userId)
    }
} 