package com.pixelsface.towntalk.features.auth.presentation.login

import android.content.Intent
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

sealed class LoginState {
    object Initial : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
    object PhoneVerificationSent : LoginState()
    data class LaunchGoogleSignIn(val intent: Intent) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Initial)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    private var verificationId: String? = null

    private fun formatPhoneNumber(phoneNumber: String): String {
        // Remove any non-digit characters except +
        val cleanedNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        // If the number already starts with +, return it as is
        if (cleanedNumber.startsWith("+")) {
            return cleanedNumber
        }
        
        // If the number starts with 91 (India country code), add +
        if (cleanedNumber.startsWith("91")) {
            return "+$cleanedNumber"
        }
        
        // For Indian numbers without country code, add +91
        return "+91$cleanedNumber"
    }

    fun onEmailSignIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                when (val result = loginUseCase(email, password)) {
                    is Result.Success -> {
                        _state.value = LoginState.Success(result.data)
                    }
                    is Result.Error -> {
                        _state.value = LoginState.Error(result.exception.message ?: "Login failed")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun onGoogleSignInClick() {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                when (val result = authRepository.getGoogleSignInIntent()) {
                    is Result.Success -> {
                        _state.value = LoginState.LaunchGoogleSignIn(result.data)
                    }
                    is Result.Error -> {
                        _state.value = LoginState.Error(result.exception.message ?: "Failed to start Google Sign-In")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                when (val result = loginUseCase.handleGoogleSignIn(data)) {
                    is Result.Success -> {
                        _state.value = LoginState.Success(result.data)
                    }
                    is Result.Error -> {
                        _state.value = LoginState.Error(result.exception.message ?: "Google sign in failed")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    fun onPhoneSignInClick(phoneNumber: String) {
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                val formattedNumber = formatPhoneNumber(phoneNumber)
                when (val result = loginUseCase.sendPhoneVerificationCode(formattedNumber)) {
                    is Result.Success -> {
                        verificationId = result.data
                        _state.value = LoginState.PhoneVerificationSent
                    }
                    is Result.Error -> {
                        _state.value = LoginState.Error(result.exception.message ?: "Failed to send verification code")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
    
    fun onVerifyPhoneCode(code: String) {
        val currentVerificationId = verificationId
        if (currentVerificationId == null) {
            _state.value = LoginState.Error("Verification ID not found. Please try again.")
            return
        }
        
        viewModelScope.launch {
            try {
                _state.value = LoginState.Loading
                when (val result = authRepository.verifyPhoneNumber(currentVerificationId, code)) {
                    is Result.Success -> {
                        _state.value = LoginState.Success(result.data)
                    }
                    is Result.Error -> {
                        _state.value = LoginState.Error(result.exception.message ?: "Failed to verify phone number")
                    }
                }
            } catch (e: Exception) {
                _state.value = LoginState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
} 