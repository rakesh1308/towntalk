package com.pixelsface.towntalk.features.chat.domain.repository

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.model.Message
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat-related operations.
 */
interface ChatRepository {

    /**
     * Gets a real-time stream of chat conversations for a given user.
     * @param userId The ID of the current user.
     * @return A Flow emitting a Result containing a list of ChatConversations or an error.
     */
    fun getChatConversations(userId: String): Flow<Result<List<ChatConversation>>>

    /**
     * Gets a real-time stream of a single chat conversation, including typing status.
     * @param chatId The ID of the chat conversation.
     * @return A Flow emitting a Result containing the ChatConversation or an error.
     */
    fun getChatConversationStream(chatId: String): Flow<Result<ChatConversation>>

    /**
     * Gets a real-time stream of messages for a specific chat conversation.
     * @param chatId The ID of the chat conversation.
     * @param limit The maximum number of messages to retrieve initially (for pagination).
     * @return A Flow emitting a Result containing a list of Messages or an error.
     */
    fun getMessages(chatId: String, limit: Int = 50): Flow<Result<List<Message>>>

    /**
     * Sends a message to a chat conversation.
     * This will also trigger an update to the conversation's last message details.
     * @param message The message to send (should include chatId, senderId, text, timestamp).
     * @return A Result indicating success or failure.
     */
    suspend fun sendMessage(message: Message): Result<Unit>

    /**
     * Finds an existing 1-on-1 chat conversation between the current user and another user.
     * @param currentUserId The ID of the current user.
     * @param otherUserId The ID of the other user.
     * @return A Result containing the chatId if found, or null if not found, or an error.
     */
    suspend fun findChatConversation(currentUserId: String, otherUserId: String): Result<String?>

    /**
     * Creates a new 1-on-1 chat conversation.
     * @param currentUserId The ID of the current user.
     * @param otherUserId The ID of the other user.
     * @param currentUserInfo ParticipantInfo for the current user.
     * @param otherUserInfo ParticipantInfo for the other user.
     * @return A Result containing the ID of the newly created chat conversation or an error.
     */
    suspend fun createChatConversation(
        currentUserId: String,
        otherUserId: String,
        currentUserInfo: ParticipantInfo,
        otherUserInfo: ParticipantInfo
    ): Result<String>
    
    /**
     * Updates the conversation document (e.g., lastMessage, timestamp, unread counts)
     * typically after a new message is sent.
     * Note: This might be called internally by sendMessage or be part of its transaction.
     * For now, exposing it if granular control or background updates are needed.
     * @param chatId The ID of the conversation to update.
     * @param message The latest message that was sent to this conversation.
     * @return A Result indicating success or failure.
     */
    suspend fun updateConversationOnNewMessage(chatId: String, message: Message): Result<Unit>

    /**
     * Marks all messages in a conversation as read for the specified user.
     * This typically involves resetting the unread count for that user in the conversation.
     * @param chatId The ID of the chat conversation.
     * @param userId The ID of the user for whom the conversation is to be marked as read.
     * @return A Result indicating success or failure.
     */
    suspend fun markConversationAsRead(chatId: String, userId: String): Result<Unit>

    /**
     * Updates the typing status of a user within a specific chat conversation.
     * @param chatId The ID of the chat conversation.
     * @param userId The ID of the user whose typing status is being updated.
     * @param isTyping True if the user is typing, false otherwise.
     * @return A Result indicating success or failure.
     */
    suspend fun updateUserTypingStatus(chatId: String, userId: String, isTyping: Boolean): Result<Unit>
} 