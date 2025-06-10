package com.pixelsface.towntalk.features.feed.domain.use_case

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        if (postId.isBlank()) {
            return Result.error(ValidationException("Post ID cannot be empty"))
        }
        
        return postRepository.deletePost(
            postId
        )
    }
} 