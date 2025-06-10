package com.pixelsface.towntalk.features.chat.domain.model

/**
 * Represents a single message within a chat conversation.
 */
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "", // Denormalized for easier display
    val text: String = "",
    val timestamp: Long = 0L,
    // val imageUrl: String? = null, // For future: image messages
    // val audioUrl: String? = null, // For future: audio messages
    // val status: MessageStatus = MessageStatus.SENT // For future: sent, delivered, read
)

// For future enhancements:
// enum class MessageStatus {
//     SENDING,
//     SENT,
//     DELIVERED,
//     READ,
//     FAILED
// } 