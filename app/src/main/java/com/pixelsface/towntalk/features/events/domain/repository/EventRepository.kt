package com.pixelsface.towntalk.features.events.domain.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.events.domain.model.Event

/**
 * Repository interface for event-related operations.
 */
interface EventRepository {
    /**
     * Get events for a specific city, ordered by start time.
     * @param city The city to get events for.
     * @return A Result containing a list of events or an error.
     */
    suspend fun getEvents(city: String): Result<List<Event>>

    /**
     * Get a specific event by its ID.
     * @param eventId The ID of the event.
     * @return A Result containing the event or an error.
     */
    suspend fun getEventById(eventId: String): Result<Event>

    /**
     * Create a new event.
     * @param event The event to create.
     * @return A Result containing the ID of the created event or an error.
     */
    suspend fun createEvent(event: Event): Result<String>

    /**
     * Update an existing event.
     * @param event The event with updated information.
     * @return A Result indicating success or failure.
     */
    suspend fun updateEvent(event: Event): Result<Unit>

    /**
     * Delete an event.
     * @param eventId The ID of the event to delete.
     * @param userId The ID of the user attempting to delete (for authorization).
     * @return A Result indicating success or failure.
     */
    suspend fun deleteEvent(eventId: String, userId: String): Result<Unit>

    /**
     * RSVP to an event.
     * @param eventId The ID of the event.
     * @param userId The ID of the user RSVPing.
     * @return A Result indicating success or failure.
     */
    suspend fun rsvpToEvent(eventId: String, userId: String): Result<Unit>

    /**
     * Cancel an RSVP for an event.
     * @param eventId The ID of the event.
     * @param userId The ID of the user canceling the RSVP.
     * @return A Result indicating success or failure.
     */
    suspend fun cancelRsvp(eventId: String, userId: String): Result<Unit>

    /**
     * Get events organized by a specific user.
     * @param userId The ID of the organizer.
     * @return A Result containing a list of events or an error.
     */
    suspend fun getEventsByOrganizer(userId: String): Result<List<Event>>

    /**
     * Get events a specific user is attending.
     * @param userId The ID of the user.
     * @return A Result containing a list of events or an error.
     */
    suspend fun getAttendingEvents(userId: String): Result<List<Event>>
} 