package com.pixelsface.towntalk.features.feed.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.feed.domain.use_case.PostInteractionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostInteractionEvent {
    data class LikeSuccess(val postId: String) : PostInteractionEvent()
    data class UnlikeSuccess(val postId: String) : PostInteractionEvent()
    data class CommentAdded(val postId: String) : PostInteractionEvent()
    data class CommentDeleted(val postId: String, val commentId: String) : PostInteractionEvent()
    data class Error(val message: String) : PostInteractionEvent()
}

@HiltViewModel
class PostInteractionViewModel @Inject constructor(
    private val postInteractionUseCase: PostInteractionUseCase
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _events = MutableSharedFlow<PostInteractionEvent>()
    val events: SharedFlow<PostInteractionEvent> = _events.asSharedFlow()
    
    fun likePost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = postInteractionUseCase.likePost(postId)) {
                is Result.Success -> {
                    _events.emit(PostInteractionEvent.LikeSuccess(postId))
                }
                is Result.Error -> {
                    _events.emit(PostInteractionEvent.Error(
                        result.exception.message ?: "Failed to like post"
                    ))
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun unlikePost(postId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = postInteractionUseCase.unlikePost(postId)) {
                is Result.Success -> {
                    _events.emit(PostInteractionEvent.UnlikeSuccess(postId))
                }
                is Result.Error -> {
                    _events.emit(PostInteractionEvent.Error(
                        result.exception.message ?: "Failed to unlike post"
                    ))
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = postInteractionUseCase.addComment(postId, content)) {
                is Result.Success -> {
                    _events.emit(PostInteractionEvent.CommentAdded(postId))
                }
                is Result.Error -> {
                    _events.emit(PostInteractionEvent.Error(
                        result.exception.message ?: "Failed to add comment"
                    ))
                }
            }
            
            _isLoading.value = false
        }
    }
    
    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = postInteractionUseCase.deleteComment(postId, commentId)) {
                is Result.Success -> {
                    _events.emit(PostInteractionEvent.CommentDeleted(postId, commentId))
                }
                is Result.Error -> {
                    _events.emit(PostInteractionEvent.Error(
                        result.exception.message ?: "Failed to delete comment"
                    ))
                }
            }
            
            _isLoading.value = false
        }
    }
} 