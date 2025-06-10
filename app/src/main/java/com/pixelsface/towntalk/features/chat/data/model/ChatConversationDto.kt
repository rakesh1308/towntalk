package com.pixelsface.towntalk.features.chat.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.model.MessagePreview
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import java.util.Date

/**
 * DTO for Participant Information.
 */
data class ParticipantInfoDto(
    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("photo_url")
    @set:PropertyName("photo_url")
    var photoUrl: String? = null
) {
    constructor() : this("", "", null)
}

fun ParticipantInfoDto.toDomain(): ParticipantInfo {
    return ParticipantInfo(
        userId = this.userId,
        name = this.name,
        photoUrl = this.photoUrl
    )
}

fun ParticipantInfo.toDto(): ParticipantInfoDto {
    return ParticipantInfoDto(
        userId = this.userId,
        name = this.name,
        photoUrl = this.photoUrl
    )
}

/**
 * DTO for Message Preview.
 */
data class MessagePreviewDto(
    @get:PropertyName("message_id")
    @set:PropertyName("message_id")
    var messageId: String = "",

    @get:PropertyName("text")
    @set:PropertyName("text")
    var text: String = "",

    @get:PropertyName("sender_id")
    @set:PropertyName("sender_id")
    var senderId: String = "",

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Date? = null // Consistent with MessageDto.timestamp
) {
    constructor() : this("", "", "", null)
}

fun MessagePreviewDto.toDomain(): MessagePreview {
    return MessagePreview(
        messageId = this.messageId,
        text = this.text,
        senderId = this.senderId,
        timestamp = this.timestamp?.time ?: 0L
    )
}

fun MessagePreview.toDto(): MessagePreviewDto {
    return MessagePreviewDto(
        messageId = this.messageId,
        text = this.text,
        senderId = this.senderId,
        timestamp = if (this.timestamp == 0L) null else Date(this.timestamp)
    )
}

/**
 * Data Transfer Object for a ChatConversation, used for Firestore interaction.
 */
data class ChatConversationDto(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("participant_ids")
    @set:PropertyName("participant_ids")
    var participantIds: List<String> = emptyList(),

    // Store participant details as a map where key is userId
    @get:PropertyName("participant_details")
    @set:PropertyName("participant_details")
    var participantDetails: Map<String, ParticipantInfoDto> = emptyMap(),

    @get:PropertyName("last_message")
    @set:PropertyName("last_message")
    var lastMessage: MessagePreviewDto? = null,

    // Map of userId to unread count for that user in this chat
    @get:PropertyName("unread_count")
    @set:PropertyName("unread_count")
    var unreadCount: Map<String, Int> = emptyMap(),

    @ServerTimestamp
    @get:PropertyName("last_activity_timestamp")
    @set:PropertyName("last_activity_timestamp")
    var lastActivityTimestamp: Date? = null,

    @ServerTimestamp
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Date? = null,

    @get:PropertyName("typing_status")
    @set:PropertyName("typing_status")
    var typingStatus: Map<String, Boolean>? = null
) {
    constructor() : this("", emptyList(), emptyMap(), null, emptyMap(), null, null, null)
}

// Mapper functions
fun ChatConversationDto.toDomain(): ChatConversation {
    return ChatConversation(
        id = this.id,
        participantIds = this.participantIds,
        participantDetails = this.participantDetails.mapValues { it.value.toDomain() },
        lastMessage = this.lastMessage?.toDomain(),
        unreadCount = this.unreadCount,
        lastActivityTimestamp = this.lastActivityTimestamp?.time ?: 0L,
        createdAt = this.createdAt?.time ?: 0L,
        typingParticipantIds = this.typingStatus?.filterValues { it }?.keys ?: emptySet()
    )
}

fun ChatConversation.toDto(): ChatConversationDto {
    return ChatConversationDto(
        id = this.id,
        participantIds = this.participantIds,
        participantDetails = this.participantDetails.mapValues { it.value.toDto() },
        lastMessage = this.lastMessage?.toDto(),
        unreadCount = this.unreadCount,
        lastActivityTimestamp = if (this.lastActivityTimestamp == 0L) null else Date(this.lastActivityTimestamp),
        createdAt = if (this.createdAt == 0L) null else Date(this.createdAt),
        typingStatus = if (this.typingParticipantIds.isEmpty()) null else this.typingParticipantIds.associateWith { true }
    )
} 