package com.pixelsface.towntalk.features.feed.domain.model

/**
 * Domain model representing a comment on a post in the TownTalk application.
 */
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val timestamp: Long
) 