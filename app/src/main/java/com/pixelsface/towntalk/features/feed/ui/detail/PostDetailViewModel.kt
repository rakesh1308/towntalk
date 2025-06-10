package com.pixelsface.towntalk.features.feed.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.usecase.AddCommentUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.GetPostUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.GetCommentsUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.LikePostUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.UnlikePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * UI state for the PostDetail screen.
 */
sealed class PostDetailUiState {
    object Loading : PostDetailUiState()
    data class Success(
        val post: Post,
        val comments: List<Comment> = emptyList(),
        val isLiked: Boolean = false,
        val isAddingComment: Boolean = false,
        val isSharing: Boolean = false
    ) : PostDetailUiState()
    data class Error(val message: String) : PostDetailUiState()
}

/**
 * ViewModel for the PostDetail screen.
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val getPostUseCase: GetPostUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val currentUserId: String get() = auth.currentUser?.uid ?: ""
    
    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadPost()
    }
    
    /**
     * Load the post details and its comments.
     */
    fun loadPost() {
        viewModelScope.launch {
            _uiState.value = PostDetailUiState.Loading
            
            try {
                val postResult = getPostUseCase(postId)
                val commentsResult = getCommentsUseCase(postId)
                
                if (postResult.isSuccess() && commentsResult.isSuccess()) {
                    val post = postResult.getOrNull()!!
                    val comments = commentsResult.getOrNull()!!
                    val isLiked = post.likes.contains(currentUserId)
                    
                    _uiState.value = PostDetailUiState.Success(
                        post = post,
                        comments = comments,
                        isLiked = isLiked
                    )
                } else {
                    val error = postResult.exceptionOrNull() ?: commentsResult.exceptionOrNull()
                    _uiState.value = PostDetailUiState.Error(error?.message ?: "Failed to load post")
                }
            } catch (e: Exception) {
                _uiState.value = PostDetailUiState.Error(e.message ?: "Failed to load post")
            }
        }
    }
    
    /**
     * Like the post.
     */
    fun likePost() {
        val currentState = _uiState.value
        if (currentState !is PostDetailUiState.Success) return
        
        // Check if user has already liked the post
        if (currentState.post.likes.contains(currentUserId)) {
            return // User has already liked the post, do nothing
        }
        
        viewModelScope.launch {
            likePostUseCase(postId)
                .onSuccess {
                    val updatedLikes = currentState.post.likes + currentUserId
                    val updatedPost = currentState.post.copy(likes = updatedLikes)
                    _uiState.value = currentState.copy(
                        post = updatedPost,
                        isLiked = true
                    )
                }
                .onError { error ->
                    _uiState.value = PostDetailUiState.Error(error.message ?: "Failed to like post")
                }
        }
    }
    
    /**
     * Unlike the post.
     */
    fun unlikePost() {
        val currentState = _uiState.value
        if (currentState !is PostDetailUiState.Success) return
        
        // Check if user has not liked the post
        if (!currentState.post.likes.contains(currentUserId)) {
            return // User hasn't liked the post, do nothing
        }
        
        viewModelScope.launch {
            unlikePostUseCase(postId)
                .onSuccess {
                    val updatedLikes = currentState.post.likes.filter { it != currentUserId }
                    val updatedPost = currentState.post.copy(likes = updatedLikes)
                    _uiState.value = currentState.copy(
                        post = updatedPost,
                        isLiked = false
                    )
                }
                .onError { error ->
                    _uiState.value = PostDetailUiState.Error(error.message ?: "Failed to unlike post")
                }
        }
    }
    
    /**
     * Add a comment to the post.
     */
    fun addComment(content: String) {
        if (content.isBlank()) return
        
        val currentState = _uiState.value
        if (currentState !is PostDetailUiState.Success) return
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isAddingComment = true)
            
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                content = content,
                authorId = currentUserId,
                authorName = auth.currentUser?.displayName ?: "Anonymous",
                timestamp = System.currentTimeMillis()
            )
            
            addCommentUseCase(postId, comment)
                .onSuccess {
                    val updatedComments = currentState.comments + comment
                    val updatedPost = currentState.post.copy(comments = currentState.post.comments + 1)
                    _uiState.value = currentState.copy(
                        post = updatedPost,
                        comments = updatedComments,
                        isAddingComment = false
                    )
                }
                .onError { error ->
                    _uiState.value = currentState.copy(isAddingComment = false)
                    _uiState.value = PostDetailUiState.Error(error.message ?: "Failed to add comment")
                }
        }
    }
    
    /**
     * Share the post.
     * This method updates the UI state to reflect that sharing is in progress,
     * but the actual sharing is handled by the UI layer through Android's share intent.
     */
    fun sharePost() {
        val currentState = _uiState.value
        if (currentState !is PostDetailUiState.Success) return
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(isSharing = true)
                // The actual sharing is handled by the UI layer
                delay(500) // Small delay to show sharing animation if needed
                _uiState.value = currentState.copy(isSharing = false)
            } catch (e: Exception) {
                _uiState.value = currentState.copy(isSharing = false)
            }
        }
    }
} 