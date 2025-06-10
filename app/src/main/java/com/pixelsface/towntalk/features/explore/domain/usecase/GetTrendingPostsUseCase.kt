package com.pixelsface.towntalk.features.explore.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class GetTrendingPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(limit: Int = 10): Result<List<Post>> {
        // In a real scenario, "trending" logic could be complex (e.g., based on recent likes, comments, views)
        // This will call a new method in PostRepository
        return postRepository.getTrendingPosts(limit)
    }
} 