package com.pixelsface.towntalk.features.location.domain.usecase

import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all locations.
 */
class GetLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Invoke the use case.
     * @return A Flow of locations.
     */
    operator fun invoke(): Flow<List<Location>> {
        return locationRepository.getLocations()
    }
} 