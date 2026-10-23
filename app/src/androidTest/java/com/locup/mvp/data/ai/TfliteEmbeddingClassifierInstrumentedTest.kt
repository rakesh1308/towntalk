package com.locup.mvp.data.ai

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.repository.Classification
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Instrumented tests for the **real** bundled TFLite model.
 *
 * These run on a device/emulator because they exercise the actual
 * `org.tensorflow.lite.Interpreter` against the APK's assets — exactly the
 * path the app uses at runtime.
 *
 * The concurrency test is the regression test for a real bug: `infer()` used
 * to be documented as "caller must hold [TfliteEmbeddingClassifier.lock]" while
 * its only caller never did. Because `Interpreter` is explicitly not
 * thread-safe, two concurrent `classify()` calls could enter
 * `Interpreter.runSignature` on the same interpreter simultaneously and
 * corrupt each other's output. The fix makes `infer()` acquire the lock
 * itself, across the native call *and* the read of the output buffer.
 *
 * Every test skips gracefully (via [assumeTrue]) when the model assets are
 * not bundled, so a stripped-down APK does not produce spurious failures.
 */
@RunWith(AndroidJUnit4::class)
class TfliteEmbeddingClassifierInstrumentedTest {

    private lateinit var classifier: TfliteEmbeddingClassifier

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        assumeTrue(
            "Model assets are not bundled in this APK; skipping instrumented test.",
            hasModelAssets(ctx),
        )
        classifier = TfliteEmbeddingClassifier(ctx)
    }

    @After
    fun tearDown() {
        if (::classifier.isInitialized) {
            classifier.close()
        }
    }

    // region tests

    @Test
    fun modelLoadsAndReportsTheRealEngine() {
        // The engine is loaded lazily, so classify once before asking for the
        // status — otherwise `status()` legitimately reports "not loaded yet".
        classifier.classify(TRAFFIC_TEXT)

        val status = classifier.status()

        assertEquals(
            "Expected the real TFLite engine, got ${status.engine} (${status.detail})",
            InferenceEngine.TfliteEmbeddingClassifier,
            status.engine,
        )
        assertTrue(
            "Expected the model summary to advertise the int32[1,24] input, was: ${status.detail}",
            status.detail.contains("int32[1,24]"),
        )
        assertTrue("status.isRealModel must be true for a real model", status.isRealModel)
    }

    /**
     * REGRESSION TEST for the interpreter data race.
     *
     * Forty classifications are submitted to an eight-thread pool so that
     * several of them are inside `infer()` at the same time. Without the
     * lock this reliably produced corrupted output (wrong/garbage scores, or
     * a native crash); with the lock every result must be a clean, normalised
     * five-category distribution.
     */
    @Test
    fun concurrentClassifyCallsAreSerialisedSafely() {
        val poolSize = 8
        val taskCount = 40

        val executor = Executors.newFixedThreadPool(poolSize)
        // `ready` releases the pool only once every worker thread is up.
        val ready = CountDownLatch(poolSize)
        // `start` is the gate: nobody calls classify() before it opens.
        val start = CountDownLatch(1)
        // `wave` re-synchronises every round of `poolSize` tasks so each wave
        // calls classify() at the same instant, maximising the overlap.
        val wave = CyclicBarrier(poolSize)

        val results = CopyOnWriteArrayList<Classification>()
        val errors = CopyOnWriteArrayList<Throwable>()

        val futures = (0 until taskCount).map {
            executor.submit {
                try {
                    ready.countDown()
                    start.await(30, TimeUnit.SECONDS)
                    wave.await(30, TimeUnit.SECONDS)
                    results.add(classifier.classify(TRAFFIC_TEXT))
                } catch (t: Throwable) {
                    errors.add(t)
                }
            }
        }

        try {
            assertTrue(
                "Workers did not reach the start gate in time",
                ready.await(30, TimeUnit.SECONDS),
            )
            start.countDown()
            futures.forEach { it.get(60, TimeUnit.SECONDS) }
        } finally {
            // Release any worker still blocked on a latch, even on failure.
            start.countDown()
            executor.shutdownNow()
        }

        assertTrue(
            "Concurrent inference threw: ${errors.map { it.toString() }}",
            errors.isEmpty(),
        )
        assertEquals("Expected $taskCount classifications", taskCount, results.size)

        results.forEach { result ->
            assertEquals(
                "Expected exactly 5 category scores, got ${result.scores.keys}",
                5,
                result.scores.size,
            )
            assertEquals(
                "Expected every category to be scored",
                PostCategory.entries.toSet(),
                result.scores.keys,
            )
            val total = result.scores.values.sum()
            assertTrue("Scores must sum to ~1.0 but summed to $total", total in 0.95f..1.05f)
            assertNotNull("Top category must not be null", result.topCategory)
            assertTrue(
                "Unexpected top category ${result.topCategory}",
                result.topCategory in PostCategory.entries,
            )
            assertEquals(
                "Concurrent calls must still use the real model",
                InferenceEngine.TfliteEmbeddingClassifier,
                result.engine,
            )
        }
    }

    /**
     * `close()` releases the interpreter; a racing `classify()` must fall back
     * to keyword scoring instead of calling into a closed interpreter.
     */
    @Test
    fun classifyAfterCloseDoesNotCrash() {
        classifier.close()

        val result = classifier.classify(TRAFFIC_TEXT)

        assertNotNull("classify() after close() must still return a result", result)
        assertEquals("Expected the keyword fallback's 5 scores", 5, result.scores.size)
        assertTrue(
            "Top category must be one of the 5 categories, was ${result.topCategory}",
            result.topCategory in PostCategory.entries,
        )
        val total = result.scores.values.sum()
        assertTrue("Fallback scores must sum to ~1.0 but summed to $total", total in 0.95f..1.05f)
    }

    /**
     * The bundled model must actually discriminate: clearly different inputs
     * must produce clearly different top categories.
     */
    @Test
    fun realModelDiscriminatesCategories() {
        val traffic = classifier.classify(TRAFFIC_TEXT)
        assertEquals(
            "'$TRAFFIC_TEXT' should classify as Traffic, got ${traffic.topCategory} " +
                "(${traffic.scores})",
            PostCategory.Traffic,
            traffic.topCategory,
        )
        assertEquals(InferenceEngine.TfliteEmbeddingClassifier, traffic.engine)

        val emergency = classifier.classify(EMERGENCY_TEXT)
        assertEquals(
            "'$EMERGENCY_TEXT' should classify as Emergency, got ${emergency.topCategory} " +
                "(${emergency.scores})",
            PostCategory.Emergency,
            emergency.topCategory,
        )
        assertEquals(InferenceEngine.TfliteEmbeddingClassifier, emergency.engine)
    }

    // endregion

    /**
     * True when all three model assets are present in the APK.
     *
     * `assets.list("")` is the documented way to enumerate the assets root;
     * some `AssetManager` implementations return nothing useful for the
     * empty path, so a direct open-probe is used as a fallback before the
     * test is skipped.
     */
    private fun hasModelAssets(ctx: Context): Boolean {
        val listed = runCatching { ctx.assets.list("")?.toSet().orEmpty() }
            .getOrElse { emptySet() }
        if (REQUIRED_ASSETS.all { it in listed }) return true

        return REQUIRED_ASSETS.all { name ->
            runCatching { ctx.assets.open(name).close() }.isSuccess
        }
    }

    private companion object {
        const val TRAFFIC_TEXT = "heavy traffic jam on the highway"
        const val EMERGENCY_TEXT = "fire accident rescue needed ambulance"

        val REQUIRED_ASSETS = listOf(
            ClassifierAssets.MODEL_ASSET,
            ClassifierAssets.VOCAB_ASSET,
            ClassifierAssets.LABELS_ASSET,
        )
    }
}
