package com.pixelsface.towntalk.core.domain.model

/**
 * Core domain model representing a user in the TownTalk application.
 * This model is used across multiple features.
 */
data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val phoneNumber: String?,
    val photoUrl: String?,
    val bio: String?,
    val city: String,
    val joinDate: Long,
    val postCount: Int = 0,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val profileCompletionPercentage: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) 