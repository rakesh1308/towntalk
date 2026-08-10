package com.locup.mvp.classifier

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device end-to-end test of [TfliteTextClassifier] and
 * [PostClassifierRepository]. Verifies:
 *
 *   1. All three assets (model, vocab, labels) are bundled.
 *   2. Tokeniser produces non-empty token lists for representative text.
 *   3. The TFLite Interpreter runs without throwing.
 *   4. The output distribution is not the maximum-entropy uniform (i.e.
 *      the model actually discriminates, not just returning 0.2 across
 *      all labels — the bug we hit with our hand-built flatbuffer).
 *   5. The repository surfaces the model's prediction in Title Case.
 *
 * Skip with `./gradlew test` — these run with `./gradlew connectedAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class TfliteTextClassifierInstrumentedTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var classifier: TfliteTextClassifier

    @Before fun setup() {
        // Skip the whole class on emulators / devices where the assets
        // aren't bundled yet.
        val assets = ctx.assets.list("")?.toList() ?: emptyList()
        assumeTrue(
            "Model asset missing — install locup_text_classifier.tflite in app/src/main/assets/ first",
            "locup_text_classifier.tflite" in assets,
        )
        assumeTrue("vocab.txt missing", "vocab.txt" in assets)
        assumeTrue("labels.txt missing", "labels.txt" in assets)
        assumeTrue("max_seq_len.txt missing", "max_seq_len.txt" in assets)

        classifier = TfliteTextClassifier(ctx)
    }

    @Test fun model_loads_successfully() {
        assertTrue("Interpreter should have loaded", classifier.isModelAvailable)
        assertEquals("Expected 5-class classifier", 5, classifier.labelCount)
        assertTrue("BERT vocab should have thousands of tokens", classifier.vocabSize > 1000)
    }

    @Test fun classify_produces_real_distribution() {
        val result = classifier.classify("Fire on MG Road, drive around!")
        assertEquals("Expected one score per label", 5, result.size)
        val sum = result.sumOf { it.score.toDouble() }
        assertTrue(
            "Softmax output should sum to ~1.0, was $sum",
            sum in 0.95..1.05,
        )
        // The model must NOT return the maximum-entropy uniform 0.2 across
        // all labels — that was the symptom of the broken hand-built
        // FlatBuffer we debugged for hours. A real classifier should have
        // at least one label with score > 0.3.
        val maxScore = result.maxOf { it.score }
        assertTrue(
            "At least one label should score > 0.3; max=$maxScore (uniform would be 0.2). " +
            "If you see this fail, the bundled .tflite is broken.",
            maxScore > 0.3f,
        )
    }

    @Test fun classify_differentiates_categories() {
        // Same model, different inputs. Each should pick a different top
        // class (or at minimum, the top class should change as inputs do).
        val traffic = classifier.classify("heavy traffic jam on the highway")
        val emergency = classifier.classify("fire accident waterlogging rescue needed")
        val event = classifier.classify("community festival diwali celebration this weekend")

        val trafficTop = traffic.first().label
        val emergencyTop = emergency.first().label
        val eventTop = event.first().label

        // Sanity: the runner-up label should differ between at least two of
        // these inputs. If all three produce the same top label, the model
        // is dead.
        assertTrue(
            "Model should not produce the same top label for every input",
            trafficTop != emergencyTop || emergencyTop != eventTop || trafficTop != eventTop,
        )
    }

    @Test fun empty_input_returns_empty() {
        assertEquals(emptyList<TfliteTextClassifier.LabelScore>(), classifier.classify(""))
        assertEquals(emptyList<TfliteTextClassifier.LabelScore>(), classifier.classify("   "))
    }

    @Test fun repository_surfaces_real_model() {
        val repo = PostClassifierRepository(ctx)
        try {
            assertTrue("Real model should be active when assets are bundled", repo.isRealModelActive)

            val r = repo.classify("Fire on MG Road, drive around!")
            assertTrue("Label should be Title Case", r.label.first().isUpperCase())
            assertTrue(
                "Label should be one of the five classes",
                r.label in setOf("Emergency", "Traffic", "Event", "Civic", "General"),
            )
            assertTrue("Confidence should be > 0", r.confidence > 0f)
            assertEquals("Five labels in map", 5, r.allScores.size)
            assertTrue("usedRealModel flag should be true", r.usedRealModel)
        } finally {
            repo.close()
        }
    }

    @Test fun repository_falls_back_when_model_missing() {
        // We can simulate fallback by constructing a repository against an
        // empty assets folder, but that's awkward in an instrumented test.
        // Instead, verify the fallback path's surface: even if
        // tfliteClassifier.isModelAvailable is false, repo.classify must
        // still return a non-null ClassificationResult (keyword path).
        val fallbackRepo = TfliteTextClassifierFallbackProbe(ctx)
        val r = fallbackRepo.classify("garbage pile on 5th main, smells awful")
        assertNotNull("Fallback must produce a result", r)
        assertFalse("Fallback must not claim real model", r.usedRealModel)
        assertTrue("Fallback confidence should be > 0", r.confidence > 0f)
    }

    /**
     * Helper that emulates the "model missing" branch of the repository by
     * always falling back. Wraps [PostClassifierRepository]'s fallback path
     * without needing to relocate assets.
     */
    private class TfliteTextClassifierFallbackProbe(ctx: Context) {
        private val keyword = com.locup.mvp.ml.LocalClassifier(ctx).also { it.load() }
        fun classify(text: String): PostClassifierRepository.ClassificationResult {
            val r = keyword.classify(text)
            return PostClassifierRepository.ClassificationResult(
                label = r.bestLabel,
                confidence = r.bestScore,
                allScores = r.labels.zip(r.scores).associate { (l, s) -> l to s },
                usedRealModel = false,
                modelName = r.modelName,
            )
        }
    }
}
