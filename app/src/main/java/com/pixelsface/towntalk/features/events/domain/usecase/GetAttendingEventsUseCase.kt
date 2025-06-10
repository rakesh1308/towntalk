package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for getting events a specific user is attending.
 */
class GetAttendingEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Event>> {
        if (userId.isBlank()) {
            return Result.Error(ValidationException("User ID cannot be empty."))
        }
        return eventRepository.getAttendingEvents(userId)
    }
} 