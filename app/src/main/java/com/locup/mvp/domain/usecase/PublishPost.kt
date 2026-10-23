package com.locup.mvp.domain.usecase

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.repository.PostRepository

/**
 * Publishes a post, applying the business rules around *where* it lands.
 *
 * Exists as a use case because "fall back to demo coordinates when there is no
 * GPS fix" is a product decision, not a ViewModel detail — and because it
 * makes the requirement that the app works without location permission
 * explicit and testable in one place.
 */
class PublishPost(
    private val posts: PostRepository,
) {
    suspend operator fun invoke(
        text: String,
        location: GeoPoint?,
        author: String = PostRepository.DEFAULT_AUTHOR,
    ): PublishOutcome {
        val trimmed = text.trim()
        require(trimmed.isNotEmpty()) { "Cannot publish an empty post" }

        val resolved = location ?: DEMO_LOCATION
        posts.publish(trimmed, resolved, author)
        return PublishOutcome(
            usedFallbackLocation = location == null,
            location = resolved,
        )
    }

    data class PublishOutcome(
        val usedFallbackLocation: Boolean,
        val location: GeoPoint,
    )

    companion object {
        /**
         * Central Bengaluru, matching the seeded demo posts so a post made
         * without a GPS fix still lands in a populated cluster.
         */
        val DEMO_LOCATION = GeoPoint(12.97, 77.59)
    }
}