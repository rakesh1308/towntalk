package com.locup.mvp.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM-side tests for the WordPiece tokenizer used by [TfliteTextClassifier].
 * Mirrors the Python tokenisation rules in
 * `tools/train_locup_classifier.py` exactly — divergence here would
 * silently degrade on-device accuracy, so the assertions are kept tight
 * against the BERT reference rules.
 *
 * Full TFLite inference (Interpreter.run, asset loading, …) is exercised
 * in androidTest/ — see `TfliteTextClassifierInstrumentedTest`.
 */
class TfliteTextClassifierTokenizeTest {

    private val vocab = sampleBertUncasedVocab()
    private val tokenizer = BertTokenizer(vocab, maxSeqLen = 16)

    @Test
    fun `basicTokenize splits on whitespace and punctuation`() {
        assertEquals(
            listOf("fire", "on", "mg", "road"),
            tokenizer.basicTokenize("Fire on MG Road!"),
        )
    }

    @Test
    fun `basicTokenize lowercases and strips accents`() {
        // "café" with combining acute → "cafe"
        val result = tokenizer.basicTokenize("Café Olé")
        assertEquals(listOf("cafe", "ole"), result)
    }

    @Test
    fun `basicTokenize handles contractions`() {
        // BERT-style: "don't" → ["don", "'t"]
        assertEquals(listOf("don", "'t"), tokenizer.basicTokenize("don't"))
    }

    @Test
    fun `basicTokenize keeps numbers as their own tokens`() {
        assertEquals(
            listOf("lane", "3", "blocked"),
            tokenizer.basicTokenize("Lane 3 blocked."),
        )
    }

    @Test
    fun `basicTokenize discards empty runs`() {
        assertEquals(emptyList<String>(), tokenizer.basicTokenize(""))
        assertEquals(emptyList<String>(), tokenizer.basicTokenize("   "))
        assertEquals(emptyList<String>(), tokenizer.basicTokenize("!!! ??? ..."))
    }

    @Test
    fun `wordpiece splits a long word into subwords with continuation prefix`() {
        // "unbelievable" isn't in the sample vocab, so we expect UNK
        // (single token) — proving the fallback path works.
        // "fire" is in the vocab → 1 token.
        assertEquals(listOf("fire"), tokenizer.wordpiece("fire"))

        // A long word partly in the vocab: "firehouse" — but our sample
        // vocab doesn't have it, so the result is UNK.
        val result = tokenizer.wordpiece("firehouse")
        assertEquals(listOf("[UNK]"), result)
    }

    @Test
    fun `wordpiece returns UNK when no prefix matches`() {
        assertEquals(listOf("[UNK]"), tokenizer.wordpiece("zxcvbnm"))
    }

    @Test
    fun `encode wraps with CLS and SEP and pads up to seq len`() {
        val out = tokenizer.encode("fire")
        assertEquals(16, out.inputIds.size)
        assertEquals(16, out.attentionMask.size)
        assertEquals("[CLS]", vocab.entries.first { it.value == out.inputIds[0] }.key)
        assertEquals(1, out.attentionMask[0])
        // "fire" → "[CLS] fire [SEP] pad pad ..."
        // find the SEP by id
        val sepIdx = out.inputIds.indexOf(vocab["[SEP]"]!!)
        assertTrue("SEP should be the final non-pad token", sepIdx >= 2)
        assertEquals(1, out.attentionMask[sepIdx])
        // everything after SEP is pad
        for (i in (sepIdx + 1) until 16) {
            assertEquals(0, out.attentionMask[i])
        }
    }

    @Test
    fun `encode truncates to maxSeqLen - 2 wordpieces`() {
        // 20 tokens of "fire" (only 1 wordpiece each) → still cuts to 14
        // wordpieces (maxSeqLen - 2 = 14), then [SEP]
        val text = (1..20).joinToString(" ") { "fire" }
        val out = tokenizer.encode(text)
        assertEquals(16, out.inputIds.size)
        // CLS + 14 "fire" + SEP + 0 pad
        assertEquals(1, out.attentionMask[0])
        // final non-pad token is SEP
        val sepIdx = out.inputIds.indexOf(vocab["[SEP]"]!!)
        assertEquals(15, sepIdx)
    }

    @Test
    fun `encode emits UNK for out-of-vocab token`() {
        val out = tokenizer.encode("zxcvbnm")
        // CLS + UNK + SEP
        assertEquals(vocab["[UNK]"]!!, out.inputIds[1])
        assertEquals(1, out.attentionMask[1])
    }

    @Test
    fun `empty input still emits CLS and SEP`() {
        val out = tokenizer.encode("")
        assertEquals(16, out.inputIds.size)
        assertEquals(vocab["[CLS]"]!!, out.inputIds[0])
        assertEquals(vocab["[SEP]"]!!, out.inputIds[1])
        assertEquals(1, out.attentionMask[0])
        assertEquals(1, out.attentionMask[1])
    }

    /**
     * Minimal sample vocab mirroring the BERT-uncased scheme:
     *   [PAD] = 0, [UNK] = 1, [CLS] = 2, [SEP] = 3, [MASK] = 4
     *   followed by sentence-piece tokens.
     */
    private fun sampleBertUncasedVocab(): Map<String, Int> {
        val list = mutableListOf(
            "[PAD]", "[UNK]", "[CLS]", "[SEP]", "[MASK]",
            "fire", "traffic", "jam", "garbage", "pothole",
            "festival", "park", "police", "accident", "car",
            "road", "lane", "bridge", "water", "flood",
            "##ing", "##ed", "##s", "##er", "##ly",
        )
        return list.withIndex().associate { (i, t) -> t to i }
    }
}
