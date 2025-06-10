package com.pixelsface.towntalk.features.profile.domain.model

import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.core.domain.model.Post

/**
 * Domain model representing a user's profile in the TownTalk application.
 * This model extends the core User model with profile-specific properties.
 */
data class ProfileUser(
    val user: User,
    val achievements: List<Achievement> = emptyList(),
    val recentActivity: List<Activity> = emptyList(),
    val posts: List<Post> = emptyList()
) 