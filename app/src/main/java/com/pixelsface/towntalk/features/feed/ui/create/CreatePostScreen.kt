package com.pixelsface.towntalk.features.feed.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelsface.towntalk.features.feed.domain.model.UploadStatus
import com.pixelsface.towntalk.features.feed.domain.validation.PostValidation
import com.pixelsface.towntalk.ui.theme.TownTalkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPostCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val category by viewModel.category.collectAsState()
    val images by viewModel.images.collectAsState()
    val isDropdownExpanded by viewModel.isDropdownExpanded.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val contentResolver = context.contentResolver
            val size = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
            val type = contentResolver.getType(uri) ?: "image/*"
            viewModel.onImageSelected(uri, size, type)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            TownTalkColors.Primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background
        )
    )

    LaunchedEffect(uiState) {
        if (uiState is CreatePostUiState.Success) {
            val postId = (uiState as CreatePostUiState.Success).postId
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onPostCreated(postId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Create Post",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNavigateBack()
                            }
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.createPost() 
                            },
                            enabled = (title.isNotBlank() || content.isNotBlank()) && 
                                    category.isNotBlank() &&
                                    uiState !is CreatePostUiState.Loading &&
                                    images.none { it.uploadStatus is UploadStatus.Uploading },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TownTalkColors.Primary,
                                contentColor = TownTalkColors.OnPrimary,
                                disabledContainerColor = TownTalkColors.Primary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            if (uiState is CreatePostUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(
                                    "Post",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Profile section with animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = userInfo?.photoUrl ?: "https://ui-avatars.com/api/?name=${userInfo?.name?.replace(" ", "+") ?: "Anonymous"}&background=random",
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userInfo?.name ?: "Anonymous",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Posting to ${userInfo?.city ?: "your community"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Title input with animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = title,
                                onValueChange = viewModel::onTitleChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Title (optional)") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TownTalkColors.Primary,
                                    focusedLabelColor = TownTalkColors.Primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Title,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            
                            AnimatedVisibility(
                                visible = title.isNotBlank(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Text(
                                    text = "${title.length}/${PostValidation.MAX_TITLE_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (title.length > PostValidation.MAX_TITLE_LENGTH * 0.9) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Content input with animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = content,
                                onValueChange = viewModel::onContentChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                placeholder = { Text("What's happening in your community?") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TownTalkColors.Primary,
                                    focusedLabelColor = TownTalkColors.Primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            
                            AnimatedVisibility(
                                visible = content.isNotBlank(),
                                enter = fadeIn() + slideInVertically(),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (content.length < PostValidation.MIN_CONTENT_LENGTH)
                                            "At least ${PostValidation.MIN_CONTENT_LENGTH} characters needed"
                                        else
                                            "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "${content.length}/${PostValidation.MAX_CONTENT_LENGTH}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (content.length > PostValidation.MAX_CONTENT_LENGTH * 0.9) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category dropdown with animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = viewModel::onDropdownExpandedChange
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                placeholder = { Text("Select a category") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TownTalkColors.Primary,
                                    focusedLabelColor = TownTalkColors.Primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isDropdownExpanded
                                    )
                                }
                            )
                            
                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { viewModel.onDropdownExpandedChange(false) }
                            ) {
                                viewModel.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { 
                                            Column {
                                                Text(category.id)
                                                Text(
                                                    category.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onCategoryChanged(category.id)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Image selection with animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = "Add Images",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(images) { image ->
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = image.uri,
                                            contentDescription = "Selected image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Upload status overlay
                                        when (image.uploadStatus) {
                                            is UploadStatus.Uploading -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Black.copy(alpha = 0.5f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(24.dp),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            is UploadStatus.Error -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Red.copy(alpha = 0.5f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Error,
                                                        contentDescription = "Upload failed",
                                                        tint = MaterialTheme.colorScheme.onError
                                                    )
                                                }
                                            }
                                            else -> {}
                                        }
                                        
                                        // Remove button
                                        IconButton(
                                            onClick = { 
                                                viewModel.removeImage(image.id)
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove image",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                                
                                // Add image button if under limit
                                if (images.size < PostValidation.MAX_IMAGES) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { 
                                                    imagePicker.launch("image/*")
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Add image",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    "${images.size}/${PostValidation.MAX_IMAGES}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Error snackbar
                AnimatedVisibility(
                    visible = uiState is CreatePostUiState.Error,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(
                                onClick = { 
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.resetState() 
                                }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text((uiState as? CreatePostUiState.Error)?.message ?: "")
                    }
                }
            }
        }
    }
} 