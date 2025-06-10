package com.pixelsface.towntalk.features.auth.presentation.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelsface.towntalk.features.auth.presentation.components.PhoneNumberInput
import com.pixelsface.towntalk.ui.theme.TownTalkColors

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var showVerificationCodeInput by remember { mutableStateOf(false) }
    var selectedAuthMethod by remember { mutableStateOf(AuthMethod.EMAIL) }
    var passwordVisible by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { intent ->
            viewModel.onGoogleSignInResult(intent)
        }
    }

    LaunchedEffect(state) {
        when (state) {
            is LoginState.LaunchGoogleSignIn -> {
                launcher.launch((state as LoginState.LaunchGoogleSignIn).intent)
            }
            is LoginState.Success -> {
                onNavigateToHome()
            }
            is LoginState.PhoneVerificationSent -> {
                showVerificationCodeInput = true
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Twitter-inspired background with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            TownTalkColors.Primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo and header section
            Spacer(modifier = Modifier.height(48.dp))
            
            // App logo or icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(TownTalkColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TT",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header text
            Text(
                text = "Sign in to TownTalk",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Connect with your local community",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Auth method selector - Twitter-inspired tab design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Email tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selectedAuthMethod = AuthMethod.EMAIL },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Email",
                            color = if (selectedAuthMethod == AuthMethod.EMAIL)
                                TownTalkColors.Primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selectedAuthMethod == AuthMethod.EMAIL) 
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Indicator line
                        Box(
                            modifier = Modifier
                                .width(if (selectedAuthMethod == AuthMethod.EMAIL) 40.dp else 0.dp)
                                .height(3.dp)
                                .background(
                                    color = TownTalkColors.Primary,
                                    shape = RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                }
                
                // Phone tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { selectedAuthMethod = AuthMethod.PHONE },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Phone",
                            color = if (selectedAuthMethod == AuthMethod.PHONE)
                                TownTalkColors.Primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (selectedAuthMethod == AuthMethod.PHONE) 
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Indicator line
                        Box(
                            modifier = Modifier
                                .width(if (selectedAuthMethod == AuthMethod.PHONE) 40.dp else 0.dp)
                                .height(3.dp)
                                .background(
                                    color = TownTalkColors.Primary,
                                    shape = RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auth form based on selected method with animation
            AnimatedVisibility(
                visible = selectedAuthMethod == AuthMethod.EMAIL,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                EmailLoginForm(
                    email = email,
                    password = password,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onSignIn = { viewModel.onEmailSignIn(email, password) },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    isLoading = state is LoginState.Loading
                )
            }

            AnimatedVisibility(
                visible = selectedAuthMethod == AuthMethod.PHONE,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                PhoneLoginForm(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    verificationCode = verificationCode,
                    onVerificationCodeChange = { verificationCode = it },
                    isVerificationSent = showVerificationCodeInput,
                    isError = state is LoginState.Error,
                    errorMessage = (state as? LoginState.Error)?.message ?: "",
                    onSendCode = { viewModel.onPhoneSignInClick(phoneNumber) },
                    onVerifyCode = { viewModel.onVerifyPhoneCode(verificationCode) },
                    isLoading = state is LoginState.Loading
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "or" text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign In button - Twitter-inspired
            OutlinedButton(
                onClick = { viewModel.onGoogleSignInClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                ),
                enabled = state !is LoginState.Loading
            ) {
                Text(
                    "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Create account link - Twitter-inspired
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(start = 4.dp)
                ) {
                    Text(
                        "Sign up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TownTalkColors.Primary
                        )
                    )
                }
            }

            // Error message
            if (state is LoginState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (state as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmailLoginForm(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TownTalkColors.Primary,
                focusedLabelColor = TownTalkColors.Primary,
                focusedLeadingIconColor = TownTalkColors.Primary
            ),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TownTalkColors.Primary,
                focusedLabelColor = TownTalkColors.Primary,
                focusedLeadingIconColor = TownTalkColors.Primary,
                focusedTrailingIconColor = TownTalkColors.Primary
            ),
            enabled = !isLoading
        )

        Button(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TownTalkColors.Primary,
                contentColor = TownTalkColors.OnPrimary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = TownTalkColors.OnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    "Sign In",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun PhoneLoginForm(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    isVerificationSent: Boolean,
    isError: Boolean,
    errorMessage: String,
    onSendCode: () -> Unit,
    onVerifyCode: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PhoneNumberInput(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            isError = isError,
            errorMessage = errorMessage,
            enabled = !isLoading
        )

        AnimatedVisibility(
            visible = isVerificationSent,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                label = { Text("Verification Code") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Verification Code",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TownTalkColors.Primary,
                    focusedLabelColor = TownTalkColors.Primary,
                    focusedLeadingIconColor = TownTalkColors.Primary
                ),
                enabled = !isLoading
            )
        }

        Button(
            onClick = if (isVerificationSent) onVerifyCode else onSendCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TownTalkColors.Primary,
                contentColor = TownTalkColors.OnPrimary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = TownTalkColors.OnPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    if (isVerificationSent) "Verify Code" else "Send Code",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

enum class AuthMethod {
    EMAIL, PHONE
} 