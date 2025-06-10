package com.pixelsface.towntalk.features.feed.domain.usecase

import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import com.pixelsface.towntalk.core.common.error.Result
import javax.inject.Inject

class GetPostLikesUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<List<String>> {
        return postRepository.getPostLikes(postId)
    }
} 