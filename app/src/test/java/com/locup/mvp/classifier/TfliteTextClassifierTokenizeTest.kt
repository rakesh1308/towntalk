package com.locup.mvp.classifier

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * JVM-side tests for the tokenisation rules used by [TfliteTextClassifier].
 *
 * The regex `[a-zA-Z0-9']+` is identical across JVM and Android, so these
 * tests don't need Robolectric. They mirror the exact rules the Colab
 * notebook used during training — divergence here would silently degrade
 * on-device accuracy, so the assertions are kept tight.
 *
 * Full TFLite inference (Interpreter.run, asset loading, …) is exercised
 * in androidTest/ — see TfliteTextClassifierInstrumentedTest.
 */
class TfliteTextClassifierTokenizeTest {

    // Mirror of the production regex. Keep in sync with
    // TfliteTextClassifier.TOKEN_REGEX.
    private val tokenRegex = Regex("[a-zA-Z0-9']+")

    private fun tokenize(text: String): List<String> =
        tokenRegex.findAll(text.lowercase()).map { it.value }.toList()

    @Test
    fun `splits on whitespace and punctuation`() {
        assertEquals(
            listOf("fire", "on", "mg", "road"),
            tokenize("Fire on MG Road!"),
        )
    }

    @Test
    fun `lowercases input`() {
        assertEquals(
            listOf("waterlogging", "today"),
            tokenize("WaterLogging Today"),
        )
    }

    @Test
    fun `preserves apostrophes inside words`() {
        // The Colab tokenizer kept contractions like "don't" as one token.
        assertEquals(listOf("don't", "go"), tokenize("Don't go!"))
    }

    @Test
    fun `numbers are kept as their own tokens`() {
        assertEquals(
            listOf("lane", "3", "blocked"),
            tokenize("Lane 3 blocked."),
        )
    }

    @Test
    fun `empty input yields empty token list`() {
        assertEquals(emptyList<String>(), tokenize(""))
        assertEquals(emptyList<String>(), tokenize("   "))
        assertEquals(emptyList<String>(), tokenize("!!! ??? ..."))
    }

    @Test
    fun `pad truncation to SEQ_LEN drops tail tokens`() {
        // SEQ_LEN is 24 in production. We craft 30 tokens here.
        val text = (1..30).joinToString(" ") { "tok$it" }
        val encoded = encodeForTest(text, vocab = emptyVocab())
        assertEquals(24, encoded.size)
        // Exactly 24 tokens are encoded (rest dropped due to take(SEQ_LEN)).
        var nonPad = 0
        encoded.forEach { if (it != 0) nonPad++ }
        assertEquals(24, nonPad)
        // tok1 is in the vocab (well, all tokens are OOV here) → OOV_INDEX.
        assertEquals(1, encoded[0])
        assertEquals(1, encoded[23])
    }

    @Test
    fun `OOV tokens map to OOV_INDEX`() {
        val ids = encodeForTest("banana apple cherry", vocab = mapOf("apple" to 7))
        assertEquals(listOf(1, 7, 1, 0, 0, 0).take(3), ids.take(3).toList())
    }

    @Test
    fun `encode handles shorter text with padding`() {
        val ids = encodeForTest("fire", vocab = mapOf("fire" to 42))
        assertEquals(24, ids.size)
        assertEquals(42, ids[0])
        assertEquals(0, ids[1])
        assertEquals(0, ids[23])
    }

    // --- helpers ---

    private fun emptyVocab(): Map<String, Int> = emptyMap()

    /**
     * Mirror of [TfliteTextClassifier.encode] that doesn't need a Context.
     */
    private fun encodeForTest(text: String, vocab: Map<String, Int>): IntArray {
        val SEQ_LEN = 24
        val OOV = 1
        val PAD = 0
        val tokens = tokenize(text).take(SEQ_LEN)
        val ids = IntArray(SEQ_LEN) { PAD }
        tokens.forEachIndexed { i, token -> ids[i] = vocab[token] ?: OOV }
        return ids
    }
}
