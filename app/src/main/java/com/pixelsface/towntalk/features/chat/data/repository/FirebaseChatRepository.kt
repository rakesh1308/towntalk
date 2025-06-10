package com.pixelsface.towntalk.features.chat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.chat.data.model.ChatConversationDto
import com.pixelsface.towntalk.features.chat.data.model.MessageDto
import com.pixelsface.towntalk.features.chat.data.model.MessagePreviewDto
import com.pixelsface.towntalk.features.chat.data.model.toDomain
import com.pixelsface.towntalk.features.chat.data.model.toDto
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.model.Message
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class FirebaseChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth // May not be strictly needed if userId is always passed in
) : ChatRepository {

    companion object {
        private const val CHATS_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages" // Sub-collection under each chat
        private const val TYPING_STATUS_FIELD = "typing_status" // For consistency
    }

    override fun getChatConversations(userId: String): Flow<Result<List<ChatConversation>>> = callbackFlow {
        val listenerRegistration = firestore.collection(CHATS_COLLECTION)
            .whereArrayContains("participant_ids", userId)
            .orderBy("last_activity_timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(UnknownException("Error fetching chat conversations", error)))
                    channel.close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val conversations = snapshot.toObjects<ChatConversationDto>().map { it.toDomain() }
                    trySend(Result.success(conversations))
                } else {
                    trySend(Result.success(emptyList()))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getChatConversationStream(chatId: String): Flow<Result<ChatConversation>> = callbackFlow {
        val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val listenerRegistration = chatDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Error listening to chat conversation $chatId")
                trySend(Result.Error(UnknownException("Error listening to chat conversation", error)))
                channel.close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val chatDto = snapshot.toObject(ChatConversationDto::class.java)
                if (chatDto != null) {
                    trySend(Result.success(chatDto.toDomain()))
                } else {
                    // This case should ideally not happen if snapshot.exists() is true and DTO is correct
                    Timber.e("Chat conversation $chatId exists but failed to parse to DTO.")
                    trySend(Result.Error(UnknownException("Failed to parse chat conversation data")))
                }
            } else {
                // Document does not exist or snapshot is null (though error should cover null snapshot with error)
                Timber.d("Chat conversation $chatId does not exist or snapshot is null.")
                trySend(Result.Error(UnknownException("Chat conversation not found"))) // Or Result.success(null) if interface allowed nullable ChatConversation
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override fun getMessages(chatId: String, limit: Int): Flow<Result<List<Message>>> = callbackFlow {
        val listenerRegistration = firestore.collection(CHATS_COLLECTION).document(chatId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(UnknownException("Error fetching messages for chat $chatId", error)))
                    channel.close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects<MessageDto>().map { it.toDomain() }.sortedBy { it.timestamp }
                    trySend(Result.success(messages))
                } else {
                     trySend(Result.success(emptyList()))
                }
            }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(message.chatId)
            val messageColRef = chatDocRef.collection(MESSAGES_COLLECTION)
            val messageDto = message.toDto()
            val messageDoc = if (messageDto.id.isNotBlank()) messageColRef.document(messageDto.id) else messageColRef.document()
            if (messageDto.id.isBlank()) {
                messageDto.id = messageDoc.id 
            }
            messageDoc.set(messageDto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            Result.Error(UnknownException("Error sending message", e))
        }
    }
    
    override suspend fun updateConversationOnNewMessage(chatId: String, message: Message): Result<Unit> {
         return try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            val chatSnapshot = chatDocRef.get().await()
            val participantIds = chatSnapshot.toObject<ChatConversationDto>()?.participantIds ?: emptyList()
            val lastMessagePreview = MessagePreviewDto(
                messageId = message.id, 
                text = message.text,
                senderId = message.senderId,
                timestamp = Date(message.timestamp)
            )
            val updates = mutableMapOf<String, Any?>(
                "last_message" to lastMessagePreview,
                "last_activity_timestamp" to FieldValue.serverTimestamp()
            )
            participantIds.forEach { participantId ->
                if (participantId != message.senderId) {
                    updates["unread_count.$participantId"] = FieldValue.increment(1)
                }
            }
            if (updates.isNotEmpty()) {
                 chatDocRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating conversation on new message for chat $chatId")
            Result.Error(UnknownException("Error updating conversation metadata", e))
        }
    }

    override suspend fun findChatConversation(currentUserId: String, otherUserId: String): Result<String?> {
        return try {
            val querySnapshot = firestore.collection(CHATS_COLLECTION)
                .whereArrayContains("participant_ids", currentUserId)
                .get()
                .await()
            val foundChat = querySnapshot.documents.find { doc ->
                val participantIdsInDoc = doc.get("participant_ids") as? List<String>
                participantIdsInDoc != null && participantIdsInDoc.contains(otherUserId) && participantIdsInDoc.size == 2
            }
            Result.success(foundChat?.id)
        } catch (e: Exception) {
            Timber.e(e, "Error finding chat conversation between $currentUserId and $otherUserId")
            Result.Error(UnknownException("Error finding chat conversation", e))
        }
    }

    override suspend fun createChatConversation(
        currentUserId: String,
        otherUserId: String,
        currentUserInfo: ParticipantInfo,
        otherUserInfo: ParticipantInfo
    ): Result<String> {
        return try {
            val newChatDocRef = firestore.collection(CHATS_COLLECTION).document()
            val chatId = newChatDocRef.id
            val participantIds = listOf(currentUserId, otherUserId).sorted()
            val participantDetailsDto = mapOf(
                currentUserId to currentUserInfo.toDto(),
                otherUserId to otherUserInfo.toDto()
            )
            val unreadCount = mapOf(currentUserId to 0, otherUserId to 0)
            val chatDataForFirestore = mapOf(
                "id" to chatId,
                "participant_ids" to participantIds,
                "participant_details" to participantDetailsDto,
                "last_message" to null,
                "unread_count" to unreadCount,
                "last_activity_timestamp" to FieldValue.serverTimestamp(),
                "created_at" to FieldValue.serverTimestamp(),
                TYPING_STATUS_FIELD to emptyMap<String, Boolean>() // Initialize typing_status as empty map
            )
            newChatDocRef.set(chatDataForFirestore).await()
            Result.success(chatId)
        } catch (e: Exception) {
            Timber.e(e, "Error creating chat conversation between $currentUserId and $otherUserId")
            Result.Error(UnknownException("Error creating chat conversation", e))
        }
    }

    override suspend fun markConversationAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            val updateField = "unread_count.$userId"
            chatDocRef.update(updateField, 0).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error marking conversation $chatId as read for user $userId")
            Result.Error(UnknownException("Error marking conversation as read", e))
        }
    }

    override suspend fun updateUserTypingStatus(chatId: String, userId: String, isTyping: Boolean): Result<Unit> {
        return try {
            val chatDocRef = firestore.collection(CHATS_COLLECTION).document(chatId)
            val typingUpdate: Any = if (isTyping) {
                true
            } else {
                FieldValue.delete() // Remove the user's ID from the map if not typing
            }
            // Use dot notation to update a specific field in the map
            chatDocRef.update("$TYPING_STATUS_FIELD.$userId", typingUpdate).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating typing status for user $userId in chat $chatId")
            Result.Error(UnknownException("Error updating typing status", e))
        }
    }
} 