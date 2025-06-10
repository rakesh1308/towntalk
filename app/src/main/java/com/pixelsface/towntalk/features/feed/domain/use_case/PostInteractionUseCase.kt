package com.pixelsface.towntalk.features.feed.domain.use_case

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import javax.inject.Inject

class PostInteractionUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend fun likePost(postId: String): Result<Unit> {
        if (postId.isBlank()) {
            return Result.error(ValidationException("Post ID cannot be empty"))
        }
        return postRepository.likePost(postId)
    }
    
    suspend fun unlikePost(postId: String): Result<Unit> {
        if (postId.isBlank()) {
            return Result.error(ValidationException("Post ID cannot be empty"))
        }
        return postRepository.unlikePost(postId)
    }
    
    suspend fun addComment(postId: String, content: String): Result<Unit> {
        if (postId.isBlank()) {
            return Result.error(ValidationException("Post ID cannot be empty"))
        }
        
        if (content.isBlank()) {
            return Result.error(ValidationException("Comment content cannot be empty"))
        }
        
        val comment = Comment(
            id = "", // Will be set by repository
            postId = postId,
            authorId = "", // Will be set by repository
            authorName = "", // Will be set by repository
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )
        
        return postRepository.addComment(postId, comment)
    }
    
    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        if (postId.isBlank() || commentId.isBlank()) {
            return Result.error(ValidationException("Post ID and Comment ID cannot be empty"))
        }
        
        return postRepository.deleteComment(postId, commentId)
    }
    
    suspend fun deletePost(postId: String): Result<Unit> {
        if (postId.isBlank()) {
            return Result.error(ValidationException("Post ID cannot be empty"))
        }
        
        return postRepository.deletePost(postId)
    }
} 