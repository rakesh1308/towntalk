package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result

/**
 * Use case for getting a specific post by ID.
 */
class GetPostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case.
     * @param id The ID of the post to get.
     * @return A Result containing the post or an error.
     */
    suspend operator fun invoke(id: String): Result<Post> {
        return postRepository.getPost(id)
    }
} 