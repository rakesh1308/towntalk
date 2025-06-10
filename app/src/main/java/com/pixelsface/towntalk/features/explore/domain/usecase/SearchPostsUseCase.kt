package com.pixelsface.towntalk.features.explore.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class SearchPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(query: String): Result<List<Post>> {
        if (query.isBlank()) {
            return Result.Success(emptyList())
        }
        // In a real scenario, you might want to add more sophisticated query processing
        // or validation here.
        return postRepository.searchPosts(query)
    }
} 