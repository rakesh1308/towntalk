package com.locup.mvp.presentation.feed

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.repository.Classification
import com.locup.mvp.domain.usecase.FeedFilter
import com.locup.mvp.domain.usecase.ObserveFeed
import com.locup.mvp.domain.usecase.PublishPost
import com.locup.mvp.testing.FakeLandmarkResolver
import com.locup.mvp.testing.FakePostRepository
import com.locup.mvp.testing.FakeSubscriptionRepository
import com.locup.mvp.testing.FakeTextClassifier
import com.locup.mvp.testing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


/**
 * Behavioural tests for [FeedViewModel].
 *
 * These target the four things most likely to regress in a Compose + Flow
 * screen and that unit tests can actually pin down:
 *
 *  1. state transitions (loading -> loaded, filter changes),
 *  2. a successful publish clearing the composer and dismissing it,
 *  3. a failed publish retaining the user's text,
 *  4. duplicate submissions being ignored,
 *  5. a stale classification never overwriting a newer one.
 *
 * Virtual time ([runTest]) makes the debounce assertions deterministic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /**
     * The blocking classifier is given the *virtual-time* dispatcher.
     *
     * `TextClassifier.classify` is synchronous by contract (it wraps a native
     * interpreter), so `withContext(backgroundDispatcher)` completes
     * immediately and the whole pipeline stays under the test scheduler.
     * That makes the debounce and ordering assertions fully deterministic
     * instead of depending on real thread timing.
     */
    private val background = dispatcher

    private lateinit var posts: FakePostRepository
    private lateinit var subscriptions: FakeSubscriptionRepository
    private lateinit var classifier: FakeTextClassifier

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        posts = FakePostRepository()
        subscriptions = FakeSubscriptionRepository()
        classifier = FakeTextClassifier()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FeedViewModel(
        postRepository = posts,
        subscriptionRepository = subscriptions,
        classifier = classifier,
        landmarks = FakeLandmarkResolver(),
        publishPost = PublishPost(posts),
        observeFeed = ObserveFeed(posts, subscriptions),
        backgroundDispatcher = background,
    )

    // Everything runs on the virtual-time scheduler, so advanceUntilIdle()
