package com.pixelsface.towntalk.features.chat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.usecase.GetChatConversationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(val conversations: List<ChatConversation>) : ChatListUiState()
    data class Error(val message: String) : ChatListUiState()
    object Empty : ChatListUiState() // When there are no conversations
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val getChatConversationsUseCase: GetChatConversationsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            val userResult = authRepository.getCurrentUser()
            currentUserId = userResult.getOrNull()?.id
            currentUserId?.let {
                loadConversations(it)
            } ?: run {
                _uiState.value = ChatListUiState.Error("User not authenticated.")
                Timber.e("ChatListViewModel: User not authenticated or ID is null.")
            }
        }
    }

    private fun loadConversations(userId: String) {
        viewModelScope.launch {
            _uiState.value = ChatListUiState.Loading
            getChatConversationsUseCase(userId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            _uiState.value = ChatListUiState.Empty
                        } else {
                            _uiState.value = ChatListUiState.Success(result.data)
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error loading chat conversations")
                        _uiState.value = ChatListUiState.Error(result.exception.message ?: "Failed to load chats")
                    }
                }
            }
        }
    }

    fun refreshConversations() {
        currentUserId?.let {
            loadConversations(it)
        } ?: run {
             _uiState.value = ChatListUiState.Error("Cannot refresh, user not authenticated.")
             Timber.e("ChatListViewModel: User not authenticated or ID is null on refresh.")
        }
    }
} 