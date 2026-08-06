package com.locup.mvp.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Local on-device classifier. Two execution paths:
 *
 * 1. If `assets/locup_text_classifier.tflite` exists, load it via the
 *    TensorFlow Lite Interpreter and run real inference on the device.
 * 2. If the asset is missing (the default state of the repo), fall back to a
 *    deterministic keyword scorer so the app still works end-to-end.
 *
 * The fallback is exposed via [isRealModel] so the UI can label results.
 *
 * Output contract: ordered list of (label, score) for the labels
 * [Emergency, Traffic, Event, Civic, General]. Score sums to ~1.0.
 */
class LocalClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var inputDim: Int = 0

    val isRealModel: Boolean get() = interpreter != null

    fun load() {
        val model = try {
            openAssetModel("locup_text_classifier.tflite")
        } catch (t: Throwable) {
            null
        }
        if (model != null) {
            interpreter = Interpreter(model)
            // Best-effort introspection of the input shape.
            inputDim = interpreter?.getInputTensor(0)?.shape()?.get(1) ?: 0
        }
    }

    private fun openAssetModel(path: String): MappedByteBuffer {
        val fd = context.assets.openFd(path)
        val input = FileInputStream(fd.fileDescriptor)
        return input.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    fun classify(text: String): Classification {
        val labels = labelOrder
        return if (interpreter != null) classifyWithTflite(text, labels)
        else KeywordFallback.score(text, labels)
    }

    private fun classifyWithTflite(text: String, labels: List<String>): Classification {
        val itp = interpreter ?: return KeywordFallback.score(text, labels)
        val feature = KeywordFallback.featureVector(text, inputDim)
        val input = Array(1) { feature }
        val output = Array(1) { FloatArray(labels.size) }
        return try {
            itp.run(input, output)
            val scores = output[0]
            Classification(labels, scores.toList(), modelName = "TensorFlow Lite")
        } catch (t: Throwable) {
            // Malformed model — graceful fallback rather than a crash.
            KeywordFallback.score(text, labels, modelName = "TensorFlow Lite (fallback)")
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    data class Classification(
        val labels: List<String>,
        val scores: List<Float>,
        val modelName: String
    ) {
        val bestLabel: String get() = labels.indices.maxByOrNull { scores[it] }?.let(labels::get) ?: "General"
        val bestScore: Float get() = scores.maxOrNull() ?: 0f
    }

    companion object {
        val labelOrder = listOf("Emergency", "Traffic", "Event", "Civic", "General")
    }
}
