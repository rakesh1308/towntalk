package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.chat.domain.model.Message
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String, limit: Int = 50): Flow<Result<List<Message>>> {
        return chatRepository.getMessages(chatId, limit)
    }
} 