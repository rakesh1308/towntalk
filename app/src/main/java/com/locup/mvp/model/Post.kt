package com.locup.mvp.model

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * 1 km × 1 km grid cluster id derived from lat/lon. Mirrors the LocUp spec.
 */
object Cluster {
    /** ~1 km in degrees latitude. */
    private const val LAT_STEP = 0.01
    private val LON_STEP: Double = cos(Math.toRadians(20.0)) * 0.01 // ~1 km at ~20° latitude

    fun idFor(lat: Double, lon: Double): String {
        val latIdx = floor(lat / LAT_STEP).roundToInt()
        val lonIdx = floor(lon / LON_STEP).roundToInt()
        return "c_${latIdx}_${lonIdx}"
    }
}

data class Post(
    val id: String,
    val author: String,
    val text: String,
    val category: String,
    val confidence: Float,
    val lat: Double,
    val lon: Double,
    val clusterId: String,
    val minutesAgo: Int
) {
    val displayLabel: String
        get() = "$category · ${(confidence * 100).toInt()}%"
}

object SamplePosts {
    fun demo(): List<Post> = listOf(
        Post("p1", "Asha", "Waterlogging near the underpass, drive around.", "Emergency", 0.91f, 12.97, 77.59, Cluster.idFor(12.97, 77.59), 4),
        Post("p2", "Ravi", "Heavy traffic on MG Road, avoid until 7pm.", "Traffic", 0.83f, 12.97, 77.60, Cluster.idFor(12.97, 77.60), 11),
        Post("p3", "Meera", "Street food festival at Cubbon Park this weekend!", "Event", 0.76f, 12.97, 77.59, Cluster.idFor(12.97, 77.59), 28),
        Post("p4", "Kunal", "Garbage pile on 5th main, smells awful.", "Civic", 0.69f, 12.97, 77.60, Cluster.idFor(12.97, 77.60), 42),
        Post("p5", "Priya", "Free yoga class at the community hall tomorrow.", "General", 0.62f, 12.97, 77.59, Cluster.idFor(12.97, 77.59), 90)
    )
}
