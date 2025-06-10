package com.pixelsface.towntalk.features.events.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for getting events for a specific city.
 */
class GetEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    suspend operator fun invoke(city: String): Result<List<Event>> {
        return eventRepository.getEvents(city)
    }
} 