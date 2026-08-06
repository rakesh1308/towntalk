package com.locup.mvp.repo

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Maps a (lat, lon) to a human-readable area label ("Near {label}").
 *
 * The MVP keeps a small in-memory table of seed landmarks. Posts whose
 * coordinates fall within ~700 m of a landmark use its label; everything
 * else falls back to a grid-coordinate string. Production code would
 * back this with a reverse-geocoding service or a downloaded boundary
 * file.
 */
object LandmarkRepository {

    private data class Landmark(val lat: Double, val lon: Double, val label: String)

    private val landmarks: List<Landmark> = listOf(
        Landmark(12.97, 77.59, "MG Road"),
        Landmark(12.97, 77.595, "Cubbon Park"),
        Landmark(12.97, 77.605, "Indiranagar"),
        Landmark(12.97, 77.615, "Koramangala"),
        Landmark(12.98, 77.59, "Whitefield"),
        Landmark(12.96, 77.59, "Jayanagar")
    )

    private const val MATCH_RADIUS_M = 700.0

    fun labelFor(lat: Double, lon: Double): String {
        val nearest = landmarks.minByOrNull { haversineMeters(lat, lon, it.lat, it.lon) }
        return if (nearest != null && haversineMeters(lat, lon, nearest.lat, nearest.lon) <= MATCH_RADIUS_M) {
            nearest.label
        } else {
            formatGrid(lat, lon)
        }
    }

    fun labelForCluster(clusterId: String): String {
        // clusterId is "c_latIdx_lonIdx" — strip prefix and pretty-print as grid coords.
        return clusterId.removePrefix("c_").replace("_", "/")
            .let { "Area $it" }
    }

    private fun formatGrid(lat: Double, lon: Double): String {
        // ~1 km grid: same step as Cluster.idFor
        val latIdx = (lat / 0.01).toInt()
        val lonStep = cos(Math.toRadians(20.0)) * 0.01
        val lonIdx = (lon / lonStep).toInt()
        return "Area $latIdx/$lonIdx"
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
