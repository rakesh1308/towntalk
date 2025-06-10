package com.pixelsface.towntalk.features.profile.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelsface.towntalk.features.profile.presentation.components.AchievementsSection
import com.pixelsface.towntalk.features.profile.presentation.components.ActivitySection
import com.pixelsface.towntalk.features.profile.presentation.components.PostsSection
import com.pixelsface.towntalk.features.profile.presentation.components.ProfileHeader
import com.pixelsface.towntalk.features.profile.presentation.viewmodel.UserProfileViewModel

/**
 * User profile screen composable.
 * Displays the profile of another user.
 *
 * @param userId The ID of the user whose profile should be displayed
 * @param viewModel ViewModel for the user profile screen
 * @param onNavigateBack Callback for when the user navigates back
 * @param onPostClick Callback for when a post is clicked
 * @param onEditProfile Callback for when the user wants to edit their profile
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    viewModel: UserProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    onEditProfile: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Initialize the view model with the user ID
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }
    
    // Show error message in snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.profileUser?.user?.name ?: "User Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.profileUser == null) {
                Text(
                    text = "Failed to load profile",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Profile header
                    ProfileHeader(
                        profileUser = uiState.profileUser!!,
                        isCurrentUser = false,
                        onEditProfile = onEditProfile,
                        onLocationClick = {}
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Achievements section
                    AchievementsSection(
                        achievements = uiState.profileUser!!.achievements
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Activity section
                    ActivitySection(
                        activities = uiState.profileUser!!.recentActivity
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Posts section
                    PostsSection(
                        posts = uiState.profileUser!!.posts,
                        onPostClick = onPostClick
                    )
                }
            }
        }
    }
} 