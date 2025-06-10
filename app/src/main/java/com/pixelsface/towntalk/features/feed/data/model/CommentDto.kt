package com.pixelsface.towntalk.features.feed.data.model

import com.google.firebase.Timestamp
import com.pixelsface.towntalk.features.feed.domain.model.Comment

/**
 * Data Transfer Object for Comment entity in Firebase.
 */
data class CommentDto(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Convert DTO to domain model.
     */
    fun toDomain(): Comment {
        return Comment(
            id = id,
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            content = content,
            timestamp = timestamp.seconds * 1000 // Convert to milliseconds
        )
    }
    
    companion object {
        /**
         * Create a DTO from a domain model.
         */
        fun fromDomain(comment: Comment): CommentDto {
            return CommentDto(
                id = comment.id,
                postId = comment.postId,
                authorId = comment.authorId,
                authorName = comment.authorName,
                content = comment.content,
                timestamp = Timestamp(comment.timestamp / 1000, 0) // Convert from milliseconds
            )
        }
    }
} 