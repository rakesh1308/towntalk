package com.pixelsface.towntalk.features.feed.domain.use_case

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        category: String,
        city: String,
        mediaUrls: List<String> = emptyList()
    ): Result<String> {
        if (title.isBlank()) {
            return Result.error(ValidationException("Title cannot be empty"))
        }
        
        if (content.isBlank()) {
            return Result.error(ValidationException("Content cannot be empty"))
        }
        
        val post = Post(
            id = "", // Will be set by repository
            title = title.trim(),
            content = content.trim(),
            authorId = "", // Will be set by repository
            authorName = "", // Will be set by repository
            timestamp = System.currentTimeMillis(),
            likes = emptyList<String>(),
            comments = 0,
            mediaUrls = mediaUrls,
            category = category,
            city = city
        )
        
        return postRepository.createPost(post)
    }
} 