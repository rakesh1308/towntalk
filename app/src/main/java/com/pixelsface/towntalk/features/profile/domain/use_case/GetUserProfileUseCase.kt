package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a user's profile.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get a user's profile.
     * @param userId The ID of the user
     * @return Flow of ProfileUser representing the user's profile
     */
    operator fun invoke(userId: String): Flow<ProfileUser> {
        return repository.getUserProfile(userId)
    }
}