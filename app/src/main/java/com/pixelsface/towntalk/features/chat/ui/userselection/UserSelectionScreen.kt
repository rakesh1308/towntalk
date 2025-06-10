package com.pixelsface.towntalk.features.chat.ui.userselection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.pixelsface.towntalk.features.auth.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionScreen(
    viewModel: UserSelectionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToChat: (chatId: String, otherUserId: String, otherUserName: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigationState by viewModel.chatNavigationState.collectAsState()

    LaunchedEffect(navigationState) {
        if (navigationState is ChatNavigationState.NavigateToChat) {
            val navArgs = navigationState as ChatNavigationState.NavigateToChat
            onNavigateToChat(navArgs.chatId, navArgs.otherUserId, navArgs.otherUserName)
            viewModel.navigationCompleted() // Reset navigation state
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start a new chat") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is UserSelectionUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UserSelectionUiState.Success -> {
                    if (state.users.isEmpty()) {
                        Text("No users found to start a chat with.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.users, key = { it.id }) { user ->
                                UserSelectItem(user = user, onItemClick = { viewModel.onUserSelected(user) })
                                Divider()
                            }
                        }
                    }
                }
                is UserSelectionUiState.Empty -> {
                    Text("No other users available to chat.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                }
                is UserSelectionUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center).padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
            // Handle chat creation error from navigationState
            if (navigationState is ChatNavigationState.Error) {
                // You might want to show a Snackbar or a Dialog here
                Text(
                    text = (navigationState as ChatNavigationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun UserSelectItem(
    user: User,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = user.photoUrl,
            loading = {
                 Icon(Icons.Filled.Person, contentDescription = "Loading image", modifier = Modifier.size(48.dp).clip(CircleShape))
            },
            error = {
                 Icon(Icons.Filled.Person, contentDescription = user.name ?: "User", modifier = Modifier.size(48.dp).clip(CircleShape))
            },
            contentDescription = (user.name ?: "User") + " profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = user.name ?: "Unknown User",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
 