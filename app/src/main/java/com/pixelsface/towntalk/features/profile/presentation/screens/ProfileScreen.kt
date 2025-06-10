package com.pixelsface.towntalk.features.profile.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelsface.towntalk.features.profile.presentation.components.AchievementsSection
import com.pixelsface.towntalk.features.profile.presentation.components.ActivitySection
import com.pixelsface.towntalk.features.profile.presentation.components.ProfileHeader
import com.pixelsface.towntalk.features.profile.presentation.components.ProfilePostsSection
import com.pixelsface.towntalk.features.profile.presentation.viewmodel.ProfileViewModel

/**
 * Main profile screen composable.
 * Displays the user's profile information, achievements, activity, and posts.
 *
 * @param userId User ID of the profile to display
 * @param onNavigateBack Callback for when the back button is clicked
 * @param onEditProfile Callback for when the edit profile button is clicked
 * @param onPostClick Callback for when a post is clicked
 * @param onLogout Callback for when the logout button is clicked
 * @param onLocationClick Callback for when the location button is clicked
 * @param viewModel ViewModel for the profile screen
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onEditProfile: () -> Unit,
    onPostClick: (String) -> Unit,
    onLogout: () -> Unit,
    onLocationClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = userId) {
        viewModel.loadProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when {
                            uiState.isLoading -> "Loading..."
                            uiState.error != null -> "Error"
                            uiState.profileUser != null -> uiState.profileUser?.user?.name ?: ""
                            else -> "Profile"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState.isCurrentUser) {
                        IconButton(onClick = onEditProfile) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = Triple(uiState.isLoading, uiState.error, uiState.profileUser),
                transitionSpec = {
                    fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                }
            ) { (isLoading, error, profileUser) ->
                when {
                    isLoading -> {
                        LoadingView()
                    }
                    error != null -> {
                        ErrorView(
                            error = error,
                            onRetry = { viewModel.loadProfile(userId) }
                        )
                    }
                    profileUser != null -> {
                        ProfileContent(
                            profileUser = profileUser,
                            isCurrentUser = uiState.isCurrentUser,
                            onEditProfile = onEditProfile,
                            onPostClick = onPostClick,
                            onLocationClick = onLocationClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
    }
}

@Composable
private fun ErrorView(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun ProfileContent(
    profileUser: com.pixelsface.towntalk.features.profile.domain.model.ProfileUser,
    isCurrentUser: Boolean,
    onEditProfile: () -> Unit,
    onPostClick: (String) -> Unit,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfileHeader(
                profileUser = profileUser,
                isCurrentUser = isCurrentUser,
                onEditProfile = onEditProfile,
                onLocationClick = onLocationClick,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            AchievementsSection(
                achievements = profileUser.achievements,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            ActivitySection(
                activities = profileUser.recentActivity,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            ProfilePostsSection(
                posts = profileUser.posts,
                onPostClick = onPostClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
} 