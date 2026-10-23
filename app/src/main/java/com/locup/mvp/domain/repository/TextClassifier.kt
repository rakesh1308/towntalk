package com.locup.mvp.domain.repository

import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory

/** Probability distribution over the five categories. */
data class Classification(
    val topCategory: PostCategory,
    /** Confidence of [topCategory], normalised to 0..1. */
    val confidence: Float,
    /** Full distribution, ordered by the model's own output order. */
    val scores: Map<PostCategory, Float>,
    /** The engine that actually produced this result. */
    val engine: InferenceEngine,
) {
    companion object {
        /** Used for blank input, before any engine has run. */
        val Empty: Classification = Classification(
            topCategory = PostCategory.General,
            confidence = 0f,
            scores = emptyMap(),
            engine = InferenceEngine.KeywordFallback,
        )
    }
}

/**
 * Why a real model is not available. Kept distinct from inference failures so
 * diagnostics can tell a developer whether to go fix an asset, a
 * configuration, or the runtime.
 */
enum class ClassifierUnavailableReason {
    /** A required asset was absent from the APK. */
    MISSING_ASSET,

    /** Assets present but the interpreter could not be built/initialised. */
    INITIALIZATION_FAILED,

    /** Model loaded, but a call to it threw. */
    INFERENCE_FAILED,
}

/** Describes the state of the on-device classifier for diagnostics/UI. */
data class ClassifierStatus(
    val engine: InferenceEngine,
    val detail: String,
    val unavailableReason: ClassifierUnavailableReason? = null,
) {
    val isRealModel: Boolean get() = engine.isNeural
}

/**
 * Port for on-device text classification.
 *
 * Implementations live in `data`. The contract is deliberately narrow so the
 * ViewModel never touches TFLite types and can be tested with a fake.
 */
interface TextClassifier {

    /** Snapshot of what is loaded right now. Cheap; safe to call from the UI. */
    fun status(): ClassifierStatus

    /**
     * Classify [text]. Blocking — callers must invoke this off the main
     * thread (the implementation loads a model and runs native inference).
     */
    fun classify(text: String): Classification

    /** Releases native resources. Idempotent. */
    fun close()
}