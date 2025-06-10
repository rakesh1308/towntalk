package com.pixelsface.towntalk.features.chat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.auth.domain.usecase.ObserveUserPresenceUseCase
import com.pixelsface.towntalk.features.chat.domain.model.Message
import com.pixelsface.towntalk.features.chat.domain.usecase.GetMessagesUseCase
import com.pixelsface.towntalk.features.chat.domain.usecase.MarkConversationAsReadUseCase
import com.pixelsface.towntalk.features.chat.domain.usecase.ObserveChatTypingStatusUseCase
import com.pixelsface.towntalk.features.chat.domain.usecase.SendMessageUseCase
import com.pixelsface.towntalk.features.chat.domain.usecase.UpdateChatTypingStatusUseCase
import com.pixelsface.towntalk.features.chat.ui.ARG_CHAT_ID
import com.pixelsface.towntalk.features.chat.ui.ARG_OTHER_USER_ID
import com.pixelsface.towntalk.features.chat.ui.ARG_OTHER_USER_NAME
import com.pixelsface.towntalk.features.chat.ui.ARG_OTHER_USER_PHOTO_URL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject
import com.pixelsface.towntalk.features.auth.data.UserPreferences

sealed class ChatUiState {
    object Loading : ChatUiState()
    data class Success(val messages: List<Message>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
    object Empty : ChatUiState()
}

sealed class MessageSendState {
    object Idle : MessageSendState()
    object Sending : MessageSendState()
    object Sent : MessageSendState()
    data class Error(val message: String) : MessageSendState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
    private val observeUserPresenceUseCase: ObserveUserPresenceUseCase,
    private val observeChatTypingStatusUseCase: ObserveChatTypingStatusUseCase,
    private val updateChatTypingStatusUseCase: UpdateChatTypingStatusUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _sendState = MutableStateFlow<MessageSendState>(MessageSendState.Idle)
    val sendState: StateFlow<MessageSendState> = _sendState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _otherUserName = MutableStateFlow<String?>(null)
    val otherUserName: StateFlow<String?> = _otherUserName.asStateFlow()

    private val _otherUserPhotoUrl = MutableStateFlow<String?>(null)
    val otherUserPhotoUrl: StateFlow<String?> = _otherUserPhotoUrl.asStateFlow()

    val chatBackgroundColor: StateFlow<String> = MutableStateFlow(userPreferences.getChatBackgroundColor())

    private val chatId: String = savedStateHandle[ARG_CHAT_ID] ?: ""
    private val otherUserId: String = savedStateHandle[ARG_OTHER_USER_ID] ?: ""

    // State for other user's presence
    private val _otherUserPresence = MutableStateFlow<User?>(null)
    val otherUserIsOnline: StateFlow<Boolean> = _otherUserPresence
        .map { it?.isOnline ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val otherUserLastSeen: StateFlow<Long?> = _otherUserPresence
        .map { it?.lastSeen }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // State for other user's typing status
    private val _isOtherUserTyping = MutableStateFlow(false)
    val isOtherUserTyping: StateFlow<Boolean> = _isOtherUserTyping.asStateFlow()
    
    private var typingUpdateJob: Job? = null
    private var lastSentTypingStatus = false

    init {
        viewModelScope.launch {
            _currentUserId.value = authRepository.currentUser?.uid
            _otherUserName.value = savedStateHandle[ARG_OTHER_USER_NAME]
            _otherUserPhotoUrl.value = savedStateHandle[ARG_OTHER_USER_PHOTO_URL]
        }

        if (chatId.isNotBlank()) {
            loadMessages()
            markMessagesAsRead()
            observeOtherUserTypingStatus()
        } else {
            _uiState.value = ChatUiState.Error("Chat ID is missing.")
            Timber.e("ChatViewModel: Chat ID is null or blank.")
        }

        if (otherUserId.isBlank()) {
            Timber.e("ChatViewModel: OtherUser ID is null or blank. Presence will not be shown.")
        } else {
            observeOtherUserPresence()
        }
        
        // Debounced message input listener for typing status
        _messageInput
            .debounce(300) // Adjust debounce time as needed
            .onEach { text ->
                if (chatId.isNotBlank() && _currentUserId.value != null) {
                    val currentlyTyping = text.isNotBlank()
                    if (currentlyTyping != lastSentTypingStatus) {
                        updateChatTypingStatusUseCase(chatId, currentlyTyping)
                        lastSentTypingStatus = currentlyTyping
                    }
                }
            }
            .launchIn(viewModelScope)

        // Fallback: ensure typing status is set to false if input is blank after a longer delay
        _messageInput
            .debounce(2000) // e.g., 2 seconds of inactivity
            .onEach {text -> 
                if (text.isBlank() && lastSentTypingStatus) {
                     if (chatId.isNotBlank() && _currentUserId.value != null) {
                        updateChatTypingStatusUseCase(chatId, false)
                        lastSentTypingStatus = false
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeOtherUserPresence() {
        if (otherUserId.isNotBlank()) {
            observeUserPresenceUseCase(otherUserId)
                .onEach { result ->
                    when (result) {
                        is Result.Success -> _otherUserPresence.value = result.data
                        is Result.Error -> Timber.e(result.exception, "Error observing user presence for $otherUserId")
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun observeOtherUserTypingStatus() {
        observeChatTypingStatusUseCase(chatId)
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        val typingIds = result.data
                        // Other user is typing if their ID is in the set AND it's not the current user
                        val currentUid = _currentUserId.value
                        _isOtherUserTyping.value = if (otherUserId.isNotBlank()) {
                            typingIds.contains(otherUserId) && (currentUid == null || !typingIds.contains(currentUid) || typingIds.size > 1)
                        } else {
                            false
                        }
                    }
                    is Result.Error -> Timber.e(result.exception, "Error observing typing status for chat $chatId")
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            getMessagesUseCase(chatId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = if (result.data.isEmpty()) ChatUiState.Empty else ChatUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Error loading messages for chat $chatId")
                        _uiState.value = ChatUiState.Error(result.exception?.message ?: "Failed to load messages")
                    }
                }
            }
        }
    }

    fun onMessageInputChange(text: String) {
        _messageInput.value = text
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isBlank() || chatId.isBlank()) return

        viewModelScope.launch {
            _sendState.value = MessageSendState.Sending
            // Explicitly set typing to false when a message is sent
            if (lastSentTypingStatus) {
                updateChatTypingStatusUseCase(chatId, false)
                lastSentTypingStatus = false
            }
            val result = sendMessageUseCase(chatId, text)
            when (result) {
                is Result.Success -> {
                    _sendState.value = MessageSendState.Sent
                    _messageInput.value = "" // Clear input field, triggers debounce to set typing to false if not already
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Error sending message to chat $chatId")
                    _sendState.value = MessageSendState.Error(result.exception?.message ?: "Failed to send message")
                }
            }
        }
    }

    private fun markMessagesAsRead() {
        _currentUserId.value?.let { userId ->
            if (chatId.isNotBlank() && userId.isNotBlank()) {
                viewModelScope.launch {
                    val result = markConversationAsReadUseCase(chatId, userId)
                    if (result is Result.Error) {
                        Timber.e(result.exception, "Failed to mark conversation $chatId as read for user $userId")
                    }
                }
            }
        }
    }
    
    fun resetSendState(){
        _sendState.value = MessageSendState.Idle
    }

    fun saveChatBackgroundColor(color: String) {
        userPreferences.saveChatBackgroundColor(color)
        (chatBackgroundColor as MutableStateFlow).value = color
    }
} 