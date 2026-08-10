package com.locup.mvp.classifier

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * On-device post classifier using the plain TFLite runtime — no MediaPipe
 * Tasks, no Task Library, no bundled tokenizer metadata. Tokenization is
 * done here in Kotlin via [BertTokenizer], mirroring the exact same logic
 * used when the model was trained (see [tools/train_locup_classifier.py](file:///C:/RAKESH/WORK/Development/LocUp/towntalk/tools/train_locup_classifier.py)):
 *   - lowercase + Unicode NFKD + accent strip
 *   - split on whitespace + punctuation
 *   - greedy longest-match WordPiece against `vocab.txt`
 *   - `[CLS]` / `[SEP]` wrapping, pad/truncate to `MAX_SEQ_LEN`.
 *
 * Expects four assets in `app/src/main/assets/`:
 *   - `locup_text_classifier.tflite` — the MobileBERT fine-tune, INT8 quantised
 *   - `vocab.txt`                    — one token per line, line number = index
 *   - `labels.txt`                   — class names, one per line, in model output order
 *   - `max_seq_len.txt`              — single integer, e.g. "64"
 *
 * The model is interpreted with three inputs (input_ids, attention_mask,
 * token_type_ids) and one output (a softmax over the 5 classes).
 *
 * If any asset is missing, [isModelAvailable] returns false and callers
 * should fall back to the existing deterministic keyword scorer.
 */
class TfliteTextClassifier(private val context: Context) {

    companion object {
        private const val TAG = "TfliteTextClassifier"
        private const val MODEL_ASSET = "locup_text_classifier.tflite"
        private const val VOCAB_ASSET = "vocab.txt"
        private const val LABELS_ASSET = "labels.txt"
        private const val SEQ_LEN_ASSET = "max_seq_len.txt"
    }

    data class LabelScore(val label: String, val score: Float)

    private data class LoadedModel(
        val interpreter: Interpreter,
        val vocab: Map<String, Int>,
        val labels: List<String>,
        val tokenizer: BertTokenizer,
        val inputIdsName: String,
        val attentionMaskName: String,
        val tokenTypeIdsName: String,
        val outputName: String,
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

    /** Pad / sequence length used by the model (for diagnostics). */
    @Suppress("unused")
    val maxSeqLen: Int get() = model?.let { m ->
        m.interpreter.getInputTensor(0).shape()?.get(1) ?: 0
    } ?: 0

    private fun tryLoad(): LoadedModel? {
        return try {
            Log.i(TAG, "Loading TFLite model from assets/$MODEL_ASSET")
            val maxSeqLen = loadMaxSeqLen()
            Log.i(TAG, "MAX_SEQ_LEN = $maxSeqLen")
            val vocab = loadVocab()
            Log.i(TAG, "Vocab loaded: ${vocab.size} tokens")
            val labels = loadLabels()
            Log.i(TAG, "Labels loaded: $labels")
            val tokenizer = BertTokenizer(vocab, maxSeqLen)
            val interpreter = Interpreter(loadModelFile())
            val inputIdsName = interpreter.getInputTensor(0).name()
            val attentionMaskName = interpreter.getInputTensor(1).name()
            val tokenTypeIdsName = interpreter.getInputTensor(2).name()
            val outputName = interpreter.getOutputTensor(0).name()
            Log.i(
                TAG,
                "Interpreter loaded — inputs: " +
                        "[$inputIdsName, $attentionMaskName, $tokenTypeIdsName], " +
                        "output: $outputName, " +
                        "input shape: ${interpreter.getInputTensor(0).shape()}, " +
                        "output shape: ${interpreter.getOutputTensor(0).shape()}",
            )
            LoadedModel(
                interpreter = interpreter,
                vocab = vocab,
                labels = labels,
                tokenizer = tokenizer,
                inputIdsName = inputIdsName,
                attentionMaskName = attentionMaskName,
                tokenTypeIdsName = tokenTypeIdsName,
                outputName = outputName,
            )
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
        val map = HashMap<String, Int>()
        context.assets.open(VOCAB_ASSET).bufferedReader().use { reader ->
            var line = reader.readLine()
            var idx = 0
            while (line != null) {
                val token = line.trim()
                if (token.isNotEmpty()) {
                    map[token] = idx
                }
                idx += 1
                line = reader.readLine()
            }
        }
        return map
    }

    private fun loadLabels(): List<String> {
        return context.assets.open(LABELS_ASSET).bufferedReader()
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    private fun loadMaxSeqLen(): Int {
        val raw = context.assets.open(SEQ_LEN_ASSET).bufferedReader().use { it.readText() }
        val n = raw.trim().toIntOrNull()
            ?: throw IllegalStateException("Could not parse MAX_SEQ_LEN from $SEQ_LEN_ASSET: '$raw'")
        if (n <= 0 || n > 512) {
            throw IllegalStateException("MAX_SEQ_LEN out of range: $n")
        }
        return n
    }

    /**
     * Returns the full probability distribution across all labels,
     * sorted by score descending. Empty list if the model isn't available.
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

        val tokens = loaded.tokenizer.encode(text)
        val n = tokens.inputIds.size
        Log.d(TAG, "classify(\"${text.take(40)}\") seqLen=$n")

        // MobileBERT expects three inputs of shape [1, MAX_SEQ_LEN]:
        //   input_ids, attention_mask, token_type_ids.
        val inputIds = Array(1) { tokens.inputIds }
        val attentionMask = Array(1) { tokens.attentionMask }
        val tokenTypeIds = Array(1) { IntArray(n) } // all zeros (single sentence)
        val output = Array(1) { FloatArray(loaded.labels.size) }

        try {
            loaded.interpreter.run(
                mapOf(
                    loaded.inputIdsName to inputIds,
                    loaded.attentionMaskName to attentionMask,
                    loaded.tokenTypeIdsName to tokenTypeIds,
                ),
                mapOf(loaded.outputName to output),
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Interpreter.run failed: ${t.message}", t)
            return emptyList()
        }

        val raw = output[0]
        val sumCheck = raw.sum()
        Log.d(
            TAG,
            "  raw scores = ${raw.joinToString(",") { "%.3f".format(it) }} (sum=%.3f)".format(sumCheck),
        )

        return loaded.labels.indices
            .map { i -> LabelScore(loaded.labels[i], output[0][i]) }
            .sortedByDescending { it.score }
    }

    /** Convenience: just the winning label + confidence. */
    fun classifyTopLabel(text: String): LabelScore? = classify(text).firstOrNull()

    fun close() {
        model?.interpreter?.close()
        Log.i(TAG, "Interpreter closed")
    }
}
