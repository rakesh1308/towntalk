package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveChatTypingStatusUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(chatId: String): Flow<Result<Set<String>>> {
        return chatRepository.getChatConversationStream(chatId).map { result ->
            result.map { chatConversation ->
                chatConversation.typingParticipantIds
            }
        }
    }
} 