package com.pixelsface.towntalk.features.feed.ui.detail

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelsface.towntalk.core.common.ui.AppLoadingIndicator
import com.pixelsface.towntalk.core.ui.components.ErrorMessage
import com.pixelsface.towntalk.core.ui.components.LoadingIndicator
import com.pixelsface.towntalk.features.feed.domain.model.Comment
import com.pixelsface.towntalk.features.feed.domain.model.Post
import com.pixelsface.towntalk.ui.theme.TownTalkColors
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
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
        
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = "Post",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (uiState is PostDetailUiState.Success) {
                            val post = (uiState as PostDetailUiState.Success).post
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, post.title)
                                        putExtra(Intent.EXTRA_TEXT, buildShareText(post))
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                                    viewModel.sharePost()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    is PostDetailUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AppLoadingIndicator(
                                color = TownTalkColors.Primary,
                                size = 48.dp
                            )
                        }
                    }
                    is PostDetailUiState.Success -> {
                        val state = uiState as PostDetailUiState.Success
                        PostDetailContent(
                            post = state.post,
                            comments = state.comments,
                            isLiked = state.isLiked,
                            onLikeClick = { viewModel.likePost() },
                            onUnlikeClick = { viewModel.unlikePost() },
                            onAddComment = { content -> viewModel.addComment(content) }
                        )
                    }
                    is PostDetailUiState.Error -> {
                        val state = uiState as PostDetailUiState.Error
                        ErrorMessage(
                            message = state.message,
                            onRetry = { viewModel.loadPost() }
                        )
                    }
                }
            }
        }
    }
}

private fun buildShareText(post: Post): String {
    return buildString {
        appendLine(post.title)
        appendLine()
        appendLine(post.content)
        appendLine()
        appendLine("Posted by ${post.authorName} in ${post.city}")
        if (post.mediaUrls.isNotEmpty()) {
            appendLine()
            appendLine("Media: ${post.mediaUrls.first()}")
        }
        appendLine()
        appendLine("Shared via TownTalk")
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    comments: List<Comment>,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var isAddingComment by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PostHeader(post = post)
        }
        
        item {
            PostContent(post = post)
        }
        
        item {
            PostActions(
                post = post,
                isLiked = isLiked,
                onLikeClick = onLikeClick,
                onUnlikeClick = onUnlikeClick
            )
        }
        
        item {
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        }
        
        item {
            Text(
                text = "Comments (${comments.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
        
        item {
            CommentInput(
                value = commentText,
                onValueChange = { commentText = it },
                onSendClick = {
                    if (commentText.isNotBlank()) {
                        isAddingComment = true
                        onAddComment(commentText)
                        commentText = ""
                        isAddingComment = false
                    }
                },
                isAddingComment = isAddingComment
            )
        }
    }
}

@Composable
private fun PostHeader(post: Post) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                            modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TownTalkColors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.first().toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TownTalkColors.Primary
                    )
                }
                        
                        Column {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                    
                            Text(
                        text = formatTimestamp(post.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            IconButton(onClick = { /* TODO: Show post options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
                        Text(
                            text = post.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun PostContent(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
                    Text(
                        text = post.content,
            style = MaterialTheme.typography.bodyLarge
        )
        
        if (post.mediaUrls.isNotEmpty()) {
            MediaGallery(mediaUrls = post.mediaUrls)
        }
    }
}

@Composable
private fun MediaGallery(mediaUrls: List<String>) {
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(mediaUrls) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedImageIndex = mediaUrls.indexOf(url) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
    
    if (selectedImageIndex != null) {
        Dialog(
            onDismissRequest = { selectedImageIndex = null }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { selectedImageIndex = null }
            ) {
                AsyncImage(
                    model = mediaUrls[selectedImageIndex!!],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                
                IconButton(
                    onClick = { selectedImageIndex = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        ) {
                            Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
@Composable
private fun PostActions(
    post: Post,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit
) {
    Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
                    modifier = Modifier
                .weight(1f)
                .clickable { if (isLiked) onUnlikeClick() else onLikeClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val scale by animateFloatAsState(
                targetValue = if (isLiked) 1.2f else 1f,
                label = "like_scale"
            )
            
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) TownTalkColors.Primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
            
                    Text(
                text = "${post.likes.size} likes",
                style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
        }
        
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Comment,
                contentDescription = "Comments",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            
                            Text(
                text = "${post.comments} comments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CommentItem(comment: Comment) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
                )
            ) {
                Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                            Text(
                                text = comment.authorName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                    )
                            )
                
                            Text(
                    text = formatTimestamp(comment.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    
                    Text(
                        text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CommentInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isAddingComment: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Add a comment...") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TownTalkColors.Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        )
        
        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank() && !isAddingComment
        ) {
            if (isAddingComment) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TownTalkColors.Primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (value.isNotBlank()) TownTalkColors.Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Format a timestamp as a readable date string.
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 1_000_000 -> String.format("%.1fK", count / 1000.0)
        else -> String.format("%.1fM", count / 1_000_000.0)
    }
} 