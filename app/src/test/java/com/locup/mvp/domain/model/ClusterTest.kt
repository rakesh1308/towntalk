package com.locup.mvp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cluster maths is pure Kotlin, so it is tested directly on the JVM rather
 * than through the UI.
 */
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

    @Test
    fun `clustering is stable across repeated calls`() {
        // A subscription matches on this id, so it must be deterministic.
        val first = Cluster.idFor(12.97, 77.59)
        repeat(5) { assertEquals(first, Cluster.idFor(12.97, 77.59)) }
    }

    @Test
    fun `parse recovers the grid indices`() {
        val id = Cluster.idFor(12.97, 77.59)
        val cell = Cluster.parse(id)
        assertNotNull("a well-formed id must parse", cell)
        assertEquals(1297, cell!!.latIndex)
    }

    @Test
    fun `parse rejects malformed ids`() {
        assertNull(Cluster.parse("not-a-cluster"))
        assertNull(Cluster.parse("c_1"))
        assertNull(Cluster.parse("c_a_b"))
    }

    @Test
    fun `GeoPoint derives its cluster id`() {
        assertEquals(Cluster.idFor(12.97, 77.59), GeoPoint(12.97, 77.59).clusterId)
    }
}