package com.locup.mvp.presentation.feed

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.repository.Classification
import com.locup.mvp.domain.repository.ClassifierStatus
import com.locup.mvp.domain.usecase.FeedFilter

/**
 * Immutable snapshot of everything the feed screen renders.
 *
 * A single data class (rather than several independent flows) is deliberate:
 * it makes it impossible for the UI to observe a half-updated state such as a
 * new filter with the previous post list.
 */
data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val filter: FeedFilter = FeedFilter.Here,

    /** True until the first Room emission has been rendered. */
    val isLoading: Boolean = true,

    /** Current user position, or null when unavailable or denied. */
    val location: GeoPoint? = null,

    /** Human-readable label for [location]'s cluster. */
    val areaLabel: String? = null,

    val isSubscribedToCurrentArea: Boolean = false,

    val classifierStatus: ClassifierStatus? = null,

    /** One-shot banner message (errors, confirmations). Cleared on consume. */
    val message: String? = null,

    /** True while a publish is in flight; guards duplicate submissions. */
    val isPublishing: Boolean = false,
) {
    val hasLocation: Boolean get() = location != null
}

/** Composer-specific state, kept apart so typing does not rebuild the feed. */
data class ComposerUiState(
    val text: String = "",
    val isPublishing: Boolean = false,
    val error: String? = null,
    /** Live classification preview for the current text. */
    val preview: Classification? = null,
    val isClassifying: Boolean = false,
) {
    val canPublish: Boolean
        get() = text.isNotBlank() && !isPublishing
}