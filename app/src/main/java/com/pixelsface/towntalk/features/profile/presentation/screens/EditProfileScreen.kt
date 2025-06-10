package com.pixelsface.towntalk.features.profile.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File
import androidx.core.content.FileProvider
import com.pixelsface.towntalk.features.profile.presentation.viewmodel.EditProfileViewModel
import com.pixelsface.towntalk.core.utils.rememberPermission
import com.pixelsface.towntalk.R
import com.pixelsface.towntalk.core.utils.ComposeFileProvider
import android.Manifest

/**
 * Edit profile screen composable.
 * Allows the user to edit their profile information.
 *
 * @param viewModel ViewModel for the edit profile screen
 * @param onNavigateBack Callback for when the user navigates back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showImagePickerDialog by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { viewModel.setSelectedImageUri(it) }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                // The URI is already available in the ViewModel from where it was created
            }
        }
    )
    
    val cameraPermissionLauncher = rememberPermission(Manifest.permission.CAMERA) { granted ->
        if (granted) {
            val uri = ComposeFileProvider.getImageUri(context)
            viewModel.setSelectedImageUri(uri)
            cameraLauncher.launch(uri)
        } else {
            // Handle permission denial
        }
    }

    val galleryPermissionLauncher = rememberPermission(Manifest.permission.READ_MEDIA_IMAGES) { granted ->
        if (granted) {
            imagePicker.launch("image/*")
        } else {
            // Handle permission denial
        }
    }
    
    // Show error message in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    // Handle profile update success
    LaunchedEffect(uiState.isProfileUpdated) {
        if (uiState.isProfileUpdated) {
            snackbarHostState.showSnackbar(
                message = "Profile updated successfully",
                duration = SnackbarDuration.Short
            )
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile photo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { showImagePickerDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.selectedImageUri != null || uiState.profileUser?.user?.photoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(uiState.selectedImageUri ?: uiState.profileUser?.user?.photoUrl)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            TextButton(onClick = { showImagePickerDialog = true }) {
                Text("Change profile photo")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.setName(it) },
                label = { Text("Name*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                },
                isError = uiState.validationErrors["name"] != null,
                supportingText = {
                    uiState.validationErrors["name"]?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username field
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.setUsername(it) },
                label = { Text("Username*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AlternateEmail,
                        contentDescription = null
                    )
                },
                isError = uiState.validationErrors["username"] != null,
                supportingText = {
                    uiState.validationErrors["username"]?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone number field
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = { viewModel.setPhoneNumber(it) },
                label = { Text("Phone Number*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null
                    )
                },
                isError = uiState.validationErrors["phoneNumber"] != null,
                supportingText = {
                    uiState.validationErrors["phoneNumber"]?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // City field
            OutlinedTextField(
                value = uiState.city,
                onValueChange = { viewModel.setCity(it) },
                label = { Text("City*") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null
                    )
                },
                isError = uiState.validationErrors["city"] != null,
                supportingText = {
                    uiState.validationErrors["city"]?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bio field
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = { viewModel.setBio(it) },
                label = { Text("Bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = { viewModel.updateProfile() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                AnimatedContent(
                    targetState = uiState.isLoading,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(150)) togetherWith
                                fadeOut(animationSpec = tween(150))
                    }, label = ""
                ) { isLoading ->
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Image picker dialog
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismissRequest = { showImagePickerDialog = false },
            onGalleryClick = {
                showImagePickerDialog = false
                galleryPermissionLauncher()
            },
            onCameraClick = {
                showImagePickerDialog = false
                cameraPermissionLauncher()
            }
        )
    }
}

@Composable
private fun ImagePickerDialog(
    onDismissRequest: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Change profile photo") },
        text = { Text("Choose an option to update your profile photo.") },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        dismissButton = {
            Column {
                TextButton(onClick = onGalleryClick) {
                    Text("Choose from gallery")
                }
                TextButton(onClick = onCameraClick) {
                    Text("Take a photo")
                }
            }
        }
    )
} 