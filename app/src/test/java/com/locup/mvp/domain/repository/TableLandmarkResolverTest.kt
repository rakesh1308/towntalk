package com.locup.mvp.domain.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The resolver is an interface in `domain` with an offline table
 * implementation, so these tests exercise the real production behaviour
 * without an Android context.
 */
class TableLandmarkResolverTest {

    private val resolver = TableLandmarkResolver()

    @Test
    fun `point near a known landmark returns its name`() {
        // ~30 m off the MG Road landmark (12.97, 77.59)
        assertEquals("MG Road", resolver.labelFor(12.9703, 77.5903))
    }

    @Test
    fun `distant point falls back to grid label`() {
        // New Delhi, far from any Bengaluru landmark
        val label = resolver.labelFor(28.6139, 77.2090)
        assertTrue("expected Area fallback, got $label", label.startsWith("Area "))
    }

    @Test
    fun `grid fallback encodes both coordinates`() {
        val label = resolver.labelFor(28.6139, 77.2090)
        assertTrue(label, label.matches(Regex("Area -?\\d+/-?\\d+")))
    }

    @Test
    fun `each table landmark resolves to its own name`() {
        TableLandmarkResolver.DEFAULT_LANDMARKS.forEach { landmark ->
            assertEquals(
                landmark.label,
                resolver.labelFor(landmark.latitude, landmark.longitude),
            )
        }
    }
}