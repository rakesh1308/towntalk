package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class UnlikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.unlikePost(postId)
    }
} 