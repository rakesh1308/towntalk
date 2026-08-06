package com.locup.mvp.classifier

import android.content.Context
import android.util.Log
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

/**
 * On-device post classifier using the plain TFLite runtime — no MediaPipe
 * Tasks, no Task Library, no bundled tokenizer metadata. Tokenization is
 * done here in Kotlin, mirroring the exact same logic used when the model
 * was trained (see the Colab notebook, section 3/4): lowercase, split on
 * non-alphanumeric characters, map to vocab indices, pad/truncate to a
 * fixed length.
 *
 * Expects three assets in app/src/main/assets/:
 *   - locup_text_classifier.tflite
 *   - vocab.json       (word -> index map, produced by the notebook)
 *   - labels.txt        (one label per line, in model output order)
 *
 * If any asset is missing, [isModelAvailable] returns false and callers
 * should fall back to the existing deterministic keyword scorer already
 * in the repo — this class deliberately does NOT throw in that case.
 */
class TfliteTextClassifier(private val context: Context) {

    companion object {
        private const val TAG = "TfliteTextClassifier"
        private const val MODEL_ASSET = "locup_text_classifier.tflite"
        private const val VOCAB_ASSET = "vocab.json"
        private const val LABELS_ASSET = "labels.txt"
        private const val SEQ_LEN = 24 // must match the notebook's SEQ_LEN
        private const val OOV_INDEX = 1
        private const val PAD_INDEX = 0

        // Same tokenizer as the Python side: lowercase, split on runs of
        // letters/digits/apostrophes, drop everything else.
        private val TOKEN_REGEX = Regex("[a-zA-Z0-9']+")
    }

    data class LabelScore(val label: String, val score: Float)

    private data class LoadedModel(
        val interpreter: Interpreter,
        val vocab: Map<String, Int>,
        val labels: List<String>,
    )

    private val model: LoadedModel? by lazy { tryLoad() }

    val isModelAvailable: Boolean
        get() = model != null

    /** True only if a real .tflite model was loaded (not just the fallback). */
    val isRealModelLoaded: Boolean
        get() = isModelAvailable

    /** Number of tokens in the loaded vocab (for diagnostics). */
    val vocabSize: Int get() = model?.vocab?.size ?: 0

    /** Number of output labels (for diagnostics). */
    val labelCount: Int get() = model?.labels?.size ?: 0

    private fun tryLoad(): LoadedModel? {
        return try {
            Log.i(TAG, "Loading TFLite model from assets/$MODEL_ASSET")
            val vocab = loadVocab()
            Log.i(TAG, "Vocab loaded: ${vocab.size} tokens")
            val labels = loadLabels()
            Log.i(TAG, "Labels loaded: $labels")
            val interpreter = Interpreter(loadModelFile())
            Log.i(TAG, "Interpreter loaded — input shape: ${interpreter.getInputTensor(0).shape()}, " +
                    "output shape: ${interpreter.getOutputTensor(0).shape()}")
            LoadedModel(interpreter, vocab, labels)
        } catch (e: Exception) {
            Log.w(TAG, "TFLite model not loaded — falling back: ${e.message}")
            null
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_ASSET)
        FileInputStream(assetFileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            Log.d(TAG, "Mapping $MODEL_ASSET bytes [$startOffset..${startOffset + declaredLength})")
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    private fun loadVocab(): Map<String, Int> {
        val json = context.assets.open(VOCAB_ASSET).bufferedReader().use { it.readText() }
        val obj = JSONObject(json)
        val map = mutableMapOf<String, Int>()
        obj.keys().forEach { key -> map[key] = obj.getInt(key) }
        return map
    }

    private fun loadLabels(): List<String> {
        return context.assets.open(LABELS_ASSET).bufferedReader()
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Tokenisation matches the Python notebook exactly: lowercase, then
     * match runs of letters/digits/apostrophes. Everything else is dropped.
     */
    fun tokenize(text: String): List<String> {
        return TOKEN_REGEX.findAll(text.lowercase()).map { it.value }.toList()
    }

    /**
     * Encode a string into the model's input tensor. Padding / truncation
     * to [SEQ_LEN] is applied. Out-of-vocab tokens map to [OOV_INDEX].
     */
    fun encode(text: String, vocab: Map<String, Int>): IntArray {
        val tokens = tokenize(text).take(SEQ_LEN)
        val ids = IntArray(SEQ_LEN) { PAD_INDEX }
        tokens.forEachIndexed { i, token ->
            ids[i] = vocab[token] ?: OOV_INDEX
        }
        return ids
    }

    /**
     * Returns the full probability distribution across all labels,
     * sorted by score descending. Empty list if the model isn't available —
     * callers should check [isModelAvailable] first and use the keyword
     * fallback scorer in that case.
     */
    fun classify(text: String): List<LabelScore> {
        val loaded = model ?: run {
            Log.w(TAG, "classify() called but model is not loaded")
            return emptyList()
        }
        if (text.isBlank()) {
            Log.d(TAG, "classify() called with blank text, returning empty")
            return emptyList()
        }

        val inputIds = encode(text, loaded.vocab)
        val preview = inputIds.take(8).joinToString(",")
        Log.d(TAG, "classify(\"${text.take(40)}\") tokens=${tokenize(text)} ids=[$preview,...]")

        // Interpreter expects shape [1, SEQ_LEN]
        val input = Array(1) { inputIds }
        val output = Array(1) { FloatArray(loaded.labels.size) }

        try {
            loaded.interpreter.run(input, output)
        } catch (t: Throwable) {
            Log.e(TAG, "Interpreter.run failed: ${t.message}", t)
            return emptyList()
        }

        val raw = output[0]
        val sumCheck = raw.sum()
        Log.d(TAG, "  raw scores = ${raw.joinToString(",") { "%.3f".format(it) }} (sum=%.3f)".format(sumCheck))

        return loaded.labels.indices
            .map { i -> LabelScore(loaded.labels[i], output[0][i]) }
            .sortedByDescending { it.score }
    }

    /** Convenience: just the winning label + confidence. */
    fun classifyTopLabel(text: String): LabelScore? {
        return classify(text).firstOrNull()
    }

    fun close() {
        model?.interpreter?.close()
        Log.i(TAG, "Interpreter closed")
    }
}
