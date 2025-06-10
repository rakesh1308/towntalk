package com.pixelsface.towntalk.features.chat.ui.userselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.chat.domain.usecase.GetAllUsersUseCase
import com.pixelsface.towntalk.features.chat.domain.usecase.GetOrCreateChatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class UserSelectionUiState {
    object Loading : UserSelectionUiState()
    data class Success(val users: List<User>) : UserSelectionUiState()
    object Empty : UserSelectionUiState()
    data class Error(val message: String) : UserSelectionUiState()
}

sealed class ChatNavigationState {
    object Idle : ChatNavigationState()
    data class NavigateToChat(val chatId: String, val otherUserId: String, val otherUserName: String) : ChatNavigationState()
    data class Error(val message: String) : ChatNavigationState()
}

@HiltViewModel
class UserSelectionViewModel @Inject constructor(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val getOrCreateChatUseCase: GetOrCreateChatUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserSelectionUiState>(UserSelectionUiState.Loading)
    val uiState: StateFlow<UserSelectionUiState> = _uiState.asStateFlow()

    private val _chatNavigationState = MutableStateFlow<ChatNavigationState>(ChatNavigationState.Idle)
    val chatNavigationState: StateFlow<ChatNavigationState> = _chatNavigationState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            currentUserId = authRepository.getCurrentUser().getOrNull()?.id
            loadUsers()
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UserSelectionUiState.Loading
            when (val result = getAllUsersUseCase()) {
                is Result.Success -> {
                    // Filter out the current user from the list
                    val otherUsers = result.data.filter { it.id != currentUserId }
                    if (otherUsers.isEmpty()) {
                        _uiState.value = UserSelectionUiState.Empty
                    } else {
                        _uiState.value = UserSelectionUiState.Success(otherUsers)
                    }
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error loading users for chat selection")
                    _uiState.value = UserSelectionUiState.Error(result.exception.message ?: "Failed to load users")
                }
            }
        }
    }

    fun onUserSelected(selectedUser: User) {
        if (selectedUser.id.isBlank()) {
            _chatNavigationState.value = ChatNavigationState.Error("Selected user has an invalid ID.")
            return
        }
        viewModelScope.launch {
            // Consider adding a loading state for chat creation if it takes time
            when (val result = getOrCreateChatUseCase(selectedUser.id)) {
                is Result.Success -> {
                    _chatNavigationState.value = ChatNavigationState.NavigateToChat(result.data, selectedUser.id, selectedUser.name)
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error getting or creating chat with user ${selectedUser.id}")
                    _chatNavigationState.value = ChatNavigationState.Error(result.exception.message ?: "Could not start chat")
                }
            }
        }
    }

    fun navigationCompleted() {
        _chatNavigationState.value = ChatNavigationState.Idle
    }

    fun refreshUsers(){
        loadUsers()
    }
} 