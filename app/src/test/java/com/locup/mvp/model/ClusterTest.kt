package com.locup.mvp.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClusterTest {

    @Test
    fun `nearby points land in same cluster`() {
        val c1 = Cluster.idFor(12.9716, 77.5946)
        val c2 = Cluster.idFor(12.9720, 77.5950)
        assertEquals(c1, c2)
    }

    @Test
    fun `farther points land in different clusters`() {
        val c1 = Cluster.idFor(12.97, 77.59)
        val c2 = Cluster.idFor(13.97, 77.59) // ~111 km north
        assertTrue(c1 != c2)
    }
}
