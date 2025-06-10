package com.pixelsface.towntalk.features.auth.presentation.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhoneAuthUiState>(PhoneAuthUiState.Initial)
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()

    fun sendOtp(phoneNumber: String) {
        if (!isValidPhoneNumber(phoneNumber)) {
            _uiState.value = PhoneAuthUiState.Error("Please enter a valid phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = PhoneAuthUiState.Loading
            authRepository.sendPhoneVerificationCode(phoneNumber)
                .onSuccess { verificationId ->
                    _uiState.value = PhoneAuthUiState.OtpSent(verificationId)
                }
                .onError { exception ->
                    _uiState.value = PhoneAuthUiState.Error(exception.message ?: "Failed to send OTP")
                }
        }
    }

    fun verifyOtp(verificationId: String, code: String) {
        if (!isValidOtpCode(code)) {
            _uiState.value = PhoneAuthUiState.Error("Please enter a valid OTP code")
            return
        }

        viewModelScope.launch {
            _uiState.value = PhoneAuthUiState.Loading
            authRepository.verifyPhoneNumber(verificationId, code)
                .onSuccess { user ->
                    _uiState.value = PhoneAuthUiState.Success(user)
                }
                .onError { exception ->
                    _uiState.value = PhoneAuthUiState.Error(exception.message ?: "Failed to verify OTP")
                }
        }
    }

    fun resetState() {
        _uiState.value = PhoneAuthUiState.Initial
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic phone number validation
        return phoneNumber.length >= 10 && phoneNumber.all { it.isDigit() || it == '+' }
    }

    private fun isValidOtpCode(code: String): Boolean {
        // Basic OTP validation (assuming 6-digit OTP)
        return code.length == 6 && code.all { it.isDigit() }
    }
}

sealed class PhoneAuthUiState {
    object Initial : PhoneAuthUiState()
    object Loading : PhoneAuthUiState()
    data class OtpSent(val verificationId: String) : PhoneAuthUiState()
    data class Success(val user: User) : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
} 