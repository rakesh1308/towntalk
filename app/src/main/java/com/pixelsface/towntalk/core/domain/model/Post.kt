package com.pixelsface.towntalk.core.domain.model

/**
 * Core domain model representing a post in the TownTalk application.
 * This model is used across multiple features.
 */
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val authorName: String,
    val timestamp: Long,
    val likes: List<String>,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val mediaUrls: List<String>,
    val category: String,
    val city: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 