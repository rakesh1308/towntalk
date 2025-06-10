package com.pixelsface.towntalk.features.feed.data.remote.dto

data class PostDto(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val timestamp: Long = 0L,
    val likes: List<String> = emptyList(),
    val comments: Int = 0,
    val mediaUrls: List<String> = emptyList(),
    val category: String = "",
    val city: String = ""
) 