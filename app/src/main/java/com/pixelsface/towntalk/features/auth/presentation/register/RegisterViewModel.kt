package com.pixelsface.towntalk.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Initial)
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _state.value = RegisterState.Error("Passwords do not match")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = RegisterState.Loading
                when (val result = registerUseCase(email, password, email.substringBefore("@"))) {
                    is Result.Success -> {
                        _state.value = RegisterState.Success(result.data)
                    }
                    is Result.Error -> {
                        _state.value = RegisterState.Error(result.exception.message ?: "Registration failed")
                    }
                }
            } catch (e: Exception) {
                _state.value = RegisterState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}

sealed class RegisterState {
    object Initial : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
} 