package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for creating a new event.
 */
class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event): Result<String> {
        // Basic validation (can be expanded)
        if (event.title.isBlank()) {
            return Result.Error(ValidationException("Event title cannot be empty."))
        }
        if (event.city.isBlank()) {
            return Result.Error(ValidationException("Event city cannot be empty."))
        }
        if (event.address.isBlank()) {
            return Result.Error(ValidationException("Event address cannot be empty."))
        }
        if (event.organizerId.isBlank()) {
            return Result.Error(ValidationException("Event organizer ID cannot be empty."))
        }
        // Add more validation as needed (e.g., for start time, category)

        return eventRepository.createEvent(event)
    }
} 