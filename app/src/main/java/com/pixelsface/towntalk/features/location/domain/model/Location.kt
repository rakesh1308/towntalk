package com.pixelsface.towntalk.features.location.domain.model

import com.google.android.gms.maps.model.LatLng

/**
 * Represents a location in the application.
 *
 * @param id Unique identifier for the location
 * @param name Name of the location (e.g., city name)
 * @param address Full address of the location
 * @param latitude Latitude coordinate
 * @param longitude Longitude coordinate
 * @param isCurrent Whether this is the user's current location
 */
data class Location(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val pincode: String,
    val isCurrent: Boolean = false
) {
    /**
     * Returns a LatLng object representing this location's coordinates.
     */
    val latLng: LatLng
        get() = LatLng(latitude, longitude)

    companion object {
        fun fromLatLng(latLng: LatLng, name: String = "", address: String = "", city: String = "", pincode: String = ""): Location {
            return Location(
                id = "${latLng.latitude},${latLng.longitude}",
                name = name,
                address = address,
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                city = city,
                pincode = pincode
            )
        }
    }
} 