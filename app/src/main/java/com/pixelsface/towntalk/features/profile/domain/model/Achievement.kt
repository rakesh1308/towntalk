package com.pixelsface.towntalk.features.profile.domain.model

/**
 * Domain model representing an achievement in the TownTalk application.
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val progress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean,
    val earnedDate: Long?
) 