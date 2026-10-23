package com.locup.mvp.domain.usecase

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.model.Subscription
import com.locup.mvp.domain.repository.PostRepository
import com.locup.mvp.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** The feed scopes the user can switch between. */
enum class FeedFilter { Here, Subscribed, All }

/**
 * Combines posts, subscriptions and the user's position into the visible feed.
 *
 * This is a genuine use case rather than a pass-through wrapper: the filter
 * rule ("subscribed" means "posts in any cluster I follow") is business logic,
 * and putting it here keeps it out of both the ViewModel and the Composable.
 */
class ObserveFeed(
    private val posts: PostRepository,
    private val subscriptions: SubscriptionRepository,
) {
    operator fun invoke(
        filter: Flow<FeedFilter>,
        currentLocation: Flow<GeoPoint?>,
    ): Flow<List<Post>> =
        combine(posts.observePosts(), subscriptions.observeSubscriptions(), filter, currentLocation) {
            allPosts, subs, selectedFilter, location ->
            val visible = when (selectedFilter) {
                FeedFilter.All -> allPosts

                FeedFilter.Here -> {
                    // With no fix yet, show everything rather than an empty
                    // screen — the app must stay usable without permission.
                    val cluster = location?.clusterId
                    if (cluster == null) allPosts else allPosts.filter { it.clusterId == cluster }
                }

                FeedFilter.Subscribed -> {
                    val ids = subs.map(Subscription::clusterId).toSet()
                    if (ids.isEmpty()) emptyList() else allPosts.filter { it.clusterId in ids }
                }
            }
            visible.sortedByDescending { it.createdAt }
        }
}