package com.locup.mvp.di

import android.content.Context
import com.locup.mvp.data.TownTalkDatabase
import com.locup.mvp.data.ai.TfliteEmbeddingClassifier
import com.locup.mvp.data.repository.RoomPostRepository
import com.locup.mvp.data.repository.RoomSubscriptionRepository
import com.locup.mvp.domain.repository.LandmarkResolver
import com.locup.mvp.domain.repository.PostRepository
import com.locup.mvp.domain.repository.SubscriptionRepository
import com.locup.mvp.domain.repository.TableLandmarkResolver
import com.locup.mvp.domain.repository.TextClassifier
import com.locup.mvp.domain.usecase.ObserveFeed
import com.locup.mvp.domain.usecase.PublishPost
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Manual dependency container.
 *
 * Deliberately hand-rolled rather than Hilt: the graph is small and entirely
 * constructor-injected, so a DI framework would add build-time cost and
 * indirection without removing any real coupling. Everything is `by lazy`, so
 * nothing is constructed until first used and the classifier's native
 * resources are only touched when the UI actually asks for a classification.
 */
class AppContainer(
    context: Context,
    /** Injected so tests can substitute a deterministic dispatcher. */
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val appContext: Context = context.applicationContext

    private val database: TownTalkDatabase by lazy { TownTalkDatabase.get(appContext) }

    val landmarks: LandmarkResolver by lazy { TableLandmarkResolver() }

    val classifier: TextClassifier by lazy { TfliteEmbeddingClassifier(appContext) }

    val postRepository: PostRepository by lazy {
        RoomPostRepository(
            postDao = database.postDao(),
            classifier = classifier,
            landmarks = landmarks,
            classificationDispatcher = backgroundDispatcher,
        )
    }

    val subscriptionRepository: SubscriptionRepository by lazy {
        RoomSubscriptionRepository(database.subscriptionDao())
    }

    val publishPost: PublishPost by lazy { PublishPost(postRepository) }

    val observeFeed: ObserveFeed by lazy { ObserveFeed(postRepository, subscriptionRepository) }

    val dispatcher: CoroutineDispatcher get() = backgroundDispatcher

    /** Releases process-wide resources held by this container. */
    fun close() {
        classifier.close()
    }
}