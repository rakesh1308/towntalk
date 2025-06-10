package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for updating an existing event.
 */
class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(event: Event): Result<Unit> {
        if (event.id.isBlank()) {
            return Result.Error(ValidationException("Event ID cannot be empty for an update."))
        }
        if (event.title.isBlank()) {
            return Result.Error(ValidationException("Event title cannot be empty."))
        }
        // Add other necessary validations similar to CreateEventUseCase

        return eventRepository.updateEvent(event)
    }
} 