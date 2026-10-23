package com.locup.mvp.domain.model

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Stable ~1 km x 1 km geographic grid used to bucket posts into "areas".
 *
 * Deliberately pure Kotlin with no Android dependency so it can be unit
 * tested on the JVM.
 *
 * Why a grid instead of a distance query: the feed needs a *stable* bucket
 * that is identical for every user and every run, so that a subscription
 * ("tell me about posts near here") matches deterministically. A radius
 * search would make membership depend on the subscriber's exact position
 * and drift as coordinates round.
 */
object Cluster {

    /** ~1 km expressed in degrees of latitude. */
    private const val LAT_STEP = 0.01

    /** Longitude degrees shrink with latitude; 20 deg N keeps this readable. */
    private val LON_STEP: Double = cos(Math.toRadians(20.0)) * 0.01

    fun idFor(lat: Double, lon: Double): String {
        val latIdx = floor(lat / LAT_STEP).roundToInt()
        val lonIdx = floor(lon / LON_STEP).roundToInt()
        return "c_${latIdx}_${lonIdx}"
    }

    /** Inverse of [idFor]; returns null for malformed ids rather than throwing. */
    fun parse(id: String): GridCell? {
        val parts = id.removePrefix("c_").split("_")
        if (parts.size != 2) return null
        val lat = parts[0].toIntOrNull() ?: return null
        val lon = parts[1].toIntOrNull() ?: return null
        return GridCell(lat, lon)
    }

    data class GridCell(val latIndex: Int, val lonIndex: Int)
}