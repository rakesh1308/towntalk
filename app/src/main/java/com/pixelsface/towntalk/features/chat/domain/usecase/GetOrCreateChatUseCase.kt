package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class GetOrCreateChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(otherUserId: String): Result<String> {
        val currentUserResult = authRepository.getCurrentUser()
        val currentUser = currentUserResult.getOrNull()
            ?: return Result.Error(UnknownException("Current user not found or not authenticated."))

        if (currentUser.id == otherUserId) {
            return Result.Error(UnknownException("Cannot create a chat with oneself."))
        }

        // 1. Try to find an existing chat
        val existingChatResult = chatRepository.findChatConversation(currentUser.id, otherUserId)
        if (existingChatResult is Result.Error) {
            return Result.Error(existingChatResult.exception)
        }
        val existingChatId = existingChatResult.getOrNull()
        if (existingChatId != null) {
            return Result.success(existingChatId)
        }

        // 2. If no chat exists, fetch details for the other user
        val otherUserResult = authRepository.getUserById(otherUserId)
        val otherUser = otherUserResult.getOrNull()
            ?: return Result.Error(UnknownException("Details for the other user (ID: $otherUserId) could not be fetched."))

        // 3. Create ParticipantInfo for both users
        val currentUserParticipantInfo = ParticipantInfo(
            userId = currentUser.id,
            name = currentUser.name,
            photoUrl = currentUser.photoUrl
        )
        val otherUserParticipantInfo = ParticipantInfo(
            userId = otherUser.id,
            name = otherUser.name,
            photoUrl = otherUser.photoUrl
        )

        // 4. Create the new chat conversation
        return chatRepository.createChatConversation(
            currentUserId = currentUser.id,
            otherUserId = otherUser.id,
            currentUserInfo = currentUserParticipantInfo,
            otherUserInfo = otherUserParticipantInfo
        )
    }
} 