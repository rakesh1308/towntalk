package com.pixelsface.towntalk.features.profile.domain.model

/**
 * Domain model representing an activity in the TownTalk application.
 */
data class Activity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val postId: String?
)

/**
 * Enum representing the type of activity in the TownTalk application.
 */
enum class ActivityType {
    POST_CREATED,
    POST_LIKED,
    POST_COMMENTED,
    ACHIEVEMENT_EARNED
} 