package com.pixelsface.towntalk.features.chat.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a chat conversation, typically between two users.
 */
@Parcelize
data class ChatConversation(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantDetails: Map<String, ParticipantInfo> = emptyMap(),
    val lastMessage: MessagePreview? = null,
    val unreadCount: Map<String, Int> = emptyMap(), // Unread count for each participant in this chat
    val lastActivityTimestamp: Long = 0L,
    val createdAt: Long = 0L,
    val typingParticipantIds: Set<String> = emptySet() // Added field
) : Parcelable

/**
 * Basic information about a participant in a chat.
 */
@Parcelize
data class ParticipantInfo(
    val userId: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val isOnline: Boolean = false, 
    val lastSeen: Long? = null
) : Parcelable

/**
 * A brief preview of the last message in a conversation, for display in the chat list.
 */
@Parcelize
data class MessagePreview(
    val messageId: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0L
) : Parcelable 