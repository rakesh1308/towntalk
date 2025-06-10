package com.pixelsface.towntalk.features.feed.domain.model

/**
 * Domain model representing a post in the TownTalk application.
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
    val comments: Int,
    val mediaUrls: List<String>,
    val category: String,
    val city: String
) 