package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for RSVPing to an event.
 */
class RsvpToEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): Result<Unit> {
        if (eventId.isBlank()) {
            return Result.Error(ValidationException("Event ID cannot be empty."))
        }
        if (userId.isBlank()) {
            return Result.Error(ValidationException("User ID cannot be empty."))
        }
        return eventRepository.rsvpToEvent(eventId, userId)
    }
} 