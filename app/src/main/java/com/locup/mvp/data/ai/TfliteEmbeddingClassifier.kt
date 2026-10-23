package com.locup.mvp.data.ai

import android.content.Context
import android.util.Log
import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory
import com.locup.mvp.domain.repository.Classification
import com.locup.mvp.domain.repository.ClassifierStatus
import com.locup.mvp.domain.repository.ClassifierUnavailableReason
import com.locup.mvp.domain.repository.TextClassifier
import org.tensorflow.lite.Interpreter
import java.util.concurrent.locks.ReentrantLock

/**
 * On-device classifier backed by the bundled TFLite word-embedding model.
 *
 * ## What the model actually is
 *
 * Introspected with `ai-edge-litert` (see `tools/inspect_model.py`):
 *
 * ```
 * signature : serving_default
 * inputs    : keras_tensor   int32  [1, 24]
 * outputs   : output_0       float32[1, 5]
 * ops       : Embedding(GatherV2) -> Dense(ReLU) -> Dense(BiasAdd)
 * size      : 592,776 bytes
 * ```
 *
 * It is a small embedding-bag MLP, **not** a MobileBERT transformer.
 *
 * The model exposes a real TFLite *signature* (`serving_default`) with named
 * inputs/outputs, so inference goes through [Interpreter.runSignature]. This
 * is the most robust call available: it resolves tensors by name, so a future
 * export with several inputs does not require changing this code. Passing a
 * `Map` to `run()` would *not* perform named-tensor lookup, and the previous
 * 3-input MobileBERT call could never have succeeded against this model.
 *
 * ## Threading
 *
 * [Interpreter] is explicitly **not** thread-safe, so every call into it is
 * serialised on [lock]. The lock is taken by the methods that actually touch
 * the interpreter: [load] and [close], and — after the fix that made this
 * class safe under concurrent use — [infer] itself, which now holds [lock]
 * across the native call *and* across the read of the output buffer that
 * native call fills.
 *
 * [infer] therefore acquires the lock itself: callers must not hold it, and
 * must not try to. Two concurrent [classify] calls can no longer enter
 * [Interpreter.runSignature] on the same interpreter at the same time.
 *
 * [classify] and [status] are blocking and must be called off the main thread;
 * the ViewModel owns the dispatcher choice.
 *
 * ## Failure reporting
 *
 * Load and inference failures are recorded separately so the UI can say *why*
 * the real model is not running rather than just that it isn't.
 */
class TfliteEmbeddingClassifier(context: Context) : TextClassifier {

    private val assets = ClassifierAssets(context.applicationContext)

    /** Guards the Interpreter; it is explicitly not thread-safe. */
    private val lock = ReentrantLock()

    @Volatile
    private var engine: LoadedEngine? = null

    @Volatile
    private var failure: Failure? = null

    @Volatile
    private var closed = false

    /** Everything needed to run one inference. */
    private class LoadedEngine(
        val interpreter: Interpreter,
        val tokenizer: VocabTokenizer,
        val labels: List<PostCategory>,
        val inputWidth: Int,
        val outputWidth: Int,
        val modelSummary: String,
        /** Signature input key, e.g. `keras_tensor`. */
        val inputName: String,
        /** Signature output key, e.g. `output_0`. */
        val outputName: String,
    )

    private data class Failure(val reason: ClassifierUnavailableReason, val detail: String)

    // region TextClassifier

    override fun status(): ClassifierStatus {
        engine?.let {
            return ClassifierStatus(
                engine = InferenceEngine.TfliteEmbeddingClassifier,
                detail = it.modelSummary,
            )
        }
        val f = failure
        return ClassifierStatus(
            engine = InferenceEngine.KeywordFallback,
            detail = f?.detail ?: "Model not loaded yet",
            unavailableReason = f?.reason,
        )
    }

    override fun classify(text: String): Classification {
        if (text.isBlank()) return Classification.Empty

        // Lazily load so app startup is not blocked by model I/O. A failure is
        // cached so we do not retry (and re-log) on every keystroke.
        val loaded = engine ?: load()

        if (loaded == null) return KeywordScorer.classify(text)

        return try {
            // `infer()` returns null only when the classifier was closed
            // while this call was waiting for the lock. Fall back to keyword
            // scoring so the composer keeps responding instead of crashing.
            infer(loaded, text) ?: KeywordScorer.classify(text)
        } catch (t: Throwable) {
            // The model is present but the call failed: record why, then fall
            // back so the composer keeps responding.
            Log.e(TAG, "Inference failed; degrading to keyword scoring", t)
            failure = Failure(
                ClassifierUnavailableReason.INFERENCE_FAILED,
                t.message ?: t::class.java.simpleName,
            )
            KeywordScorer.classify(text)
        }
    }

    override fun close() {
        lock.withLock {
            if (closed) return
            closed = true
            engine?.interpreter?.close()
            engine = null
        }
        Log.i(TAG, "Interpreter released")
    }

    // endregion

    /**
     * Loads the model once, atomically.
     *
     * Runs under [lock] so two concurrent first-callers cannot each construct
     * an Interpreter; the loser observes the already-loaded engine.
     */
    private fun load(): LoadedEngine? {
        lock.withLock {
            if (closed) return null
            engine?.let { return it }
            failure?.let { return null }

            return try {
                build().also { engine = it }
            } catch (e: ClassifierAssets.MissingAssetException) {
                Log.w(TAG, "Model assets missing: ${e.message}")
                failure = Failure(
                    ClassifierUnavailableReason.MISSING_ASSET,
                    e.message ?: "missing asset",
                )
                null
            } catch (t: Throwable) {
                Log.e(TAG, "Model initialisation failed", t)
                failure = Failure(
                    ClassifierUnavailableReason.INITIALIZATION_FAILED,
                    t.message ?: t::class.java.simpleName,
                )
                null
            }
        }
    }

