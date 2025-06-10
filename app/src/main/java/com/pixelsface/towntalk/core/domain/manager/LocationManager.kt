package com.pixelsface.towntalk.core.domain.manager

import com.pixelsface.towntalk.features.location.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing location-related operations in the application.
 */
interface LocationManager {
    /**
     * Checks if the app has location permission.
     *
     * @return true if location permission is granted, false otherwise
     */
    fun hasLocationPermission(): Boolean

    /**
     * Requests location permission from the user.
     */
    fun requestLocationPermission()

    /**
     * Gets the current location of the device.
     *
     * @return Flow emitting the current location
     */
    fun getCurrentLocation(): Flow<Location>

    /**
     * Starts receiving location updates.
     *
     * @return Flow emitting location updates
     */
    fun startLocationUpdates(): Flow<Location>

    /**
     * Stops receiving location updates.
     */
    fun stopLocationUpdates()

    /**
     * Checks if location services are enabled on the device.
     *
     * @return true if location services are enabled, false otherwise
     */
    fun isLocationEnabled(): Boolean

    /**
     * Requests the user to enable location services.
     */
    fun requestEnableLocationServices()
} 