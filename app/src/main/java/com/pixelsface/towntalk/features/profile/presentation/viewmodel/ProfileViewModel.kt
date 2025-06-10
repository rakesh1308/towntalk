package com.pixelsface.towntalk.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.use_case.DeleteUserAccountUseCase
import com.pixelsface.towntalk.features.profile.domain.use_case.GetCurrentUserUseCase
import com.pixelsface.towntalk.features.profile.domain.use_case.GetUserByIdUseCase
import com.pixelsface.towntalk.features.profile.domain.use_case.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the profile screen.
 * Handles loading and managing the user's profile data.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val deleteUserAccountUseCase: DeleteUserAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { profileUser ->
                currentUserId = profileUser.user.id
            }
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                getUserByIdUseCase(userId).collect { profileUser ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            profileUser = profileUser,
                            isCurrentUser = userId == currentUserId,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load profile"
                    )
                }
            }
        }
    }

    /**
     * Updates the user's profile with new information.
     *
     * @param name New name for the user
     * @param bio New bio for the user
     */
    fun updateProfile( userId: String,
                       name: String,
                       username: String,
                       bio: String,
                       phoneNumber: String,
                       city: String) {
        val currentUserId = currentUserId
        if (currentUserId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                try {
                    updateUserProfileUseCase(currentUserId, name, username, bio, phoneNumber, city).collect { profileUser ->
                        _uiState.update { it.copy(
                            profileUser = profileUser,
                            isLoading = false,
                            error = null
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
                error = "User ID not found"
            ) }
        }
    }

    /**
     * Deletes the user's account.
     */
    fun deleteAccount() {
        val currentUserId = currentUserId
        if (currentUserId != null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                try {
                    deleteUserAccountUseCase(currentUserId).collect { success ->
                        _uiState.update { it.copy(
                            isAccountDeleted = success,
                            isLoading = false,
                            error = if (!success) "Failed to delete account" else null
                        ) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete account"
                    ) }
                }
            }
        } else {
            _uiState.update { it.copy(
                error = "User ID not found"
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
 * UI state for the profile screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val profileUser: ProfileUser? = null,
    val isCurrentUser: Boolean = false,
    val error: String? = null,
    val isAccountDeleted: Boolean = false
) 