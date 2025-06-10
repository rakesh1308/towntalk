package com.pixelsface.towntalk.features.profile.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.domain.model.User
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.use_case.GetCurrentUserUseCase
import com.pixelsface.towntalk.features.profile.domain.use_case.UpdateUserProfileUseCase
import com.pixelsface.towntalk.features.profile.domain.use_case.UpdateProfileImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the edit profile screen.
 * Handles loading and updating the user's profile information.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateProfileImageUseCase: UpdateProfileImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    /**
     * Loads the current user's profile.
     */
    internal fun loadCurrentUser() {
        getCurrentUserUseCase()
            .onEach { profileUser ->
                _uiState.update { it.copy(
                    profileUser = profileUser,
                    name = profileUser.user.name,
                    username = profileUser.user.username,
                    bio = profileUser.user.bio ?: "",
                    phoneNumber = profileUser.user.phoneNumber ?: "",
                    city = profileUser.user.city,
                    isLoading = false,
                    error = null
                ) }
            }
            .catch { error ->
                _uiState.update { it.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load profile"
                ) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Validates all fields and returns true if all are valid
     */
    private fun validateFields(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        // Name validation
        if (_uiState.value.name.isBlank()) {
            errors["name"] = "Name is required"
        } else if (_uiState.value.name.length < 2) {
            errors["name"] = "Name must be at least 2 characters"
        }
        
        // Username validation
        if (_uiState.value.username.isBlank()) {
            errors["username"] = "Username is required"
        } else if (!_uiState.value.username.matches(Regex("^[a-zA-Z0-9_]{3,20}$"))) {
            errors["username"] = "Username must be 3-20 characters and contain only letters, numbers, and underscores"
        }
        
        // Bio validation
        if (_uiState.value.bio.isBlank()) {
            errors["bio"] = "Bio is required"
        } else if (_uiState.value.bio.length > 160) {
            errors["bio"] = "Bio must be less than 160 characters"
        }
        
        // Phone number validation
        if (_uiState.value.phoneNumber.isBlank()) {
            errors["phoneNumber"] = "Phone number is required"
        } else if (!_uiState.value.phoneNumber.matches(Regex("^\\+?[0-9]{10,15}$"))) {
            errors["phoneNumber"] = "Please enter a valid phone number"
        }
        
        // City validation
        if (_uiState.value.city.isBlank()) {
            errors["city"] = "City is required"
        }

        _uiState.update { it.copy(
            validationErrors = errors,
            isSaveEnabled = errors.isEmpty()
        ) }
        
        return errors.isEmpty()
    }

    /**
     * Sets the name field value and validates it.
     *
     * @param name The new name value
     */
    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
        validateFields()
    }

    /**
     * Sets the username field value and validates it.
     *
     * @param username The new username value
     */
    fun setUsername(username: String) {
        _uiState.update { it.copy(username = username) }
        validateFields()
    }

    /**
     * Sets the bio field value and validates it.
     *
     * @param bio The new bio value
     */
    fun setBio(bio: String) {
        _uiState.update { it.copy(bio = bio) }
        validateFields()
    }

    /**
     * Sets the phone number field value and validates it.
     *
     * @param phoneNumber The new phone number value
     */
    fun setPhoneNumber(phoneNumber: String) {
        _uiState.update { it.copy(phoneNumber = phoneNumber) }
        validateFields()
    }

    /**
     * Sets the city field value and validates it.
     *
     * @param city The new city value
     */
    fun setCity(city: String) {
        _uiState.update { it.copy(city = city) }
        validateFields()
    }

    /**
     * Sets the selected image URI.
     *
     * @param uri The URI of the selected image
     */
    fun setSelectedImageUri(uri: Uri) {
        _uiState.update { it.copy(
            selectedImageUri = uri,
            hasImageSelected = true
        ) }
    }

    /**
     * Updates the user's profile with new information.
     * Validates all fields before updating.
     */
    fun updateProfile() {
        if (!validateFields()) {
            return
        }

        val currentUser = _uiState.value.profileUser?.user
        if (currentUser != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                try {
                    // First update the profile image if one is selected
                    _uiState.value.selectedImageUri?.let { uri ->
                        updateProfileImageUseCase(currentUser.id, uri)
                            .collect { _ -> /* Image update successful */ }
                    }
                    
                    // Then update the profile information
                    updateUserProfileUseCase(
                        userId = currentUser.id,
                        name = _uiState.value.name,
                        username = _uiState.value.username,
                        bio = _uiState.value.bio,
                        phoneNumber = _uiState.value.phoneNumber,
                        city = _uiState.value.city
                    ).collect { updatedProfileUser ->
                        _uiState.update { it.copy(
                            profileUser = updatedProfileUser,
                            isLoading = false,
                            error = null,
                            isProfileUpdated = true
                        ) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update profile"
                    ) }
                }
            }
        } else {
            _uiState.update { it.copy(
                error = "User not found"
            ) }
        }
    }

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the edit profile screen.
 */
data class EditProfileUiState(
    val profileUser: ProfileUser? = null,
    val name: String = "",
    val username: String = "",
    val bio: String = "",
    val phoneNumber: String = "",
    val city: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasImageSelected: Boolean = false,
    val isProfileUpdated: Boolean = false,
    val selectedImageUri: Uri? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isSaveEnabled: Boolean = false
) 