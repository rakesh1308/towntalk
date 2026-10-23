package com.locup.mvp.domain.repository

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Turns coordinates into a human-readable area name.
 *
 * A port rather than an object because the production implementation would be
 * reverse geocoding; the demo ships a tiny built-in table. Pure Kotlin, so it
 * is unit tested on the JVM.
 */
interface LandmarkResolver {
    fun labelFor(latitude: Double, longitude: Double): String
}

/**
 * Small offline table of well-known Bengaluru landmarks.
 *
 * Deliberately offline: the demo must work with no network and no Play
 * Services, and reverse geocoding would break both constraints.
 */
class TableLandmarkResolver(
    private val landmarks: List<Landmark> = DEFAULT_LANDMARKS,
    private val matchRadiusMeters: Double = 700.0,
) : LandmarkResolver {

    data class Landmark(val latitude: Double, val longitude: Double, val label: String)

    override fun labelFor(latitude: Double, longitude: Double): String {
        val nearest = landmarks.minByOrNull {
            haversineMeters(latitude, longitude, it.latitude, it.longitude)
        }
        val distance = nearest?.let {
            haversineMeters(latitude, longitude, it.latitude, it.longitude)
        } ?: Double.MAX_VALUE
        return if (nearest != null && distance <= matchRadiusMeters) {
            nearest.label
        } else {
            gridLabel(latitude, longitude)
        }
    }

    /** Human-readable grid coordinate, e.g. "Area 1297/7724". */
    private fun gridLabel(latitude: Double, longitude: Double): String {
        val latIndex = (latitude / 0.01).toInt()
        val lonStep = cos(Math.toRadians(20.0)) * 0.01
        val lonIndex = (longitude / lonStep).toInt()
        return "Area $latIndex/$lonIndex"
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                        sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    companion object {
        val DEFAULT_LANDMARKS = listOf(
            Landmark(12.97, 77.59, "MG Road"),
            Landmark(12.97, 77.595, "Cubbon Park"),
            Landmark(12.97, 77.605, "Indiranagar"),
            Landmark(12.97, 77.615, "Koramangala"),
            Landmark(12.98, 77.59, "Whitefield"),
            Landmark(12.96, 77.59, "Jayanagar"),
        )
    }
}