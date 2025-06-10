package com.pixelsface.towntalk.features.location.domain.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location-related operations.
 */
interface LocationRepository {
    /**
     * Get the current location of the device.
     * @return A Result containing the current Location or an error.
     */
    suspend fun getCurrentLocation(): Result<Location>
    
    /**
     * Search for locations by query string.
     * @param query The search query.
     * @return A Result containing a list of matching Locations or an error.
     */
    suspend fun searchLocations(query: String): Result<List<Location>>
    
    /**
     * Save the selected location for the current user.
     * @param location The location to save.
     * @return A Result indicating success or failure.
     */
    suspend fun saveSelectedLocation(location: Location): Result<Unit>
    
    /**
     * Get the currently selected location for the user.
     * @return A Result containing the selected Location or an error.
     */
    suspend fun getSelectedLocation(): Result<Location>

    /**
     * Get a stream of all locations.
     * @return A Flow of locations.
     */
    fun getLocations(): Flow<List<Location>>
    
    /**
     * Save a location to the database.
     * @param location The location to save.
     * @return A Result indicating success or failure.
     */
    suspend fun saveLocation(location: Location): Result<Unit>
} 