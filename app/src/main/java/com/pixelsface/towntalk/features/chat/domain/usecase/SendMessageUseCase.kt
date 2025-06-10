package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.model.Message
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository // To get current user details
) {
    suspend operator fun invoke(chatId: String, text: String): Result<Unit> {
        val currentUserResult = authRepository.getCurrentUser()
        val currentUser = currentUserResult.getOrNull() 
            ?: return Result.Error(UnknownException("User not authenticated or error fetching user details."))

        if (currentUser.id.isBlank()){
             return Result.Error(UnknownException("User ID is blank."))
        }

        val message = Message(
            chatId = chatId,
            senderId = currentUser.id,
            senderName = currentUser.name, // Assuming User model has a 'name' field
            text = text,
            timestamp = System.currentTimeMillis()
        )
        
        // First, send the message through the repository
        val sendMessageResult = chatRepository.sendMessage(message)
        if (sendMessageResult is Result.Error) {
            return sendMessageResult // Propagate the error from sendMessage
        }

        // After successfully sending the message, update the conversation metadata
        // This ensures the last message, timestamp, and unread counts are updated.
        return chatRepository.updateConversationOnNewMessage(chatId, message)
    }
} 