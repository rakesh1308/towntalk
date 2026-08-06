package com.locup.mvp.repo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LandmarkRepositoryTest {

    @Test
    fun `point near a known landmark returns its name`() {
        // ~30 m off MG Road landmark (12.97, 77.59)
        assertEquals("MG Road", LandmarkRepository.labelFor(12.9703, 77.5903))
    }

    @Test
    fun `distant point falls back to grid label`() {
        // Delhi, far from any Bengaluru landmark
        val label = LandmarkRepository.labelFor(28.6139, 77.2090)
        assertTrue("expected Area fallback, got $label", label.startsWith("Area "))
    }

    @Test
    fun `labelForCluster formats as grid coordinates`() {
        val label = LandmarkRepository.labelForCluster("c_1297_8256")
        assertEquals("Area 1297/8256", label)
    }
}
