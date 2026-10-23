package com.locup.mvp.domain.model

/**
 * A plain lat/lon pair. Kept in `domain` (not `android.location.Location`)
 * so the domain layer stays free of Android types and remains JVM-testable.
 */
data class GeoPoint(val latitude: Double, val longitude: Double) {
    val clusterId: String get() = Cluster.idFor(latitude, longitude)
}