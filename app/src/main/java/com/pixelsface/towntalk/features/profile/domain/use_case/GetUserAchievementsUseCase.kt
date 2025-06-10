package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.features.profile.domain.model.Achievement
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user's achievements.
 */
class GetUserAchievementsUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get a user's achievements.
     * @param userId The ID of the user
     * @return Flow of List of Achievement representing the user's achievements
     */
    operator fun invoke(userId: String): Flow<List<Achievement>> {
        return repository.getUserAchievements(userId)
    }
} 