    /**
     * Builds the interpreter by introspecting the model's *real* signature
     * rather than assuming it. This is what keeps the engine correct for
     * whatever model is actually bundled.
     */
    private fun build(): LoadedEngine {
        val bundle = assets.load()
        val interpreter = Interpreter(bundle.model)

        val inputShape = interpreter.getInputTensor(0).shape()
        val outputShape = interpreter.getOutputTensor(0).shape()

        require(inputShape.size == 2 && outputShape.size == 2) { // rank-2 [batch, dim]
            "Expected rank-2 [batch, dim] tensors, got in=${inputShape.toList()} " +
                    "out=${outputShape.toList()}"
        }
        require(inputShape[0] == 1) {
            "Only batch size 1 is supported, model declares ${inputShape[0]}"
        }
        val inputWidth = inputShape[1]
        val outputWidth = outputShape[1]

        val labels = bundle.labels.map(PostCategory::fromAssetLabelOrGeneral)
        require(labels.size == outputWidth) {
            "labels.txt has ${labels.size} entries but the model emits $outputWidth classes"
        }

        // Resolve the signature's own input/output keys rather than hard-coding
        // tensor names, so the call site stays valid across re-exports.
        val (inputName, outputName) = readSignatureNames(interpreter)

        val summary = "int32[1,$inputWidth] -> float32[1,$outputWidth], " +
                "${bundle.vocab.size} tokens, " +
                labels.joinToString("/") { it.name.lowercase() }

        return LoadedEngine(
            interpreter = interpreter,
            tokenizer = VocabTokenizer(bundle.vocab, inputWidth),
            labels = labels,
            inputWidth = inputWidth,
            outputWidth = outputWidth,
            modelSummary = summary,
            inputName = inputName,
            outputName = outputName,
        )
    }

    /** Reads the single input/output keys declared by [SIGNATURE_KEY]. */
    private fun readSignatureNames(interpreter: Interpreter): Pair<String, String> {
        val keys = interpreter.getSignatureKeys()
        require(SIGNATURE_KEY in keys) {
            "Model does not expose a '$SIGNATURE_KEY' signature (found ${keys.toList()})"
        }
        val inputs = interpreter.getSignatureInputs(SIGNATURE_KEY)
        val outputs = interpreter.getSignatureOutputs(SIGNATURE_KEY)
        require(inputs.size == 1 && outputs.size == 1) {
            "Expected one input and one output, got ${inputs.toList()} -> ${outputs.toList()}"
        }
        return inputs[0] to outputs[0]
    }

    /**
     * Runs one inference.
     *
     * This method **acquires [lock] itself** — callers must not hold it.
     * The reason is [Interpreter]: it is explicitly not thread-safe, so two
     * concurrent [classify] calls must not be able to be inside
     * [Interpreter.runSignature] on the same interpreter at the same time.
     * The lock is therefore held across the native call *and* across the read
     * of `output[0]`, which is the buffer the native side writes into — reading
     * it outside the lock would still be a data race.
     *
     * Tokenisation happens before the lock is taken: [VocabTokenizer] is
     * immutable and thread-safe, so there is no reason to serialise it.
     *
     * Returns `null` when the classifier was closed while this call was
     * waiting for the lock — [close] releases the interpreter, so calling
     * into it afterwards would crash. The caller is expected to fall back to
     * keyword scoring.
     *
     * This is a plain, non-suspending function, so the lock can never be held
     * across a suspension point.
     */
    private fun infer(loaded: LoadedEngine, text: String): Classification? {
        val ids = loaded.tokenizer.encode(text)
        val output = Array(1) { FloatArray(loaded.outputWidth) }

        return lock.withLock {
            // `close()` may have released the interpreter while this caller
            // was queueing for the lock.
            if (closed) return@withLock null

            // Resolve by signature name rather than by tensor index, so the call
            // keeps working if a future export adds or reorders inputs.
            loaded.interpreter.runSignature(
                mapOf(loaded.inputName to arrayOf(ids)),
                mapOf(loaded.outputName to output),
                SIGNATURE_KEY,
            )

            val raw = output[0]
            // The head is softmax, but normalise defensively so a future
            // logits-only export still yields a usable confidence.
            val total = raw.sum()
            val scores = loaded.labels.indices.associate { i ->
                loaded.labels[i] to (if (total > 0f) raw[i] / total else 0f)
            }
            val topIndex = raw.indices.maxByOrNull { raw[it] } ?: 0

            Classification(
                topCategory = loaded.labels[topIndex],
                confidence = scores.getValue(loaded.labels[topIndex]),
                scores = scores,
                engine = InferenceEngine.TfliteEmbeddingClassifier,
            )
        }
    }

    companion object {
        private const val TAG = "TfliteClassifier"

        /** The only signature key this model exports. */
        private const val SIGNATURE_KEY = "serving_default"
    }
}

/** Small helper so `withLock` reads cleanly at the call sites above. */
private inline fun <T> ReentrantLock.withLock(block: () -> T): T {
    lock()
    try {
        return block()
    } finally {
        unlock()
    }
}