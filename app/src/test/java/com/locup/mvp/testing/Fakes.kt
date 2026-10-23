package com.locup.mvp.testing

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.model.Subscription
import com.locup.mvp.domain.repository.Classification
import com.locup.mvp.domain.repository.ClassifierStatus
import com.locup.mvp.domain.repository.PostRepository
import com.locup.mvp.domain.repository.SubscriptionRepository
import com.locup.mvp.domain.repository.TableLandmarkResolver
import com.locup.mvp.domain.repository.TextClassifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [PostRepository] for ViewModel tests.
 *
 * A fake rather than a mock: it implements the real contract (emits on change,
 * persists what it is given) so tests exercise behaviour instead of
 * call-count assertions.
 */
class FakePostRepository : PostRepository {

    private val posts = MutableStateFlow<List<Post>>(emptyList())

    /** Set to make [publish] throw, to exercise the failure path. */
    var publishError: Throwable? = null

    /** Records publish calls so duplicate submission can be detected. */
    val publishCalls = mutableListOf<Pair<String, GeoPoint>>()

    /**
     * When true, [publish] suspends until [finishPublishing] is called.
     *
     * The gate is created up front and reset by [blockNextPublish] so that a
     * test can arm the block *after* the ViewModel has already started the
     * publish, without racing the coroutine.
     */
    var blockPublish = false
        private set

    private var publishGate = kotlinx.coroutines.CompletableDeferred<Unit>()

    /** Arms the gate for subsequent publishes. */
    fun blockNextPublish() {
        publishGate = kotlinx.coroutines.CompletableDeferred()
        blockPublish = true
    }

    /** Lets a blocked publish complete. */
    fun finishPublishing() {
        blockPublish = false
        publishGate.complete(Unit)
    }

    override fun observePosts(): Flow<List<Post>> = posts

    override suspend fun publish(text: String, location: GeoPoint, author: String) {
        publishCalls += text to location
        publishError?.let { throw it }
        if (blockPublish) {
            // Suspend rather than block a thread, so the virtual-time
            // scheduler stays free to run the test's own assertions.
            publishGate.await()
        }
        posts.value = posts.value + Post(
            id = "post_${publishCalls.size}",
            author = author,
            text = text,
            category = PostCategory.General,
            modelConfidence = 0.5f,
            location = location,
            clusterId = location.clusterId,
            areaLabel = "Test area",
            createdAt = System.currentTimeMillis(),
            upCount = 0,
            downCount = 0,
            classifiedBy = InferenceEngine.KeywordFallback,
        )
    }

    override suspend fun upvote(postId: String) {
        posts.value = posts.value.map {
            if (it.id == postId) it.copy(upCount = it.upCount + 1) else it
        }
    }

    override suspend fun downvote(postId: String) {
        posts.value = posts.value.map {
            if (it.id == postId) it.copy(downCount = it.downCount + 1) else it
        }
    }

    override suspend fun seedDemoDataIfEmpty() = Unit

    fun setPosts(value: List<Post>) {
        posts.value = value
    }

    fun current(): List<Post> = posts.value
}

/** In-memory [SubscriptionRepository]. */
class FakeSubscriptionRepository : SubscriptionRepository {

    private val subs = MutableStateFlow<List<Subscription>>(emptyList())
    private val subscribed = mutableSetOf<String>()

    override fun observeSubscriptions(): Flow<List<Subscription>> = subs

    override suspend fun isSubscribed(clusterId: String): Boolean = clusterId in subscribed

    override suspend fun toggle(clusterId: String, label: String) {
        if (!subscribed.add(clusterId)) {
            subscribed.remove(clusterId)
            subs.value = subs.value.filterNot { it.clusterId == clusterId }
        } else {
            subs.value = subs.value + Subscription(clusterId, label, 0L)
        }
    }
}

/**
 * Scriptable [TextClassifier].
 *
 * [TextClassifier.classify] is synchronous by contract (it mirrors the real
 * interpreter, which the ViewModel calls from a background dispatcher).
 *
 * This fake deliberately **never blocks a thread**. An earlier version used a
 * CountDownLatch to hold a call "in flight", which deadlocked the test JVM:
 * under a virtual-time scheduler the background dispatcher *is* the thread
 * running the assertions, so blocking it froze the whole scheduler.
 *
 * Ordering is instead made directly observable — [calls] records the exact
 * sequence of texts classified, which is what the stale-result guard needs.
 */
class FakeTextClassifier : TextClassifier {

    var result: Classification = Classification(
        topCategory = PostCategory.Traffic,
        confidence = 0.9f,
        scores = mapOf(
            PostCategory.Traffic to 0.9f,
            PostCategory.General to 0.1f,
        ),
        engine = InferenceEngine.KeywordFallback,
    )

    var statusOverride: ClassifierStatus? = null

    /** Per-text overrides so old vs new input can produce different labels. */
    val resultsByText = mutableMapOf<String, Classification>()

    /** Every text passed to [classify], in call order. */
    val calls: MutableList<String> = mutableListOf()

    var closed = false

    override fun status(): ClassifierStatus = statusOverride ?: ClassifierStatus(
        engine = InferenceEngine.KeywordFallback,
        detail = "fake",
    )

    override fun classify(text: String): Classification {
        calls += text
        return resultsByText[text] ?: result
    }

    override fun close() {
        closed = true
    }
}

/** Fixed resolver so tests never depend on the real landmark table. */
class FakeLandmarkResolver : com.locup.mvp.domain.repository.LandmarkResolver {
    override fun labelFor(latitude: Double, longitude: Double): String = "Testville"
}

/** Real resolver, exposed for reuse where behaviour under test is generic. */
val realLandmarkResolver = TableLandmarkResolver()

/** Builds a post for feed-filter assertions. */
fun post(
    id: String,
    clusterId: String,
    createdAt: Long = 0L,
    category: PostCategory = PostCategory.General,
): Post = Post(
    id = id,
    author = "Tester",
    text = "post $id",
    category = category,
    modelConfidence = 0.5f,
    location = GeoPoint(12.97, 77.59),
    clusterId = clusterId,
    areaLabel = "Testville",
    createdAt = createdAt,
    upCount = 0,
    downCount = 0,
    classifiedBy = InferenceEngine.KeywordFallback,
)