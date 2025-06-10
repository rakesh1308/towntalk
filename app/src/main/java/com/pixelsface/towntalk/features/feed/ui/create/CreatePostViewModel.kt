package com.pixelsface.towntalk.features.feed.ui.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.core.domain.manager.UserManager
import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.features.feed.domain.model.PostImage
import com.pixelsface.towntalk.features.feed.domain.model.UploadStatus
import com.pixelsface.towntalk.features.feed.domain.repository.PostRepository
import com.pixelsface.towntalk.features.feed.domain.usecase.UploadImageUseCase
import com.pixelsface.towntalk.features.feed.domain.validation.PostValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI state for the CreatePost screen.
 */
sealed class CreatePostUiState {
    object Initial : CreatePostUiState()
    object Loading : CreatePostUiState()
    data class Success(val postId: String) : CreatePostUiState()
    data class Error(val message: String) : CreatePostUiState()
}

/**
 * ViewModel for the CreatePost screen.
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val uploadImageUseCase: UploadImageUseCase,
    private val userManager: UserManager,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Initial)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()
    
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()
    
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()
    
    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()
    
    private val _images = MutableStateFlow<List<PostImage>>(emptyList())
    val images: StateFlow<List<PostImage>> = _images.asStateFlow()
    
    private val _isDropdownExpanded = MutableStateFlow(false)
    val isDropdownExpanded: StateFlow<Boolean> = _isDropdownExpanded.asStateFlow()
    
    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()
    
    // Predefined categories with descriptions
    val categories = listOf(
        Category("General", "General discussions about your community"),
        Category("Events", "Local events and gatherings"),
        Category("News", "Local news and updates"),
        Category("Questions", "Ask questions about your community"),
        Category("Recommendations", "Ask for or share recommendations"),
        Category("Lost & Found", "Report lost or found items"),
        Category("Community", "Community initiatives and activities"),
        Category("Business", "Local business updates and promotions"),
        Category("Jobs", "Local job opportunities"),
        Category("Housing", "Housing-related discussions")
    )
    
    init {
        loadUserInfo()
    }
    
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                userManager.getCurrentUser()?.let { user ->
                    _userInfo.value = user
                }
            } catch (e: Exception) {
                _uiState.value = CreatePostUiState.Error("Failed to load user info")
            }
        }
    }
    
    fun onTitleChanged(newTitle: String) {
        if (newTitle.length <= PostValidation.MAX_TITLE_LENGTH) {
            _title.value = newTitle
        }
    }
    
    fun onContentChanged(newContent: String) {
        if (newContent.length <= PostValidation.MAX_CONTENT_LENGTH) {
            _content.value = newContent
        }
    }
    
    fun onCategoryChanged(newCategory: String) {
        _category.value = newCategory
        _isDropdownExpanded.value = false
    }
    
    fun onDropdownExpandedChange(expanded: Boolean) {
        _isDropdownExpanded.value = expanded
    }
    
    fun onImageSelected(uri: Uri, size: Long, type: String) {
        viewModelScope.launch {
            try {
                // Validate image count
                if (_images.value.size >= PostValidation.MAX_IMAGES) {
                    _uiState.value = CreatePostUiState.Error("Maximum number of images (${PostValidation.MAX_IMAGES}) reached")
                    return@launch
                }

                // Validate image size
                if (size > PostValidation.MAX_IMAGE_SIZE_MB * 1024 * 1024) {
                    _uiState.value = CreatePostUiState.Error("Image size exceeds maximum limit of ${PostValidation.MAX_IMAGE_SIZE_MB}MB")
                    return@launch
                }

                // Validate image type
                if (!type.startsWith("image/")) {
                    _uiState.value = CreatePostUiState.Error("Only image files are allowed")
                    return@launch
                }

                val newImage = PostImage(
                    id = UUID.randomUUID().toString(),
                    uri = uri.toString(),
                    size = size,
                    type = type,
                    uploadStatus = UploadStatus.Pending
                )

                _images.value = _images.value + newImage
            } catch (e: Exception) {
                _uiState.value = CreatePostUiState.Error("Failed to process image: ${e.message}")
            }
        }
    }
    
    fun removeImage(imageId: String) {
        _images.value = _images.value.filter { it.id != imageId }
    }
    
    fun createPost() {
        viewModelScope.launch {
            try {
                _uiState.value = CreatePostUiState.Loading

                // Validate content length
                if (_content.value.length < PostValidation.MIN_CONTENT_LENGTH) {
                    throw ValidationException("Post content must be at least ${PostValidation.MIN_CONTENT_LENGTH} characters")
                }

                if (_content.value.length > PostValidation.MAX_CONTENT_LENGTH) {
                    throw ValidationException("Post content cannot exceed ${PostValidation.MAX_CONTENT_LENGTH} characters")
                }

                // Validate category
                if (_category.value.isBlank()) {
                    throw ValidationException("Please select a category")
                }

                // Upload images first
                val uploadedImages = mutableListOf<PostImage>()
                for (image in _images.value) {
                    try {
                        // Update status to uploading
                        _images.value = _images.value.map { 
                            if (it.id == image.id) it.copy(uploadStatus = UploadStatus.Uploading)
                            else it
                        }

                        // Upload image
                        val result = uploadImageUseCase(Uri.parse(image.uri))
                        when (result) {
                            is Result.Success -> {
                                // Update status to success
                                val updatedImage = image.copy(
                                    uploadStatus = UploadStatus.Success,
                                    downloadUrl = result.data
                                )
                                uploadedImages.add(updatedImage)
                                
                                _images.value = _images.value.map { 
                                    if (it.id == image.id) updatedImage
                                    else it
                                }
                            }
                            is Result.Error -> {
                                // Update status to error
                                _images.value = _images.value.map { 
                                    if (it.id == image.id) it.copy(
                                        uploadStatus = UploadStatus.Error(result.exception.message ?: "Failed to upload image")
                                    )
                                    else it
                                }
                                throw ValidationException("Failed to upload image: ${result.exception.message}")
                            }
                        }
                    } catch (e: Exception) {
                        // Update status to error
                        _images.value = _images.value.map { 
                            if (it.id == image.id) it.copy(
                                uploadStatus = UploadStatus.Error(e.message ?: "Failed to upload image")
                            )
                            else it
                        }
                        throw ValidationException("Failed to upload image: ${e.message}")
                    }
                }

                // Create post with uploaded images
                val post = Post(
                    id = UUID.randomUUID().toString(),
                    title = _title.value,
                    content = _content.value,
                    authorId = userManager.getCurrentUserId()?:"",
                    authorName = auth.currentUser?.displayName ?: "Anonymous",
                    timestamp = System.currentTimeMillis(),
                    likes = emptyList(),
                    comments = 0,
                    mediaUrls = uploadedImages.map { it.downloadUrl ?: "" },
                    category = _category.value,
                    city = userManager.getCurrentUser()?.city?:"Pune"
                )

                val result = postRepository.createPost(post)
                when (result) {
                    is Result.Success -> {
                        _uiState.value = CreatePostUiState.Success(result.data)
                        resetState()
                    }
                    is Result.Error -> {
                        _uiState.value = CreatePostUiState.Error(result.exception.message ?: "Failed to create post")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CreatePostUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    fun resetState() {
        _title.value = ""
        _content.value = ""
        _category.value = ""
        _images.value = emptyList()
        _isDropdownExpanded.value = false
        _uiState.value = CreatePostUiState.Initial
    }
    
    data class Category(
        val id: String,
        val description: String
    )
    
    companion object {
        private const val AUTO_SAVE_DELAY = 3000L // 3 seconds
    }
} 