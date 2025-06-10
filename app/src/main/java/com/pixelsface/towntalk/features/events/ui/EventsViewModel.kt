package com.pixelsface.towntalk.features.events.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.events.domain.model.Event
import com.pixelsface.towntalk.features.events.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Events screen.
 */
sealed class EventsUiState {
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
    object Empty : EventsUiState() // For when there are no events to show
}

sealed class EventOperationUiState {
    object Idle : EventOperationUiState()
    object Loading : EventOperationUiState()
    data class Success(val message: String, val eventId: String? = null) : EventOperationUiState()
    data class Error(val message: String) : EventOperationUiState()
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val createEventUseCase: CreateEventUseCase,
    private val updateEventUseCase: UpdateEventUseCase,
    private val deleteEventUseCase: DeleteEventUseCase,
    private val rsvpToEventUseCase: RsvpToEventUseCase,
    private val cancelRsvpUseCase: CancelRsvpUseCase,
    private val getEventsByOrganizerUseCase: GetEventsByOrganizerUseCase,
    private val getAttendingEventsUseCase: GetAttendingEventsUseCase,
    private val uploadEventImagesUseCase: UploadEventImagesUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _eventsUiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val eventsUiState: StateFlow<EventsUiState> = _eventsUiState.asStateFlow()

    private val _eventDetailUiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val eventDetailUiState: StateFlow<EventsUiState> = _eventDetailUiState.asStateFlow()

    private val _eventOperationUiState = MutableStateFlow<EventOperationUiState>(EventOperationUiState.Idle)
    val eventOperationUiState: StateFlow<EventOperationUiState> = _eventOperationUiState.asStateFlow()

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris.asStateFlow()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    var lastRsvpEventId: String? = null

    fun loadEvents(city: String) {
        viewModelScope.launch {
            _eventsUiState.value = EventsUiState.Loading
            getEventsUseCase(city).fold(
                onSuccess = { events ->
                    _eventsUiState.value = if (events.isEmpty()) EventsUiState.Empty else EventsUiState.Success(events)
                },
                onFailure = { exception ->
                    _eventsUiState.value = EventsUiState.Error(exception.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _eventDetailUiState.value = EventsUiState.Loading
            getEventByIdUseCase(eventId).fold(
                onSuccess = { event ->
                    _eventDetailUiState.value = EventsUiState.Success(listOf(event)) // Success expects a list
                },
                onFailure = { exception ->
                    _eventDetailUiState.value = EventsUiState.Error(exception.message ?: "Unknown error")
                }
            )
        }
    }

    fun addImageUri(uri: Uri) {
        _selectedImageUris.value = _selectedImageUris.value + uri
    }

    fun addImageUris(uris: List<Uri>) {
        _selectedImageUris.value = _selectedImageUris.value + uris
    }

    fun removeImageUri(uri: Uri) {
        _selectedImageUris.value = _selectedImageUris.value - uri
    }

    fun clearSelectedImageUris() {
        _selectedImageUris.value = emptyList()
    }

    fun createEvent(event: Event) {
        viewModelScope.launch {
            _eventOperationUiState.value = EventOperationUiState.Loading
            val organizerId = auth.currentUser?.uid
            if (organizerId.isNullOrBlank()) {
                _eventOperationUiState.value = EventOperationUiState.Error("User not authenticated to create event.")
                return@launch
            }
            val eventToCreate = event.copy(
                organizerId = organizerId,
                organizerName = auth.currentUser?.displayName ?: "Anonymous"
            )

            createEventUseCase(eventToCreate).fold(
                onSuccess = { eventId ->
                    if (_selectedImageUris.value.isNotEmpty()) {
                        _eventOperationUiState.value = EventOperationUiState.Loading
                        uploadEventImagesUseCase(eventId, _selectedImageUris.value).fold(
                            onSuccess = { imageUrls ->
                                val updatedEvent = eventToCreate.copy(id = eventId, imageUrls = imageUrls)
                                updateEventUseCase(updatedEvent).fold(
                                    onSuccess = {
                                        _eventOperationUiState.value = EventOperationUiState.Success("Event created and images uploaded successfully!", eventId)
                                        clearSelectedImageUris()
                                    },
                                    onFailure = { exception ->
                                        _eventOperationUiState.value = EventOperationUiState.Error("Event created, but failed to save image URLs: ${exception.message}")
                                    }
                                )
                            },
                            onFailure = { exception ->
                                _eventOperationUiState.value = EventOperationUiState.Error("Event created, but image upload failed: ${exception.message}")
                            }
                        )
                    } else {
                        _eventOperationUiState.value = EventOperationUiState.Success("Event created successfully!", eventId)
                        clearSelectedImageUris()
                    }
                },
                onFailure = { exception ->
                    _eventOperationUiState.value = EventOperationUiState.Error(exception.message ?: "Failed to create event")
                }
            )
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId.isNullOrBlank()){
                 _eventOperationUiState.value = EventOperationUiState.Error("User not authenticated.")
                return@launch
            }
            if (event.organizerId != userId) {
                _eventOperationUiState.value = EventOperationUiState.Error("You are not authorized to update this event.")
                return@launch
            }
            _eventOperationUiState.value = EventOperationUiState.Loading
            updateEventUseCase(event).fold(
                onSuccess = {
                    _eventOperationUiState.value = EventOperationUiState.Success("Event updated successfully!", event.id)
                    loadEventById(event.id)
                },
                onFailure = { exception ->
                    _eventOperationUiState.value = EventOperationUiState.Error(exception.message ?: "Failed to update event")
                }
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId.isNullOrBlank()) {
                _eventOperationUiState.value = EventOperationUiState.Error("User not authenticated to delete event.")
                return@launch
            }
            _eventOperationUiState.value = EventOperationUiState.Loading
            deleteEventUseCase(eventId, userId).fold(
                onSuccess = {
                    _eventOperationUiState.value = EventOperationUiState.Success("Event deleted successfully!", eventId)
                },
                onFailure = { exception ->
                    _eventOperationUiState.value = EventOperationUiState.Error(exception.message ?: "Failed to delete event")
                }
            )
        }
    }

    fun rsvpToEvent(eventId: String) {
        lastRsvpEventId = eventId
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId.isNullOrBlank()) {
                _eventOperationUiState.value = EventOperationUiState.Error("User not authenticated to RSVP.")
                return@launch
            }
            _eventOperationUiState.value = EventOperationUiState.Loading
            rsvpToEventUseCase(eventId, userId).fold(
                onSuccess = {
                    _eventOperationUiState.value = EventOperationUiState.Success("RSVP successful!", eventId)
                    loadEventById(eventId)
                },
                onFailure = { exception ->
                    _eventOperationUiState.value = EventOperationUiState.Error(exception.message ?: "Failed to RSVP")
                }
            )
        }
    }

