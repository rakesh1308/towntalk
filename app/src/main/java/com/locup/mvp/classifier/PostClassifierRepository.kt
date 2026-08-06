package com.locup.mvp.classifier

import android.content.Context
import android.util.Log
import com.locup.mvp.ml.KeywordFallback
import com.locup.mvp.ml.LocalClassifier

/**
 * Single entry point the rest of the app calls to classify a post.
 *
 * Order of preference:
 *   1. Real word-embedding `.tflite` model in assets/ (locup_text_classifier.tflite
 *      + vocab.json + labels.txt).
 *   2. Deterministic keyword scorer (KeywordFallback) — byte-identical
 *      scoring to the trained model on this project's keyword taxonomy.
 *
 * All outputs are normalised to Title Case ("Emergency", "Traffic", ...)
 * to match the rest of the app's label vocabulary.
 */
class PostClassifierRepository(context: Context) {

    companion object {
        private const val TAG = "PostClassifierRepo"
    }

    private val tfliteClassifier = TfliteTextClassifier(context)
    private val keywordClassifier = LocalClassifier(context).also { it.load() }

    val isRealModelActive: Boolean
        get() = tfliteClassifier.isModelAvailable

    init {
        Log.i(TAG, "Initialized — realModel=${tfliteClassifier.isRealModelLoaded} " +
                "vocabSize=${tfliteClassifier.vocabSize} labelCount=${tfliteClassifier.labelCount}")
    }

    /**
     * Full probability distribution across all five labels plus the
     * winning label. Returned even when the model is unavailable, so
     * the UI can render the keyword fallback path the same way.
     */
    data class ClassificationResult(
        val label: String,
        val confidence: Float,
        val allScores: Map<String, Float>,
        val usedRealModel: Boolean,
        val modelName: String,
    )

    fun classify(text: String): ClassificationResult {
        if (text.isBlank()) {
            return ClassificationResult(
                label = "General",
                confidence = 0f,
                allScores = emptyMap(),
                usedRealModel = false,
                modelName = "empty input",
            )
        }

        if (tfliteClassifier.isModelAvailable) {
            val scores = tfliteClassifier.classify(text)
            val top = scores.firstOrNull()
            val allScores = scores.associate { it.label.toTitleCase() to it.score }
            val result = ClassificationResult(
                label = top?.label?.toTitleCase() ?: "General",
                confidence = top?.score ?: 0f,
                allScores = allScores,
                usedRealModel = true,
                modelName = "TensorFlow Lite (word-embedding)",
            )
            Log.d(TAG, "classify(\"${text.take(40)}\") -> ${result.label} " +
                    "(${result.confidence}) usedRealModel=true")
            return result
        }

        // Fallback path.
        val r = keywordClassifier.classify(text)
        val all = r.labels.zip(r.scores).associate { (l, s) -> l to s }
        val result = ClassificationResult(
            label = r.bestLabel,
            confidence = r.bestScore,
            allScores = all,
            usedRealModel = false,
            modelName = r.modelName,
        )
        Log.d(TAG, "classify(\"${text.take(40)}\") -> ${result.label} " +
                "(${result.confidence}) usedRealModel=false [fallback: ${r.modelName}]")
        return result
    }

    fun close() {
        tfliteClassifier.close()
        keywordClassifier.close()
    }

    private fun String.toTitleCase(): String =
        trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
