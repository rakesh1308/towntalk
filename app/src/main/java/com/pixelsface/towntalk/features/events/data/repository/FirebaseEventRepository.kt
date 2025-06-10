package com.pixelsface.towntalk.features.events.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import com.pixelsface.towntalk.core.common.error.AuthorizationException
import com.pixelsface.towntalk.core.common.error.DataNotFoundException
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.repository.EventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class FirebaseEventRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventRepository {

    private val eventsCollection = firestore.collection("events")

    override suspend fun getEvents(city: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = eventsCollection
                .whereEqualTo("city", city)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .await()
            val events = querySnapshot.documents.mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
            Result.Success(events)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error getting events for city $city", e)
            Result.Error(TownTalkException("Failed to get events: ${e.message}", e))
        }
    }

    override suspend fun getEventById(eventId: String): Result<Event> = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = eventsCollection.document(eventId).get().await()
            val event = documentSnapshot.toObject(Event::class.java)?.copy(id = documentSnapshot.id)
            if (event != null) {
                Result.Success(event)
            } else {
                Result.Error(DataNotFoundException("Event not found with ID: $eventId"))
            }
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error getting event by ID $eventId", e)
            Result.Error(TownTalkException("Failed to get event details: ${e.message}", e))
        }
    }

    override suspend fun createEvent(event: Event): Result<String> = withContext(Dispatchers.IO) {
        try {
            val eventId = UUID.randomUUID().toString()
            val currentUser = auth.currentUser ?: return@withContext Result.Error(AuthorizationException("User not authenticated to create event."))
            
            val newEvent = event.copy(
                id = eventId,
                organizerId = currentUser.uid,
                // organizerName could be fetched from user's profile or passed in event object
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            eventsCollection.document(eventId).set(newEvent).await()
            Result.Success(eventId)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error creating event", e)
            Result.Error(TownTalkException("Failed to create event: ${e.message}", e))
        }
    }

    override suspend fun updateEvent(event: Event): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: return@withContext Result.Error(AuthorizationException("User not authenticated to update event."))
            val eventRef = eventsCollection.document(event.id)
            val existingEvent = eventRef.get().await().toObject(Event::class.java)
            
            if (existingEvent == null) {
                return@withContext Result.Error(DataNotFoundException("Event not found to update."))
            }
            if (existingEvent.organizerId != currentUser.uid) {
                return@withContext Result.Error(AuthorizationException("User not authorized to update this event."))
            }

            val updatedEvent = event.copy(updatedAt = Timestamp.now())
            eventRef.set(updatedEvent).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error updating event ${event.id}", e)
            Result.Error(TownTalkException("Failed to update event: ${e.message}", e))
        }
    }

    override suspend fun deleteEvent(eventId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val eventRef = eventsCollection.document(eventId)
            val existingEvent = eventRef.get().await().toObject(Event::class.java)

            if (existingEvent == null) {
                return@withContext Result.Error(DataNotFoundException("Event not found to delete."))
            }
            if (existingEvent.organizerId != userId) {
                return@withContext Result.Error(AuthorizationException("User not authorized to delete this event."))
            }
            // TODO: Consider deleting associated data if any (e.g., images in Storage)
            eventRef.delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error deleting event $eventId by user $userId", e)
            Result.Error(TownTalkException("Failed to delete event: ${e.message}", e))
        }
    }

    override suspend fun rsvpToEvent(eventId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val eventRef = eventsCollection.document(eventId)
            // Check if event exists
            val eventSnapshot = eventRef.get().await()
            if (!eventSnapshot.exists()) {
                return@withContext Result.Error(DataNotFoundException("Event not found to RSVP."))
            }

            eventRef.update("attendees", FieldValue.arrayUnion(userId),
                            "rsvpCount", FieldValue.increment(1),
                            "updatedAt", Timestamp.now()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error RSVPing to event $eventId by user $userId", e)
            Result.Error(TownTalkException("Failed to RSVP to event: ${e.message}", e))
        }
    }

    override suspend fun cancelRsvp(eventId: String, userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val eventRef = eventsCollection.document(eventId)
            // Check if event exists and user is an attendee
            val eventSnapshot = eventRef.get().await()
            if (!eventSnapshot.exists()) {
                return@withContext Result.Error(DataNotFoundException("Event not found to cancel RSVP."))
            }
            val currentAttendees = eventSnapshot.toObject(Event::class.java)?.attendees ?: emptyList()
            if (!currentAttendees.contains(userId)){
                 // Optionally return success if user wasn't an attendee anyway, or a specific error/message
                 Log.w("FirebaseEventRepo", "User $userId not an attendee of event $eventId, cannot cancel RSVP.")
                 return@withContext Result.Success(Unit) // Or a custom Result.Error if preferred
            }

            eventRef.update("attendees", FieldValue.arrayRemove(userId),
                            "rsvpCount", FieldValue.increment(-1),
                            "updatedAt", Timestamp.now()).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error canceling RSVP to event $eventId by user $userId", e)
            Result.Error(TownTalkException("Failed to cancel RSVP: ${e.message}", e))
        }
    }

    override suspend fun getEventsByOrganizer(userId: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = eventsCollection
                .whereEqualTo("organizerId", userId)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .await()
            val events = querySnapshot.documents.mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
            Result.Success(events)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error getting events by organizer $userId", e)
            Result.Error(TownTalkException("Failed to get events by organizer: ${e.message}", e))
        }
    }

    override suspend fun getAttendingEvents(userId: String): Result<List<Event>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = eventsCollection
                .whereArrayContains("attendees", userId)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .await()
            val events = querySnapshot.documents.mapNotNull { it.toObject(Event::class.java)?.copy(id = it.id) }
            Result.Success(events)
        } catch (e: Exception) {
            Log.e("FirebaseEventRepo", "Error getting events attended by $userId", e)
            Result.Error(TownTalkException("Failed to get attending events: ${e.message}", e))
        }
    }
} 