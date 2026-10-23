package com.locup.mvp.data.ai

import com.locup.mvp.domain.model.InferenceEngine
import com.locup.mvp.domain.model.PostCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The keyword scorer is the documented fallback, so it must stay correct and,
 * critically, must always identify itself as the fallback.
 */
class KeywordScorerTest {

    @Test
    fun `emergency text scores emergency highest`() {
        val r = KeywordScorer.classify("reported accident on the bridge, waterlogging!")
        assertEquals(PostCategory.Emergency, r.topCategory)
        assertTrue(r.confidence > 0.4f)
    }

    @Test
    fun `traffic text scores traffic highest`() {
        val r = KeywordScorer.classify("huge traffic jam near the signal, avoid")
        assertEquals(PostCategory.Traffic, r.topCategory)
    }

    @Test
    fun `event text scores event highest`() {
        val r = KeywordScorer.classify("music festival at the park this weekend")
        assertEquals(PostCategory.Event, r.topCategory)
    }

    @Test
    fun `civic text scores civic highest`() {
        val r = KeywordScorer.classify("garbage pile since two days, smelly")
        assertEquals(PostCategory.Civic, r.topCategory)
    }

    @Test
    fun `general text scores general highest`() {
        val r = KeywordScorer.classify("hello anyone, thanks for the help")
        assertEquals(PostCategory.General, r.topCategory)
    }

    @Test
    fun `scores always sum to one`() {
        val r = KeywordScorer.classify("traffic jam and garbage everywhere")
        assertEquals(1f, r.scores.values.sum(), 1e-4f)
    }

    @Test
    fun `no seed words still produces a valid distribution`() {
        // Smoothing must prevent a zero denominator / NaN.
        val r = KeywordScorer.classify("zzzz qqqq")
        assertEquals(PostCategory.entries.size, r.scores.size)
        assertTrue(r.scores.values.all { it.isFinite() })
    }

    @Test
    fun `result always reports the fallback engine`() {
        // Guards the rule that a fallback must never be presented as a model.
        assertEquals(
            InferenceEngine.KeywordFallback,
            KeywordScorer.classify("anything at all").engine,
        )
    }

    @Test
    fun `scoring is deterministic`() {
        val a = KeywordScorer.classify("traffic jam near the bridge")
        val b = KeywordScorer.classify("traffic jam near the bridge")
        assertEquals(a.topCategory, b.topCategory)
        assertEquals(a.confidence, b.confidence, 0f)
    }

    @Test
    fun `classification is case insensitive`() {
        assertEquals(
            KeywordScorer.classify("TRAFFIC JAM").topCategory,
            KeywordScorer.classify("traffic jam").topCategory,
        )
    }
}