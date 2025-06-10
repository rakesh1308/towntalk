package com.pixelsface.towntalk.features.location.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case for searching locations.
 */
class SearchLocationsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Invoke the use case.
     * @param query The search query.
     * @return A Result containing a list of matching Locations or an error.
     */
    suspend operator fun invoke(query: String): Result<List<Location>> {
        return locationRepository.searchLocations(query)
    }
} 