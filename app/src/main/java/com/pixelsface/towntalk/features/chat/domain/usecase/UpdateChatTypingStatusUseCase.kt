package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class UpdateChatTypingStatusUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(chatId: String, isTyping: Boolean): Result<Unit> {
        val currentUserResult = authRepository.getCurrentUser()
        return currentUserResult.fold(
            onSuccess = {
                val userId = it?.id
                if (userId != null) {
                    chatRepository.updateUserTypingStatus(chatId, userId, isTyping)
                } else {
                    Result.Error(UnknownException("User not logged in, cannot update typing status."))
                }
            },
            onFailure = {
                Result.Error(it)
            }
        )
    }
} 