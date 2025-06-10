package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import javax.inject.Inject

class MarkConversationAsReadUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, userId: String): Result<Unit> {
        return chatRepository.markConversationAsRead(chatId, userId)
    }
} 