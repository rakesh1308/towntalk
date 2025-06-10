package com.pixelsface.towntalk.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.auth.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Initial)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = AuthState.Loading
                when (val result = loginUseCase(email, password)) {
                    is Result.Success -> {
                        _state.value = AuthState.Success(result.data)
                    }
                    is Result.Error -> {
                        _state.value = AuthState.Error(result.exception.message ?: "Login failed")
                    }
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _state.value = AuthState.Loading
                authRepository.signOut()
                _state.value = AuthState.Initial
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Failed to sign out")
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                _state.value = AuthState.Loading
                when (val result = authRepository.getCurrentUser()) {
                    is Result.Success -> {
                        result.data?.let { user ->
                            _state.value = AuthState.Success(user)
                        } ?: run {
                            _state.value = AuthState.Initial
                        }
                    }
                    is Result.Error -> {
                        _state.value = AuthState.Initial
                    }
                }
            } catch (e: Exception) {
                _state.value = AuthState.Initial
            }
        }
    }
} 