package com.pixelsface.towntalk.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixelsface.towntalk.core.common.domain.model.Comment

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Comment {
        return Comment(
            id = id,
            postId = postId,
            authorId = authorId,
            authorName = authorName,
            content = content,
            timestamp = timestamp,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(comment: Comment): CommentEntity {
            return CommentEntity(
                id = comment.id,
                postId = comment.postId,
                authorId = comment.authorId,
                authorName = comment.authorName,
                content = comment.content,
                timestamp = comment.timestamp,
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt
            )
        }
    }
} 