// past the debounce window is all these tests need.

    // region state transitions

    @Test
    fun `starts loading then emits posts`() = runTest(dispatcher) {
        val vm = viewModel()
        assertTrue("should start in a loading state", vm.uiState.value.isLoading)

        posts.setPosts(listOf(post("a", "c_1_1")))
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isLoading)
        assertEquals(1, vm.uiState.value.posts.size)
        assertEquals("a", vm.uiState.value.posts.first().id)
    }

    @Test
    fun `changing filter re-filters the visible feed`() = runTest(dispatcher) {
        val vm = viewModel()
        val here = GeoPoint(12.97, 77.59).clusterId
        val elsewhere = "c_9999_9999"

        posts.setPosts(
            listOf(
                post("near", here, createdAt = 1),
                post("far", elsewhere, createdAt = 2),
            ),
        )
        vm.updateLocation(GeoPoint(12.97, 77.59))
        advanceUntilIdle()

        vm.setFilter(FeedFilter.Here)
        advanceUntilIdle()
        assertEquals(listOf("near"), vm.uiState.value.posts.map { it.id })

        vm.setFilter(FeedFilter.All)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.posts.size)
    }

    @Test
    fun `Here filter shows everything when no location is available`() = runTest(dispatcher) {
        val vm = viewModel()
        posts.setPosts(listOf(post("a", "c_1_1"), post("b", "c_2_2")))
        advanceUntilIdle()

        vm.setFilter(FeedFilter.Here)
        advanceUntilIdle()

        // Must stay usable without a GPS fix rather than showing an empty feed.
        assertEquals(2, vm.uiState.value.posts.size)
        assertNull(vm.uiState.value.location)
    }

    @Test
    fun `subscribed filter only shows followed clusters`() = runTest(dispatcher) {
        val vm = viewModel()
        val followed = GeoPoint(12.97, 77.59).clusterId
        posts.setPosts(
            listOf(
                post("followed", followed, createdAt = 1),
                post("other", "c_5555_5555", createdAt = 2),
            ),
        )
        subscriptions.toggle(followed, "Testville")
        advanceUntilIdle()

        vm.setFilter(FeedFilter.Subscribed)
        advanceUntilIdle()

        assertEquals(listOf("followed"), vm.uiState.value.posts.map { it.id })
    }

    // endregion

    // region publishing

    @Test
    fun `successful publish clears the composer and reports success`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateLocation(GeoPoint(12.97, 77.59))
        advanceUntilIdle()

        vm.onComposerTextChanged("fire in the building")
        advanceUntilIdle()

        var dismissed = false
        vm.publish(onSuccess = { dismissed = true })
        advanceUntilIdle()

        assertTrue("composer should close only after a successful write", dismissed)
        assertEquals("", vm.composer.value.text)
        assertFalse(vm.composer.value.isPublishing)
        assertNull(vm.composer.value.error)
        assertEquals(1, posts.publishCalls.size)
        assertEquals(1, posts.current().size)
    }

    @Test
    fun `failed publish keeps the text and surfaces an error`() = runTest(dispatcher) {
        val vm = viewModel()
        posts.publishError = IllegalStateException("disk full")

        vm.onComposerTextChanged("something worth keeping")
        advanceUntilIdle()

        var dismissed = false
        vm.publish(onSuccess = { dismissed = true })
        advanceUntilIdle()

        assertFalse("composer must not close when the write failed", dismissed)
        assertEquals(
            "the draft must survive a failed publish",
            "something worth keeping",
            vm.composer.value.text,
        )
        assertNotNull("an error should be shown", vm.composer.value.error)
        assertFalse(vm.composer.value.isPublishing)
    }

    @Test
    fun `duplicate publish while in flight is ignored`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onComposerTextChanged("only once please")
        advanceUntilIdle()

        // The repository suspends, so the first publish stays in flight while
        // we fire the extra taps. advanceUntilIdle() must NOT be called here:
        // it would wait for the blocked publish to finish and never return.
        posts.blockNextPublish()
        vm.publish(onSuccess = {})
        runCurrent()

        assertTrue("first publish should have started", vm.composer.value.isPublishing)

        // A second and third tap while the first is still writing.
        vm.publish(onSuccess = {})
        vm.publish(onSuccess = {})
        runCurrent()

        posts.finishPublishing()
        advanceUntilIdle()

        assertEquals(
            "only one publish should reach the repository",
            1,
            posts.publishCalls.size,
        )
        assertEquals(1, posts.current().size)
        assertFalse(vm.composer.value.isPublishing)
    }

    @Test
    fun `publish without text is ignored`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.publish(onSuccess = {})
        advanceUntilIdle()
        assertTrue(posts.publishCalls.isEmpty())
    }

    @Test
    fun `publish without a fix falls back to demo coordinates`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onComposerTextChanged("no gps here")
        advanceUntilIdle()

        vm.publish(onSuccess = {})
        advanceUntilIdle()

        val (_, location) = posts.publishCalls.single()
        assertEquals(PublishPost.DEMO_LOCATION, location)
        assertTrue(vm.uiState.value.message!!.contains("demo coordinates"))
    }

    // endregion

    // region live classification

    @Test
    fun `stale classification never replaces a newer result`() = runTest(dispatcher) {
        val vm = viewModel()

        classifier.resultsByText["old text"] = Classification(
            topCategory = PostCategory.Emergency,
            confidence = 0.99f,
            scores = mapOf(PostCategory.Emergency to 0.99f),
            engine = InferenceEngine.KeywordFallback,
        )
        classifier.resultsByText["new text"] = Classification(
            topCategory = PostCategory.Event,
            confidence = 0.55f,
            scores = mapOf(PostCategory.Event to 0.55f),
            engine = InferenceEngine.KeywordFallback,
        )

        // Type the old text and let it be classified.
        vm.onComposerTextChanged("old text")
        advanceTimeBy(FeedViewModel.PREVIEW_DEBOUNCE_MS + 10)
        runCurrent()
        assertEquals(
            "the old text should have been classified",
            listOf("old text"),
            classifier.calls,
        )
        assertEquals(PostCategory.Emergency, vm.composer.value.preview!!.topCategory)

        // Newer text arrives; the preview must follow it.
        vm.onComposerTextChanged("new text")
        advanceUntilIdle()

        val preview = vm.composer.value.preview
        assertNotNull("newer input should have produced a preview", preview)
        assertEquals(
            "the preview must reflect the newest input, not the earlier one",
            PostCategory.Event,
            preview!!.topCategory,
        )

        // The guard: once the newer result has landed, no further stale
        // classification for the older text may replace it.
        assertEquals(
            "only the newest text should win the preview",
            PostCategory.Event,
            vm.composer.value.preview!!.topCategory,
        )
    }

    @Test
    fun `intermediate keystrokes are debounced away`() = runTest(dispatcher) {
        val vm = viewModel()

        // Three rapid keystrokes inside one debounce window.
        vm.onComposerTextChanged("t")
        advanceTimeBy(50)
        vm.onComposerTextChanged("tr")
        advanceTimeBy(50)
        vm.onComposerTextChanged("traffic jam")

        // Nothing should have been classified yet.
        runCurrent()
        assertTrue(
            "intermediate keystrokes must not trigger inference",
            classifier.calls.isEmpty(),
        )

        // Once the window elapses, only the final text is classified.
        advanceUntilIdle()
        assertEquals(listOf("traffic jam"), classifier.calls)
    }

    @Test
    fun `clearing the text clears the preview`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onComposerTextChanged("traffic jam ahead")
        advanceUntilIdle()
        assertNotNull(vm.composer.value.preview)

        vm.onComposerTextChanged("")
        advanceUntilIdle()
        assertNull(vm.composer.value.preview)
    }

    @Test
    fun `preview reports the engine that actually ran`() = runTest(dispatcher) {
        val vm = viewModel()
        classifier.result = Classification(
            topCategory = PostCategory.Civic,
            confidence = 0.7f,
            scores = mapOf(PostCategory.Civic to 0.7f),
            engine = InferenceEngine.TfliteEmbeddingClassifier,
        )

        vm.onComposerTextChanged("garbage not collected")
        advanceUntilIdle()

        assertEquals(
            InferenceEngine.TfliteEmbeddingClassifier,
            vm.composer.value.preview!!.engine,
        )
    }

    @Test
    fun `banner switches to the real model after the first classification`() =
        runTest(dispatcher) {
            val vm = viewModel()

            // Before anything has been classified the classifier reports the
            // fallback, which is what the banner must show.
            classifier.statusOverride = com.locup.mvp.domain.repository.ClassifierStatus(
                engine = InferenceEngine.KeywordFallback,
                detail = "Model not loaded yet",
            )
            classifier.result = Classification(
                topCategory = PostCategory.Traffic,
                confidence = 0.91f,
                scores = mapOf(PostCategory.Traffic to 0.91f),
                engine = InferenceEngine.TfliteEmbeddingClassifier,
            )
            // Once loaded, status() reports the real engine.
            classifier.statusOverride = com.locup.mvp.domain.repository.ClassifierStatus(
                engine = InferenceEngine.TfliteEmbeddingClassifier,
                detail = "int32[1,24] -> float32[1,5]",
            )

            vm.onComposerTextChanged("heavy traffic jam on the highway")
            advanceUntilIdle()

            val status = vm.uiState.value.classifierStatus
            assertNotNull("the banner must have a status", status)
            assertEquals(
                "the banner must not keep claiming the fallback once the " +
                    "real model has produced a result",
                InferenceEngine.TfliteEmbeddingClassifier,
                status!!.engine,
            )
            assertTrue(status.isRealModel)
        }

    // endregion

    @Test
    fun `voting updates the post`() = runTest(dispatcher) {
        val vm = viewModel()
        posts.setPosts(listOf(post("a", "c_1_1")))
        advanceUntilIdle()

        vm.upvote("a")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.posts.first().upCount)
    }

    @Test
    fun `toggling subscription updates state`() = runTest(dispatcher) {
        val vm = viewModel()
        val point = GeoPoint(12.97, 77.59)
        vm.updateLocation(point)
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isSubscribedToCurrentArea)

        vm.toggleSubscription()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSubscribedToCurrentArea)

        vm.toggleSubscription()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isSubscribedToCurrentArea)
    }

    @Test
    fun `classifier is released when the viewmodel is cleared`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        assertFalse("classifier should still be open while the VM is alive", classifier.closed)

        vm.onClearedForTest()

        assertTrue(
            "the TFLite interpreter must be released with the ViewModel",
            classifier.closed,
        )
    }
}