package com.locup.mvp.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the tokenizer that matches the bundled model: a fixed-width id
 * sequence built from `vocab.json`.
 *
 * These rules must stay in lockstep with whatever produced the model, because
 * a mismatch here silently degrades every prediction rather than failing
 * loudly.
 */
class VocabTokenizerTest {

    private val vocab = mapOf(
        VocabTokenizer.PAD to 0,
        VocabTokenizer.OOV to 1,
        "traffic" to 2,
        "jam" to 3,
        "fire" to 4,
        "on" to 5,
    )
    private val tokenizer = VocabTokenizer(vocab, sequenceLength = 8)

    @Test
    fun `splits on whitespace and punctuation`() {
        assertEquals(
            listOf("fire", "on", "traffic"),
            tokenizer.tokenize("Fire on traffic!"),
        )
    }

    @Test
    fun `lowercases input`() {
        assertEquals(listOf("jam"), tokenizer.tokenize("JAM"))
    }

    @Test
    fun `strips digits attached to words`() {
        assertEquals(listOf("5th", "avenue"), tokenizer.tokenize("5th Avenue"))
    }

    @Test
    fun `unknown words map to OOV`() {
        val ids = tokenizer.encode("firehouse")
        assertEquals(vocab[VocabTokenizer.OOV], ids[0])
    }

    @Test
    fun `known words map to their vocab id`() {
        val ids = tokenizer.encode("traffic")
        assertEquals(vocab["traffic"], ids[0])
    }

    @Test
    fun `always produces the model input width`() {
        // The model declares [1, 24]; a short input must still be padded so no
        // runtime reshaping is ever needed.
        assertEquals(8, tokenizer.encode("traffic").size)
        assertEquals(8, tokenizer.encode("").size)
        assertEquals(
            8,
            tokenizer.encode("traffic jam fire on traffic jam fire on traffic").size,
        )
    }

    @Test
    fun `padding fills the tail with PAD`() {
        val ids = tokenizer.encode("traffic jam")
        assertEquals(vocab[VocabTokenizer.PAD], ids[2])
        assertEquals(vocab[VocabTokenizer.PAD], ids[7])
    }

    @Test
    fun `long input is truncated to the sequence width`() {
        val ids = tokenizer.encode((1..20).joinToString(" ") { "traffic" })
        assertEquals(8, ids.size)
        assertTrue(ids.all { it == vocab["traffic"] })
    }

    @Test
    fun `empty input is all padding`() {
        val ids = tokenizer.encode("")
        assertTrue(ids.all { it == vocab[VocabTokenizer.PAD] })
    }

    @Test
    fun `tokenizing is pure and repeatable`() {
        val text = "Traffic jam near MG Road!"
        assertEquals(tokenizer.encode(text).toList(), tokenizer.encode(text).toList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `vocab without PAD is rejected at construction`() {
        VocabTokenizer(mapOf(VocabTokenizer.OOV to 0, "a" to 1), sequenceLength = 4)
    }
}