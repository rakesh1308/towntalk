package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result

/**
 * Use case for creating a new post.
 */
class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case.
     * @param post The post to create.
     * @return A Result containing the ID of the created post or an error.
     */
    suspend operator fun invoke(post: Post): Result<String> {
        return postRepository.createPost(post)
    }
} 