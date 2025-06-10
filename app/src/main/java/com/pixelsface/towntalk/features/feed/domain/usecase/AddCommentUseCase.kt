package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject
import com.pixelsface.towntalk.core.common.error.Result

/**
 * Use case for adding a comment to a post.
 */
class AddCommentUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    /**
     * Invoke the use case.
     * @param postId The ID of the post to comment on.
     * @param comment The comment to add.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(postId: String, comment: Comment): Result<Unit> {
        return postRepository.addComment(postId, comment)
    }
} 