package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result

/**
 * Use case for getting posts for a specific city.
 */
class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case.
     * @param city The city to get posts for.
     * @return A Result containing a list of posts or an error.
     */
    suspend operator fun invoke(city: String): Result<List<Post>> {
        return postRepository.getPosts(city)
    }
} 