    fun cancelRsvp(eventId: String) {
        lastRsvpEventId = eventId
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId.isNullOrBlank()) {
                _eventOperationUiState.value = EventOperationUiState.Error("User not authenticated to cancel RSVP.")
                return@launch
            }
            _eventOperationUiState.value = EventOperationUiState.Loading
            cancelRsvpUseCase(eventId, userId).fold(
                onSuccess = {
                    _eventOperationUiState.value = EventOperationUiState.Success("RSVP cancelled.", eventId)
                    loadEventById(eventId)
                },
                onFailure = { exception ->
                    _eventOperationUiState.value = EventOperationUiState.Error(exception.message ?: "Failed to cancel RSVP")
                }
            )
        }
    }

    fun getEventsByOrganizer(organizerId: String) {
        viewModelScope.launch {
            _eventsUiState.value = EventsUiState.Loading
            getEventsByOrganizerUseCase(organizerId).fold(
                onSuccess = { events ->
                    _eventsUiState.value = if (events.isEmpty()) EventsUiState.Empty else EventsUiState.Success(events)
                },
                onFailure = { exception ->
                    _eventsUiState.value = EventsUiState.Error(exception.message ?: "Failed to fetch events by organizer")
                }
            )
        }
    }

    fun getAttendingEvents(userId: String) {
        viewModelScope.launch {
            _eventsUiState.value = EventsUiState.Loading
            getAttendingEventsUseCase(userId).fold(
                onSuccess = { events ->
                    _eventsUiState.value = if (events.isEmpty()) EventsUiState.Empty else EventsUiState.Success(events)
                },
                onFailure = { exception ->
                    _eventsUiState.value = EventsUiState.Error(exception.message ?: "Failed to fetch attending events")
                }
            )
        }
    }

    fun resetEventOperationState() {
        _eventOperationUiState.value = EventOperationUiState.Idle
        lastRsvpEventId = null
    }

    fun setEventOperationError(message: String) {
        _eventOperationUiState.value = EventOperationUiState.Error(message)
    }
} 