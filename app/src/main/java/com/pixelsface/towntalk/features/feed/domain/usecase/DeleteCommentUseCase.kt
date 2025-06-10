package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import com.pixelsface.towntalk.core.common.error.Result
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, commentId: String): Result<Unit> {
        return postRepository.deleteComment(postId, commentId)
    }
} 