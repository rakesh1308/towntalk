package com.pixelsface.towntalk.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pixelsface.towntalk.core.common.domain.model.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val timestamp: Long,
    val likes: List<String>,
    val comments: Int,
    val mediaUrls: List<String>,
    val category: String,
    val city: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Post {
        return Post(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            authorName = authorName,
            timestamp = timestamp,
            likes = likes,
            comments = comments,
            mediaUrls = mediaUrls,
            category = category,
            city = city,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        fun fromDomain(post: Post): PostEntity {
            return PostEntity(
                id = post.id,
                title = post.title,
                content = post.content,
                authorId = post.authorId,
                authorName = post.authorName,
                timestamp = post.timestamp,
                likes = post.likes,
                comments = post.comments,
                mediaUrls = post.mediaUrls,
                category = post.category,
                city = post.city,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
    }
} 