package com.locup.mvp.domain.model

/**
 * Identifies *what actually classified a post*.
 *
 * This exists because the project previously reported "TensorFlow Lite
 * (MobileBERT)" from a hard-coded string while the real behaviour was a
 * keyword scorer. The UI must derive its banner text from this value, so a
 * fallback can never be presented as a neural network.
 *
 * [TfliteEmbeddingClassifier] is the accurate name for the bundled model: an
 * `Embedding(GatherV2) -> Dense(ReLU) -> Dense(softmax)` MLP over a 24-token
 * id sequence. See `tools/inspect_model.py` and README for the verified
 * signature.
 */
enum class InferenceEngine(val displayName: String, val isNeural: Boolean) {
    /** The bundled TFLite word-embedding MLP. */
    TfliteEmbeddingClassifier("TFLite word-embedding MLP", true),

    /** Deterministic keyword scorer, used when the model cannot run. */
    KeywordFallback("Keyword fallback", false);

    companion object {
        /**
         * Resolve an engine from the model's own self-description.
         *
         * Used by the data layer so the label is derived from what was really
         * loaded, never assumed.
         */
        fun fromModelName(name: String?): InferenceEngine =
            if (name.isNullOrBlank()) KeywordFallback else TfliteEmbeddingClassifier
    }
}