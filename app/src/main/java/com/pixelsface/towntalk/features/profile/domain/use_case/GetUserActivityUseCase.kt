package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.Activity
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user's activity.
 */
class GetUserActivityUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get a user's activity.
     * @param userId The ID of the user
     * @return Flow of List of Activity representing the user's activity
     */
    operator fun invoke(userId: String): Flow<List<Activity>> {
        return repository.getUserActivity(userId)
    }
} 