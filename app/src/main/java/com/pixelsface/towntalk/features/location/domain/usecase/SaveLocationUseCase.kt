package com.pixelsface.towntalk.features.location.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case for saving a selected location.
 */
class SaveLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Invoke the use case.
     * @param location The location to save.
     * @return A Result indicating success or failure.
     */
    suspend operator fun invoke(location: Location): Result<Unit> {
        return locationRepository.saveSelectedLocation(location)
    }
} 