package com.pixelsface.towntalk.features.feed.data.model

import com.google.firebase.Timestamp
import com.pixelsface.towntalk.features.feed.domain.model.Post

/**
 * Data Transfer Object for Post entity in Firebase.
 */
data class PostDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val likes: List<String> = emptyList(),
    val likeCount: Int = 0,
    val comments: Int = 0,
    val mediaUrls: List<String> = emptyList(),
    val category: String = "",
    val city: String = "",
    val searchKeywords: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Convert DTO to domain model.
     */
    fun toDomain(): Post {
        return Post(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            authorName = authorName,
            timestamp = timestamp.seconds * 1000, // Convert to milliseconds
            likes = likes,
            likeCount = likeCount,
            comments = comments,
            mediaUrls = mediaUrls,
            category = category,
            city = city
        )
    }
    
    companion object {
        /**
         * Create a DTO from a domain model.
         */
        fun fromDomain(post: Post): PostDto {
            val keywords = mutableSetOf<String>()
            keywords.addAll(post.title.lowercase().split(" ").filter { it.length > 2 })
            keywords.addAll(post.content.lowercase().split(" ").filter { it.length > 2 })
            keywords.add(post.category.lowercase())
            keywords.addAll(post.authorName.lowercase().split(" ").filter { it.length > 1 })
            keywords.add(post.city.lowercase())

            // Add full phrases as well, if desired for specific fields, e.g., title
            if (post.title.isNotBlank()) keywords.add(post.title.lowercase())

            return PostDto(
                id = post.id,
                title = post.title,
                content = post.content,
                authorId = post.authorId,
                authorName = post.authorName,
                timestamp = Timestamp(post.timestamp / 1000, 0), // Convert from milliseconds
                likes = post.likes,
                likeCount = post.likeCount,
                comments = post.comments,
                mediaUrls = post.mediaUrls,
                category = post.category,
                city = post.city,
                searchKeywords = keywords.toList().distinct()
            )
        }
    }
} 