package com.pixelsface.towntalk.features.location.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.location.domain.model.Location
import com.pixelsface.towntalk.features.location.domain.repository.LocationRepository
import javax.inject.Inject

/**
 * Use case for getting the current location of the device.
 */
class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Invoke the use case.
     * @return A Result containing the current Location or an error.
     */
    suspend operator fun invoke(): Result<Location> {
        return locationRepository.getCurrentLocation()
    }
} 