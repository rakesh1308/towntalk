package com.pixelsface.towntalk.core.domain.model

/**
 * Core domain model representing a comment on a post in the TownTalk application.
 * This model is used across multiple features.
 */
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 