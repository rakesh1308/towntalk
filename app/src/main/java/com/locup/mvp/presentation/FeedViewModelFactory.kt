package com.locup.mvp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.locup.mvp.di.AppContainer
import com.locup.mvp.presentation.feed.FeedViewModel

/**
 * Builds [FeedViewModel] from the [AppContainer].
 *
 * Manual factory rather than Hilt or `viewModels { }` DSL magic, so the exact
 * dependency list for the screen is visible in one place.
 */
class FeedViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return FeedViewModel(
            postRepository = container.postRepository,
            subscriptionRepository = container.subscriptionRepository,
            classifier = container.classifier,
            landmarks = container.landmarks,
            publishPost = container.publishPost,
            observeFeed = container.observeFeed,
            backgroundDispatcher = container.dispatcher,
        ) as T
    }
}