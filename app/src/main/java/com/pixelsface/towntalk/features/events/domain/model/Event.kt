package com.pixelsface.towntalk.features.events.domain.model

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Domain model representing an event in the TownTalk application.
 */
data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val city: String = "", // For querying events by city
    val address: String = "", // Detailed address or venue name
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startTime: Timestamp = Timestamp.now(),
    val endTime: Timestamp? = null, // Optional end time
    val organizerId: String = "",
    val organizerName: String = "", // Denormalized for easier display
    val attendees: List<String> = emptyList(), // List of user IDs
    val rsvpCount: Int = 0, // Derived or stored count of RSVPs
    val category: String = "", // E.g., Music, Workshop, Community Meetup
    val imageUrls: List<String>? = null, // Changed from imageUrl: String?
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val status: String = "ACTIVE" // E.g., ACTIVE, CANCELLED, COMPLETED
) 