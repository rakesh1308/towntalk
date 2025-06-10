@file:OptIn(ExperimentalMaterial3Api::class)

package com.pixelsface.towntalk.features.chat.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelsface.towntalk.R
import com.pixelsface.towntalk.features.chat.domain.model.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
    // chatId and otherUserName are passed via SavedStateHandle to ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val sendState by viewModel.sendState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val otherUserName by viewModel.otherUserName.collectAsState()
    val otherUserPhotoUrl by viewModel.otherUserPhotoUrl.collectAsState()
    val isOtherUserOnline by viewModel.otherUserIsOnline.collectAsState()
    val otherUserLastSeen by viewModel.otherUserLastSeen.collectAsState()
    val chatBackgroundColor by viewModel.chatBackgroundColor.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showColorPicker by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive or keyboard opens (conditionally)
    LaunchedEffect(uiState) {
        if (uiState is ChatUiState.Success) {
            val messages = (uiState as ChatUiState.Success).messages
            if (messages.isNotEmpty()) {
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }
    
    // Handle SendState (e.g., show a Snackbar on error, clear input on success)
    LaunchedEffect(sendState) {
        if (sendState is MessageSendState.Sent) {
            // Message sent, input is cleared in ViewModel
            viewModel.resetSendState() // Reset state to Idle
        } else if (sendState is MessageSendState.Error) {
            // Show error to user (e.g., Snackbar)
            // For now, just logging or simple text display can be placeholder
            // viewModel.resetSendState() // or keep error until user dismisses
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onColorSelected = { color ->
                viewModel.saveChatBackgroundColor(String.format("#%06X", (0xFFFFFF and color.toArgb())))
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    Scaffold(
        topBar = {
            ChatTopAppBar(
                otherUserName = otherUserName?.replace("+", " ") ?: "Chat",
                otherUserPhotoUrl = otherUserPhotoUrl,
                isOnline = isOtherUserOnline,
                lastSeen = otherUserLastSeen,
                onNavigateBack = onNavigateBack,
                onWallpaperClick = { showColorPicker = true }
            )
        },
        bottomBar = {
            MessageInputBar(
                message = messageInput,
                onMessageChange = viewModel::onMessageInputChange,
                onSendMessage = viewModel::sendMessage,
                isSending = sendState is MessageSendState.Sending
            )
        },
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(android.graphics.Color.parseColor(chatBackgroundColor)))
        ) {
            when (val state = uiState) {
                is ChatUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ChatUiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(message = message, isCurrentUserMessage = message.senderId == currentUserId)
                        }
                    }
                }
                is ChatUiState.Empty -> {
                    Text("No messages yet. Start the conversation!", modifier = Modifier.align(Alignment.Center))
                }
                is ChatUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ChatTopAppBar(
    otherUserName: String,
    otherUserPhotoUrl: String?,
    isOnline: Boolean,
    lastSeen: Long?,
    onNavigateBack: () -> Unit,
    onWallpaperClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = otherUserPhotoUrl,
                    contentDescription = "Profile Picture",
                    placeholder = painterResource(id = R.drawable.ic_default_profile),
                    error = painterResource(id = R.drawable.ic_default_profile),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = otherUserName, fontWeight = FontWeight.Bold)
                    if (isOnline) {
                        Text(text = "online", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        lastSeen?.let {
                            Text(text = formatLastSeen(it), fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Implement video call */ }) {
                Icon(Icons.Filled.Videocam, contentDescription = "Video Call")
            }
            IconButton(onClick = { /* TODO: Implement voice call */ }) {
                Icon(Icons.Outlined.Call, contentDescription = "Call")
            }
            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Wallpaper") },
                    onClick = {
                        onWallpaperClick()
                        showMenu = false
                    }
                )
            }
        }
    )
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUserMessage: Boolean
) {
    val alignment = if (isCurrentUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurrentUserMessage) Color(0xFFDCF8C6) else Color.White
    val shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUserMessage) 16.dp else 0.dp,
        bottomEnd = if (isCurrentUserMessage) 0.dp else 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .background(backgroundColor, shape)
                .padding(8.dp),
            horizontalAlignment = if (isCurrentUserMessage) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        FloatingActionButton(
            onClick = onSendMessage,
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatLastSeen(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        seconds < 60 -> "last seen just now"
        minutes < 60 -> "last seen $minutes minutes ago"
        hours < 24 -> "last seen $hours hours ago"
        days == 1L -> "last seen yesterday"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            "last seen on ${sdf.format(Date(timestamp))}"
        }
    }
}

@Composable
fun ColorPickerDialog(onColorSelected: (Color) -> Unit, onDismiss: () -> Unit) {
    val colors = listOf(
        Color(0xFFECE5DD), // Default
        Color(0xFFB4E7F0), // Light Blue
        Color(0xFFD3FFC4), // Light Green
        Color(0xFFFFDDC4), // Light Orange
        Color(0xFFFFC4C4), // Light Red
        Color(0xFFC4C4FF)  // Light Purple
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Choose a color", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onColorSelected(color) }
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
