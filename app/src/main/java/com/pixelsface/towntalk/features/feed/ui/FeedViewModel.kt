package com.pixelsface.towntalk.features.feed.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.usecase.GetPostsUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.LikePostUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.UnlikePostUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.AddCommentUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.DeletePostUseCase
import com.pixelsface.towntalk.features.feed.domain.usecase.ReportPostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth

/**
 * UI state for the Feed screen.
 */
sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

/**
 * ViewModel for the Feed screen.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val reportPostUseCase: ReportPostUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    internal val currentUserId: String get() = auth.currentUser?.uid ?: ""
    internal val currentUserName: String get() = auth.currentUser?.displayName ?: "Anonymous"
    private val _currentUserCity = MutableStateFlow("Mumbai") // Default city
    internal val currentUserCity: String get() = _currentUserCity.value
    
    init {
        loadPosts()
    }
    
    /**
     * Load posts for the current user's city.
     */
    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            
            getPostsUseCase(currentUserCity)
                .onSuccess { posts ->
                    _uiState.value = FeedUiState.Success(posts)
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    /**
     * Like a post.
     */
    fun likePost(postId: String) {
        viewModelScope.launch {
            if (currentUserId.isBlank()) {
                _uiState.value = FeedUiState.Error("Please sign in to like posts")
                return@launch
            }

            likePostUseCase(postId)
                .onSuccess {
                    // Update the UI state with the new like
                    updatePostLikes(postId, true)
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to like post")
                }
        }
    }

    /**
     * Unlike a post.
     */
    fun unlikePost(postId: String) {
        viewModelScope.launch {
            if (currentUserId.isBlank()) {
                _uiState.value = FeedUiState.Error("Please sign in to unlike posts")
                return@launch
            }

            unlikePostUseCase(postId)
                .onSuccess {
                    // Update the UI state with the removed like
                    updatePostLikes(postId, false)
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to unlike post")
                }
        }
    }

    /**
     * Add a comment to a post.
     */
    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            if (currentUserId.isBlank()) {
                _uiState.value = FeedUiState.Error("Please sign in to comment")
                return@launch
            }

            val comment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                content = content,
                authorId = currentUserId,
                authorName = currentUserName,
                timestamp = System.currentTimeMillis()
            )

            addCommentUseCase(postId, comment)
                .onSuccess {
                    // Update the UI state with the new comment
                    updatePostComments(postId)
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to add comment")
                }
        }
    }

    /**
     * Update the likes for a post in the UI state.
     */
    private fun updatePostLikes(postId: String, isLiked: Boolean) {
        val currentState = _uiState.value
        if (currentState is FeedUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    val updatedLikes = if (isLiked) {
                        post.likes + currentUserId
                    } else {
                        post.likes - currentUserId
                    }
                    post.copy(likes = updatedLikes)
                } else {
                    post
                }
            }
            _uiState.value = FeedUiState.Success(updatedPosts)
        }
    }

    /**
     * Update the comments count for a post in the UI state.
     */
    private fun updatePostComments(postId: String) {
        val currentState = _uiState.value
        if (currentState is FeedUiState.Success) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    post.copy(comments = post.comments + 1)
                } else {
                    post
                }
            }
            _uiState.value = FeedUiState.Success(updatedPosts)
        }
    }

    /**
     * Called when the screen resumes to refresh the posts.
     */
    fun onResume() {
        loadPosts()
    }

    /**
     * Share a post.
     */
    fun sharePost(post: Post): String {
        return buildString {
            append("Check out this post on TownTalk!\n\n")
            if (post.title.isNotBlank()) {
                append(post.title)
                append("\n\n")
            }
            append(post.content)
            append("\n\nPosted by ${post.authorName} in ${post.city}")
            append("\n\nhttps://towntalk.app/posts/${post.id}")
        }
    }

    /**
     * Get the deep link for a post.
     */
    fun getPostLink(postId: String): String {
        return "https://towntalk.app/posts/$postId"
    }

    /**
     * Delete a post.
     */
    fun deletePost(postId: String) {
        viewModelScope.launch {
            if (currentUserId.isBlank()) {
                _uiState.value = FeedUiState.Error("Please sign in to delete posts")
                return@launch
            }

            // Show loading or a specific deleting state if desired
            // _uiState.value = FeedUiState.Loading // Or a new state e.g., FeedUiState.DeletingPost(postId)

            deletePostUseCase(postId, currentUserId)
                .onSuccess {
                    val currentState = _uiState.value
                    if (currentState is FeedUiState.Success) {
                        val updatedPosts = currentState.posts.filter { it.id != postId }
                        _uiState.value = FeedUiState.Success(updatedPosts)
                        // Optionally, emit an event for a Snackbar/Toast confirmation
                        // _events.emit(FeedEvent.PostDeletedSuccessfully)
                    }
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to delete post")
                }
        }
    }

    /**
     * Report a post.
     */
    fun reportPost(postId: String, reason: String) {
        viewModelScope.launch {
            if (currentUserId.isBlank()) {
                _uiState.value = FeedUiState.Error("Please sign in to report posts")
                return@launch
            }

            reportPostUseCase(postId, currentUserId, reason)
                .onSuccess {
                    // Optionally, emit an event for a Snackbar/Toast confirmation
                    // _events.emit(FeedEvent.PostReportedSuccessfully)
                    // For now, we can just update the UI state or log, 
                    // as there isn't a direct UI element showing "reported" status on the post itself.
                    // Consider adding a temporary state or message if needed.
                    Log.i("FeedViewModel", "Post $postId reported successfully by user $currentUserId for reason: $reason")
                }
                .onError { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Failed to report post")
                }
        }
    }

    /**
     * Update the current user's city and reload posts.
     */
    fun updateCurrentCity(city: String) {
        _currentUserCity.value = city
        loadPosts()
    }
} 