package com.pixelsface.towntalk.features.chat.domain.usecase

import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import com.pixelsface.towntalk.features.chat.domain.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetChatConversationsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<ChatConversation>>> {
        return chatRepository.getChatConversations(userId)
            .flatMapLatest { conversationsResult ->
                if (conversationsResult is Result.Error) {
                    return@flatMapLatest flowOf(conversationsResult)
                }
                val conversations = (conversationsResult as Result.Success).data
                if (conversations.isEmpty()) {
                    return@flatMapLatest flowOf(Result.success(emptyList()))
                }

                val allParticipantIds = conversations
                    .flatMap { it.participantIds }
                    .distinct()
                    .filter { it != userId }

                if (allParticipantIds.isEmpty()) {
                    return@flatMapLatest flowOf(Result.success(conversations.map { convo ->
                        convo.copy(participantDetails = convo.participantDetails.mapValues { (pUserId, pInfo) ->
                            if (pUserId == userId) {
                                pInfo
                            } else {
                                pInfo
                            }
                        })
                    }))
                }

                val userPresenceFlows: List<Flow<Pair<String, User?>>> = allParticipantIds.map { participantId ->
                    authRepository.getUserStream(participantId).map { userResult ->
                        participantId to userResult.getOrNull()
                    }
                }

                combine(userPresenceFlows) { presenceArray ->
                    val presenceMap = presenceArray.toMap()
                    val enrichedConversations = conversations.map { conversation ->
                        val updatedParticipantDetails = conversation.participantDetails.mapValues { (pId, pInfo) ->
                            val userPresence = presenceMap[pId]
                            pInfo.copy(
                                isOnline = userPresence?.isOnline ?: pInfo.isOnline,
                                lastSeen = userPresence?.lastSeen ?: pInfo.lastSeen
                            )
                        }
                        conversation.copy(participantDetails = updatedParticipantDetails)
                    }
                    Result.success(enrichedConversations)
                }
            }
    }
} 