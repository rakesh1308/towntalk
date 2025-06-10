package com.pixelsface.towntalk.features.chat.ui.chatlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.pixelsface.towntalk.features.chat.domain.model.ChatConversation
import com.pixelsface.towntalk.features.chat.domain.model.ParticipantInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    onNavigateToChat: (chatId: String, otherUserId: String, otherUserName: String?, otherUserPhotoUrl: String?) -> Unit,
    onNavigateToUserSelection: () -> Unit,
    currentUserId: String? // Pass current user ID for determining other participant
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Implement Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search Chats")
                    }
                    IconButton(onClick = { /* TODO: Implement Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToUserSelection) {
                Icon(Icons.Filled.Message, contentDescription = "New Chat")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is ChatListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ChatListUiState.Success -> {
                    if (state.conversations.isEmpty()) {
                        Text("No chats yet. Start a new conversation!", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.conversations, key = { it.id }) {
                                conversation ->
                                val otherParticipant = conversation.participantDetails.values.firstOrNull { it.userId != currentUserId }
                                ChatItem(
                                    conversation = conversation,
                                    otherParticipant = otherParticipant,
                                    currentUserId = currentUserId,
                                    onItemClick = {
                                        otherParticipant?.let {
                                            onNavigateToChat(conversation.id, it.userId, it.name, it.photoUrl)
                                        }
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
                is ChatListUiState.Empty -> {
                     Text("No chats yet. Start a new conversation!", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                }
                is ChatListUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center).padding(16.dp), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added OptIn for Badge
@Composable
fun ChatItem(
    conversation: ChatConversation,
    otherParticipant: ParticipantInfo?,
    currentUserId: String?,
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
            model = otherParticipant?.photoUrl,
            loading = {
                Icon(Icons.Filled.Person, contentDescription = "Loading image", modifier = Modifier.size(56.dp).clip(CircleShape))
            },
            error = {
                Icon(Icons.Filled.Person, contentDescription = otherParticipant?.name ?: "User", modifier = Modifier.size(56.dp).clip(CircleShape))
            },
            contentDescription = otherParticipant?.name ?: "User profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = otherParticipant?.name ?: "Unknown User",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.lastMessage?.text ?: "No messages yet",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = if ((conversation.unreadCount[currentUserId] ?: 0) > 0) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = conversation.lastMessage?.timestamp?.let { formatChatListTimestamp(it) } ?: "",
                fontSize = 12.sp,
                color = if ((conversation.unreadCount[currentUserId] ?: 0) > 0) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            val unreadCount = conversation.unreadCount[currentUserId] ?: 0
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatChatListTimestamp(timestamp: Long): String {
    val messageDate = Date(timestamp)
    val now = Date()

    val sdfToday = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val sdfYesterday = SimpleDateFormat("'Yesterday'", Locale.getDefault())
    val sdfDefault = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    val calMessage = java.util.Calendar.getInstance().apply { time = messageDate }
    val calNow = java.util.Calendar.getInstance().apply { time = now }

    return when {
        calMessage.get(java.util.Calendar.YEAR) == calNow.get(java.util.Calendar.YEAR) &&
                calMessage.get(java.util.Calendar.DAY_OF_YEAR) == calNow.get(java.util.Calendar.DAY_OF_YEAR) -> {
            sdfToday.format(messageDate)
        }
        calMessage.get(java.util.Calendar.YEAR) == calNow.get(java.util.Calendar.YEAR) &&
                calMessage.get(java.util.Calendar.DAY_OF_YEAR) == calNow.get(java.util.Calendar.DAY_OF_YEAR) - 1 -> {
            sdfYesterday.format(messageDate)
        }
        else -> {
            sdfDefault.format(messageDate)
        }
    }
}