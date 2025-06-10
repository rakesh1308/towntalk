package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting the current user's profile.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get the current user's profile.
     * @return Flow of ProfileUser object
     */
    operator fun invoke(): Flow<ProfileUser> {
        return repository.getCurrentUser()
    }
} 