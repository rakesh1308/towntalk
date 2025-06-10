package com.pixelsface.towntalk.features.profile.domain.use_case

import com.pixelsface.towntalk.core.domain.model.Post
import com.pixelsface.towntalk.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user's posts.
 */
class GetUserPostsUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    /**
     * Invoke the use case to get a user's posts.
     * @param userId The ID of the user
     * @return Flow of List of Post representing the user's posts
     */
    operator fun invoke(userId: String): Flow<List<Post>> {
        return repository.getUserPosts(userId)
    }
} 