package com.pixelsface.towntalk.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.features.profile.domain.model.ProfileUser
import com.pixelsface.towntalk.features.profile.domain.use_case.GetUserProfileUseCase
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
 * ViewModel for the user profile screen.
 * Handles loading and displaying another user's profile.
 */
@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    /**
     * Loads the user profile for the specified user ID.
     *
     * @param userId The ID of the user whose profile should be loaded
     */
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            getUserProfileUseCase(userId)
                .onEach { user ->
                    _uiState.update { it.copy(
                        profileUser = user,
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
    }

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the user profile screen.
 */
data class UserProfileUiState(
    val profileUser: ProfileUser? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) 