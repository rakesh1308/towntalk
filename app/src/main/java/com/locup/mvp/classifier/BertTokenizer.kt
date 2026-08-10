package com.locup.mvp.classifier

import android.util.Log
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * Hand-rolled WordPiece tokenizer for MobileBERT (`google/mobilebert-uncased`).
 *
 * This class implements the **exact same tokenisation rules** as the
 * Python training script ([tools/train_locup_classifier.py](file:///C:/RAKESH/WORK/Development/LocUp/towntalk/tools/train_locup_classifier.py)).
 * Any divergence here will silently degrade on-device accuracy, so
 * the rules below are kept in lockstep with the Python equivalent.
 *
 * Pipeline:
 *   1. **basicTokenize** — Unicode normalise (NFKD), strip combining
 *      marks, lowercase, split into alphabetic / numeric / punctuation
 *      / whitespace runs using the same regex BERT uses.
 *   2. **wordpiece** — for each basic token, run greedy longest-match-first
 *      against the loaded vocab. Out-of-vocab pieces become `[UNK]`.
 *   3. **encode** — wrap with `[CLS]` / `[SEP]`, pad/truncate to
 *      `MAX_SEQ_LEN`, return `input_ids` and `attention_mask`.
 *
 * Tensor type ids are all zeros (single-sentence inputs).
 */
class BertTokenizer(
    private val vocab: Map<String, Int>,
    private val maxSeqLen: Int,
) {
    companion object {
        private const val TAG = "BertTokenizer"

        // Same specials used by `google/mobilebert-uncased`.
        private const val CLS_TOKEN = "[CLS]"
        private const val SEP_TOKEN = "[SEP]"
        private const val PAD_TOKEN = "[PAD]"
        private const val UNK_TOKEN = "[UNK]"

        // Matches BERT's basic tokenizer regex. Order matters: contractions
        // first (so "don't" splits as ["don", "'t"]), then words, then
        // numbers, then punctuation, then whitespace.
        private val BASIC_TOKEN_PATTERN: Pattern = Pattern.compile(
            "'(?:s|ve|re|ll|t|m|d)|[a-z]+|[0-9]+|[^\\sa-z0-9]+|\\s+"
        )

        // Continuation marker — subword pieces past the first start with "##".
        private const val CONTINUATION_PREFIX = "##"
    }

    /** Stripped lowercase + NFKD-normalised, no accents. */
    private fun normalize(text: String): String {
        val nfkd = Normalizer.normalize(text, Normalizer.Form.NFKD)
        return nfkd.lowercase()
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    /**
     * BERT's basic tokenizer: lowercase, strip accents, split on
     * whitespace + punctuation using the standard BERT regex.
     */
    fun basicTokenize(text: String): List<String> {
        val normalized = normalize(text)
        val matcher = BASIC_TOKEN_PATTERN.matcher(normalized)
        val out = ArrayList<String>()
        while (matcher.find()) {
            val tok = matcher.group().trim()
            if (tok.isNotEmpty()) {
                out.add(tok)
            }
        }
        return out
    }

    /**
     * Greedy longest-match-first WordPiece on a single basic token.
     * Returns `[UNK]` if no match is found for any prefix.
     */
    fun wordpiece(token: String): List<String> {
        val out = ArrayList<String>()
        var start = 0
        val n = token.length
        while (start < n) {
            var end = n
            var cur: String? = null
            while (start < end) {
                val sub = if (start == 0) {
                    token.substring(start, end)
                } else {
                    CONTINUATION_PREFIX + token.substring(start, end)
                }
                if (vocab.containsKey(sub)) {
                    cur = sub
                    break
                }
                end -= 1
            }
            if (cur == null) {
                return listOf(UNK_TOKEN)
            }
            out.add(cur)
            start = end
        }
        return out
    }

    /**
     * Run the full pipeline: basic tokenize → WordPiece → wrap with
     * `[CLS]` / `[SEP]` → pad/truncate → return `input_ids` and
     * `attention_mask` (length `maxSeqLen`).
     *
     * The token_type_ids are all zeros (single-sentence input) — the
     * caller can fill them with `IntArray(maxSeqLen) { 0 }`.
     */
    fun encode(text: String): TokenizedInput {
        val clsId = vocab[CLS_TOKEN]
            ?: error("$CLS_TOKEN missing from vocab")
        val sepId = vocab[SEP_TOKEN]
            ?: error("$SEP_TOKEN missing from vocab")
        val padId = vocab[PAD_TOKEN]
            ?: error("$PAD_TOKEN missing from vocab")
        val unkId = vocab[UNK_TOKEN]
            ?: error("$UNK_TOKEN missing from vocab")

        // Step 1+2: basic + WordPiece.
        val wordpieces = ArrayList<String>()
        for (basic in basicTokenize(text)) {
            wordpieces.addAll(wordpiece(basic))
        }

        // Step 3: trim to leave room for [CLS] and [SEP].
        val maxWordpieces = maxSeqLen - 2
        if (wordpieces.size > maxWordpieces) {
            wordpieces.subList(maxWordpieces, wordpieces.size).clear()
        }

        val ids = IntArray(maxSeqLen) { padId }
        val mask = IntArray(maxSeqLen) { 0 }
        ids[0] = clsId
        mask[0] = 1
        for ((i, wp) in wordpieces.withIndex()) {
            ids[i + 1] = vocab[wp] ?: unkId
            mask[i + 1] = 1
        }
        val sepIdx = wordpieces.size + 1
        if (sepIdx < maxSeqLen) {
            ids[sepIdx] = sepId
            mask[sepIdx] = 1
        }
        return TokenizedInput(ids, mask)
    }

    /** Convenience for log lines. */
    fun debugPreview(text: String): String {
        val basic = basicTokenize(text)
        val wordpieces = basic.flatMap { wordpiece(it) }
        return "basic=$basic wordpieces=$wordpieces"
    }

    data class TokenizedInput(
        val inputIds: IntArray,
        val attentionMask: IntArray,
    )

    init {
        Log.i(TAG, "BertTokenizer ready: vocabSize=${vocab.size} maxSeqLen=$maxSeqLen")
    }
}
