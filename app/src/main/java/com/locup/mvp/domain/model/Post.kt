package com.locup.mvp.domain.model

/**
 * A post as the presentation layer consumes it.
 *
 * This is the *domain* shape: no Room annotations, no Android types. The data
 * layer maps `PostEntity` -> `Post` so that the rest of the app never touches
 * persistence concerns directly.
 */
data class Post(
    val id: String,
    val author: String,
    val text: String,
    val category: PostCategory,
    /** Model confidence in 0..1, captured at publish time. */
    val modelConfidence: Float,
    val location: GeoPoint,
    val clusterId: String,
    val areaLabel: String,
    val createdAt: Long,
    val upCount: Int,
    val downCount: Int,
    /** Which inference engine actually produced [category]. */
    val classifiedBy: InferenceEngine,
) {
    /**
     * Crowd-sourced trust signal: net upvotes. Kept separate from
     * [modelConfidence] on purpose — a model can be confidently wrong, and
     * the demo shows both numbers so the distinction is visible.
     */
    val communityScore: Int get() = upCount - downCount

    val isDemoData: Boolean get() = id.startsWith(DEMO_ID_PREFIX)

    companion object {
        /** Seeded sample rows are prefixed so the UI can label them as demo. */
        const val DEMO_ID_PREFIX = "demo_"
    }
}

/** A user's subscription to a geographic cluster. */
data class Subscription(
    val clusterId: String,
    val label: String,
    val subscribedAt: Long,
)