package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import javax.inject.Inject

/**
 * Use case for getting comments for a specific post.
 */
class GetCommentsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case.
     * @param postId The ID of the post to get comments for.
     * @return A Result containing a list of comments or an error.
     */
    suspend operator fun invoke(postId: String): Result<List<Comment>> {
        return postRepository.getPostComments(postId)
    }
} 