package com.locup.mvp.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordFallbackTest {

    @Test
    fun `emergency text scores emergency highest`() {
        val r = KeywordFallback.score("reported accident on the bridge, waterlogging!", LocalClassifier.labelOrder)
        assertEquals("Emergency", r.bestLabel)
        assertTrue(r.bestScore > 0.4f)
    }

    @Test
    fun `traffic text scores traffic highest`() {
        val r = KeywordFallback.score("huge traffic jam near the signal, avoid", LocalClassifier.labelOrder)
        assertEquals("Traffic", r.bestLabel)
    }

    @Test
    fun `event text scores event highest`() {
        val r = KeywordFallback.score("music festival at the park this weekend", LocalClassifier.labelOrder)
        assertEquals("Event", r.bestLabel)
    }

    @Test
    fun `civic text scores civic highest`() {
        val r = KeywordFallback.score("garbage pile since two days, smelly", LocalClassifier.labelOrder)
        assertEquals("Civic", r.bestLabel)
    }

    @Test
    fun `feature vector is non-zero on dense inputs`() {
        val v = KeywordFallback.featureVector("traffic jam jam jam", 256)
        assertTrue(v.any { it > 0f })
    }
}
