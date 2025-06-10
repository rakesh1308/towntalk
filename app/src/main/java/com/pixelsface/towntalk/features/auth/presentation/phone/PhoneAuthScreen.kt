package com.pixelsface.towntalk.features.auth.presentation.phone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelsface.towntalk.core.common.ui.FormField
import com.pixelsface.towntalk.features.auth.presentation.phone.PhoneAuthUiState.*

@Composable
fun PhoneAuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: PhoneAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is Success -> onAuthSuccess()
            is Error -> errorMessage = (uiState as Error).message
            else -> errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is Initial -> {
                PhoneNumberInput(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    onSendOtp = { viewModel.sendOtp(phoneNumber) },
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
            is OtpSent -> {
                OtpInput(
                    otpCode = otpCode,
                    onOtpChange = { otpCode = it },
                    onVerifyOtp = { viewModel.verifyOtp((uiState as OtpSent).verificationId, otpCode) },
                    onResendOtp = { viewModel.resetState() },
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
            is Loading -> {
                LoadingState()
            }
            else -> { /* Success and Error states are handled by LaunchedEffect */ }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PhoneNumberInput(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter your phone number",
            style = MaterialTheme.typography.headlineSmall
        )
        
        FormField(
            value = phoneNumber,
            onValueChange = { value ->
                // Only allow digits and '+' symbol
                if (value.isEmpty() || value.all { it.isDigit() || it == '+' }) {
                    onPhoneNumberChange(value)
                }
            },
            label = "Phone Number",
            placeholder = "+1234567890",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = isError,
            errorMessage = errorMessage,
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = onSendOtp,
            enabled = phoneNumber.length >= 10,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Verification Code")
        }
    }
}

@Composable
private fun OtpInput(
    otpCode: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter verification code",
            style = MaterialTheme.typography.headlineSmall
        )
        
        FormField(
            value = otpCode,
            onValueChange = { value ->
                // Only allow digits and limit to 6 characters
                if (value.all { it.isDigit() } && value.length <= 6) {
                    onOtpChange(value)
                }
            },
            label = "Verification Code",
            placeholder = "123456",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = isError,
            errorMessage = errorMessage,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onVerifyOtp,
                enabled = otpCode.length == 6,
                modifier = Modifier.weight(1f)
            ) {
                Text("Verify")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            TextButton(
                onClick = onResendOtp,
                modifier = Modifier.weight(1f)
            ) {
                Text("Resend Code")
            }
        }
    }
} 