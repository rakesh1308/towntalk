package com.pixelsface.towntalk.features.explore.domain.model

/**
 * Represents a category for browsing or filtering content in the Explore section.
 */
data class Category(
    val id: String,
    val name: String,
    val iconName: String? = null // e.g., name of a Material Icon or a drawable resource
) 