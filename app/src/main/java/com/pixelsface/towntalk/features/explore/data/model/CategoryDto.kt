package com.pixelsface.towntalk.features.explore.data.model

/**
 * Data Transfer Object for a Category, representing how it's stored in Firestore.
 */
data class CategoryDto(
    val id: String = "", // Firestore document ID will often be mapped here
    val name: String = "",
    val iconName: String? = null,
    val sortOrder: Int? = null // Optional: for ordering categories if needed
) {
    // No-argument constructor for Firestore deserialization
    constructor() : this("", "", null, null)

    fun toDomain(): com.pixelsface.towntalk.features.explore.domain.model.Category {
        return com.pixelsface.towntalk.features.explore.domain.model.Category(
            id = id,
            name = name,
            iconName = iconName
        )
    }
} 