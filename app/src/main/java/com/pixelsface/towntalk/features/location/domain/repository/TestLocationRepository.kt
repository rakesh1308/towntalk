package com.pixelsface.towntalk.features.location.domain.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Test implementation of LocationRepository for testing purposes.
 */
class TestLocationRepository : LocationRepository {
    private val locations = mutableListOf<Location>()
    private var selectedLocation: Location? = null
    private var currentLocation: Location? = null

    override suspend fun searchLocations(query: String): Result<List<Location>> {
        val filteredLocations = locations.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.address.contains(query, ignoreCase = true)
        }
        return Result.success(filteredLocations)
    }

    override suspend fun saveSelectedLocation(location: Location): Result<Unit> {
        selectedLocation = location
        return Result.success(Unit)
    }

    override suspend fun getSelectedLocation(): Result<Location> {
        return selectedLocation?.let { Result.success(it) } 
            ?: Result.error(com.pixelsface.towntalk.core.common.error.ResourceNotFoundException("No location selected"))
    }

    override suspend fun getCurrentLocation(): Result<Location> {
        return currentLocation?.let { Result.success(it) }
            ?: Result.error(com.pixelsface.towntalk.core.common.error.ResourceNotFoundException("Current location not available"))
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        locations.add(location)
        return Result.success(Unit)
    }

    // Test helper methods
    fun setCurrentLocation(location: Location) {
        currentLocation = location
    }

    fun clearLocations() {
        locations.clear()
        selectedLocation = null
        currentLocation = null
    }

    override fun getLocations(): Flow<List<Location>> {
        return flowOf(
            listOf(
                Location(
                    id = "test-id-1",
                    name = "Test Location 1",
                    address = "123 Test St",
                    latitude = 0.0,
                    longitude = 0.0,
                    city = "Test City 1",
                    pincode = "12345"
                ),
                Location(
                    id = "test-id-2",
                    name = "Test Location 2",
                    address = "456 Test Ave",
                    latitude = 1.0,
                    longitude = 1.0,
                    city = "Test City 2",
                    pincode = "67890"
                )
            )
        )
    }
} 