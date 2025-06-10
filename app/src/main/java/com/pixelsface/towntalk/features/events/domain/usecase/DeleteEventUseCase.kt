package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for deleting an event.
 */
class DeleteEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(eventId: String, userId: String): Result<Unit> {
        if (eventId.isBlank()) {
            return Result.Error(ValidationException("Event ID cannot be empty."))
        }
        if (userId.isBlank()) {
            return Result.Error(ValidationException("User ID cannot be empty for authorization."))
        }
        // Authorization to delete (e.g., checking if user is organizer) will be handled in the repository or by Firestore rules.
        // This use case primarily ensures IDs are provided.
        return eventRepository.deleteEvent(eventId, userId)
    }
} 