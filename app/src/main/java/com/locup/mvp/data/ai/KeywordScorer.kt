package com.locup.mvp.data.ai

import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.repository.Classification

/**
 * Deterministic keyword scorer.
 *
 * This is the **fallback**, not the primary classifier. It exists so the demo
 * still classifies sensibly if the TFLite assets are missing or the model
 * fails to initialise on a given device — and it is always reported as
 * [InferenceEngine.KeywordFallback] so it can never be mistaken for the model.
 *
 * Pure and deterministic: identical text always yields identical scores, which
 * makes it directly unit-testable without a device.
 */
object KeywordScorer {

    private val SEEDS: Map<PostCategory, List<String>> = mapOf(
        PostCategory.Emergency to listOf(
            "fire", "accident", "flood", "rescue", "police", "ambulance",
            "waterlogging", "stuck", "danger", "emergency", "collapse", "injured",
        ),
        PostCategory.Traffic to listOf(
            "traffic", "jam", "roadblock", "closed", "congestion", "delay",
            "lane", "signal", "diversion", "blocked",
        ),
        PostCategory.Event to listOf(
            "festival", "concert", "yoga", "match", "celebration", "fair",
            "weekend", "workshop", "marathon", "meetup",
        ),
        PostCategory.Civic to listOf(
            "garbage", "streetlight", "pothole", "water", "power", "sewage",
            "drain", "smell", "sanitation", "waste",
        ),
        PostCategory.General to listOf(
            "hello", "hi", "anyone", "lost", "found", "thanks", "shoutout",
        ),
    )

    /**
     * Scores [text] by counting seed-word hits per category.
     *
     * Each category gets a `+0.001` smoothing term so the denominator is
     * never zero and no category is left with a NaN score.
     */
    fun classify(text: String): Classification {
        val tokens = text.lowercase()
            .split(NON_ALPHANUMERIC)
            .filter { it.isNotBlank() }
            .toSet()

        val raw = PostCategory.entries.associateWith { category ->
            (SEEDS[category].orEmpty().count { it in tokens } + 0.001f)
        }
        val total = raw.values.sum()
        val scores = raw.mapValues { (_, score) -> score / total }
        val top = raw.maxByOrNull { it.value }?.key ?: PostCategory.General

        return Classification(
            topCategory = top,
            confidence = scores.getValue(top),
            scores = scores,
            engine = InferenceEngine.KeywordFallback,
        )
    }

    private val NON_ALPHANUMERIC = Regex("\\W+")
}