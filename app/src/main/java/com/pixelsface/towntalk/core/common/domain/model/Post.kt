package com.pixelsface.towntalk.core.common.domain.model

data class Post(
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
) 