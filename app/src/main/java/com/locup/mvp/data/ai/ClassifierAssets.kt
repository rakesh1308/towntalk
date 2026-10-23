package com.locup.mvp.data.ai

import android.content.Context
import android.util.Log
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Reads the three assets the classifier needs out of the APK.
 *
 * Split out from the engine so that "the file is missing" is distinguishable
 * from "the model is broken" — the two need very different fixes and the UI
 * reports them differently.
 */
internal class ClassifierAssets(private val context: Context) {

    data class Loaded(
        val model: MappedByteBuffer,
        val vocab: Map<String, Int>,
        /** Labels in the model's own output order. */
        val labels: List<String>,
    )

    /**
     * Thrown when an asset is absent. Distinguished from a corrupt model so
     * [com.locup.mvp.domain.repository.ClassifierUnavailableReason] can report
     * `MISSING_ASSET` rather than a generic initialisation failure.
     */
    class MissingAssetException(val assetName: String) :
        RuntimeException("Required asset '$assetName' is not bundled in the APK")

    fun load(): Loaded {
        // Probe every asset up front so a missing file is reported by name.
        requireAsset(MODEL_ASSET)
        requireAsset(VOCAB_ASSET)
        requireAsset(LABELS_ASSET)
        return Loaded(
            model = mapModel(),
            vocab = loadVocab(),
            labels = loadLabels(),
        )
    }

    private fun requireAsset(name: String) {
        val present = try {
            context.assets.open(name).close()
            true
        } catch (e: java.io.IOException) {
            Log.w(TAG, "Asset '$name' missing: ${e.message}")
            false
        }
        if (!present) throw MissingAssetException(name)
    }

    /**
     * Memory-maps the model rather than reading it into a byte array: the
     * interpreter needs random access, and mapping avoids doubling peak
     * memory for a file this size.
     */
    private fun mapModel(): MappedByteBuffer {
        val fd = context.assets.openFd(MODEL_ASSET)
        FileInputStream(fd.fileDescriptor).use { stream ->
            return stream.channel.map(
                FileChannel.MapMode.READ_ONLY,
                fd.startOffset,
                fd.declaredLength,
            )
        }
    }

    /**
     * Parses `vocab.json`, a flat `{"token": id}` object (8 KB, 578 entries).
     *
     * Parsed by hand with a small scanner rather than pulling in a JSON
     * dependency or `org.json` reflection for a single flat map — this keeps
     * the asset contract obvious and avoids a heavyweight parse at startup.
     */
    private fun loadVocab(): Map<String, Int> {
        val raw = context.assets.open(VOCAB_ASSET).bufferedReader().use { it.readText() }
        val vocab = LinkedHashMap<String, Int>(1024)
        var i = 0
        val n = raw.length

        fun skipWhitespace() {
            while (i < n && raw[i].isWhitespace()) i++
        }

        fun readString(): String {
            require(i < n && raw[i] == '"') { "Expected '\"' at offset $i in $VOCAB_ASSET" }
            i++
            val sb = StringBuilder()
            while (i < n && raw[i] != '"') {
                when (val c = raw[i]) {
                    '\\' -> {
                        i++
                        require(i < n) { "Dangling escape in $VOCAB_ASSET" }
                        when (val esc = raw[i]) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('')
                            'u' -> {
                                require(i + 4 < n) { "Truncated \\u escape in $VOCAB_ASSET" }
                                sb.append(raw.substring(i + 1, i + 5).toInt(16).toChar())
                                i += 4
                            }
                            else -> sb.append(esc)
                        }
                        i++
                    }
                    else -> sb.append(c)
                }
                i++
            }
            require(i < n) { "Unterminated string in $VOCAB_ASSET" }
            i++ // closing quote
            return sb.toString()
        }

        fun readInt(): Int {
            skipWhitespace()
            val start = i
            if (i < n && (raw[i] == '-' || raw[i] == '+')) i++
            while (i < n && raw[i].isDigit()) i++
            require(start < i) { "Expected an integer at offset $start in $VOCAB_ASSET" }
            return raw.substring(start, i).toInt()
        }

        skipWhitespace()
        require(i < n && raw[i] == '{') { "$VOCAB_ASSET must be a JSON object" }
        i++
        skipWhitespace()
        if (i < n && raw[i] == '}') return vocab

        while (true) {
            skipWhitespace()
            val key = readString()
            skipWhitespace()
            require(i < n && raw[i] == ':') { "Expected ':' after key in $VOCAB_ASSET" }
            i++
            vocab[key] = readInt()
            skipWhitespace()
            require(i < n) { "Unterminated object in $VOCAB_ASSET" }
            when (raw[i]) {
                ',' -> i++
                '}' -> return vocab
                else -> throw IllegalArgumentException(
                    "Expected ',' or '}' at offset $i in $VOCAB_ASSET",
                )
            }
        }
    }

    private fun loadLabels(): List<String> =
        context.assets.open(LABELS_ASSET).bufferedReader().use { reader ->
            reader.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

    companion object {
        private const val TAG = "ClassifierAssets"

        const val MODEL_ASSET = "locup_text_classifier.tflite"
        const val VOCAB_ASSET = "vocab.json"
        const val LABELS_ASSET = "labels.txt"
    }
}