package com.locup.mvp.presentation.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.locup.mvp.domain.model.Post
import com.locup.mvp.domain.repository.ClassifierUnavailableReason
import com.locup.mvp.domain.usecase.FeedFilter

/**
 * The feed screen.
 *
 * Stateless by design: it renders [FeedUiState] and emits intents upward.
 * That is what allows it to be previewed and reasoned about without a
 * ViewModel, a database, or a device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onFilterChange: (FeedFilter) -> Unit,
    onToggleSubscription: () -> Unit,
    onRequestLocation: () -> Unit,
    onUpvote: (String) -> Unit,
    onDownvote: (String) -> Unit,
    onComposeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TownTalk", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = state.areaLabel?.let { "Near $it" } ?: "Location unavailable",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (state.hasLocation) {
                        AssistChip(
                            onClick = onToggleSubscription,
                            leadingIcon = {
                                Icon(
                                    imageVector = if (state.isSubscribedToCurrentArea) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Filled.StarBorder
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            label = {
                                Text(
                                    if (state.isSubscribedToCurrentArea) {
                                        "Following"
                                    } else {
                                        "Follow"
                                    },
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    } else {
                        AssistChip(
                            onClick = onRequestLocation,
                            label = { Text("Use my location") },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onComposeClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Post") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            ClassifierBanner(state)

            FilterRow(
                selected = state.filter,
                onChange = onFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            when {
                state.isLoading -> LoadingState()
                state.posts.isEmpty() -> EmptyState(state.filter, state.hasLocation)
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.posts, key = Post::id) { post ->
                        PostCard(
                            post = post,
                            onUpvote = { onUpvote(post.id) },
                            onDownvote = { onDownvote(post.id) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * States which engine is actually running.
 *
 * Reads [FeedUiState.classifierStatus] rather than hard-coding a string, so
 * the app can never claim a neural network when the keyword scorer is what
 * produced the label.
 */
@Composable
private fun ClassifierBanner(state: FeedUiState, modifier: Modifier = Modifier) {
    val status = state.classifierStatus ?: return
    val isReal = status.isRealModel

    val background = if (isReal) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val foreground = if (isReal) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val detail = when (status.unavailableReason) {
        ClassifierUnavailableReason.MISSING_ASSET ->
            "model asset not bundled — using keyword fallback"

        ClassifierUnavailableReason.INITIALIZATION_FAILED ->
            "model failed to initialise (${status.detail}) — using keyword fallback"

        ClassifierUnavailableReason.INFERENCE_FAILED ->
            "inference error (${status.detail}) — using keyword fallback"

        null -> status.detail
    }

    Surface(
        color = background,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status dot: filled when the real model is running, hollow when
            // the keyword fallback is in charge.
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isReal) foreground else MaterialTheme.colorScheme.outline),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "On-device: ${status.engine.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = foreground,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelSmall,
                    color = foreground,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    selected: FeedFilter,
    onChange: (FeedFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == FeedFilter.Here,
            onClick = { onChange(FeedFilter.Here) },
            label = { Text("Near me") },
        )
        FilterChip(
            selected = selected == FeedFilter.Subscribed,
            onClick = { onChange(FeedFilter.Subscribed) },
            label = { Text("Following") },
        )
        FilterChip(
            selected = selected == FeedFilter.All,
            onClick = { onChange(FeedFilter.All) },
            label = { Text("All") },
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Empty states explain *what to do next*, not just that there is nothing here —
 * the difference between a dead end and an invitation.
 */
@Composable
private fun EmptyState(filter: FeedFilter, hasLocation: Boolean, modifier: Modifier = Modifier) {
    // The message must describe the filter the user actually selected. An
    // earlier version keyed this off location alone, so tapping "Following"
    // with no GPS fix wrongly showed "Showing every area" — advice that had
    // nothing to do with the empty list in front of them.
    val (title, body) = when (filter) {
        FeedFilter.Subscribed ->
            "No areas followed yet" to
                "Use Follow on the top bar while you're near an area, then posts from there will appear here."

        FeedFilter.Here ->
            if (hasLocation) {
                "Nothing nearby yet" to
                    "Be the first to post what's happening around here."
            } else {
                "Showing every area" to
                    "No location yet, so this is everything. Allow location to filter to your neighbourhood."
            }

        FeedFilter.All ->
            "No posts yet" to
                "Tap Post to add the first update for your neighbourhood."
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.author,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                CategoryChip(post)
            }

            if (post.isDemoData) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sample post",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clearAndSetSemantics { },
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(post.text, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Place,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = post.areaLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                VoteButton(
                    icon = Icons.Filled.KeyboardArrowUp,
                    count = post.upCount,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Upvote ${post.text}",
                    onClick = onUpvote,
                )
                Spacer(Modifier.width(8.dp))
                VoteButton(
                    icon = Icons.Filled.KeyboardArrowDown,
                    count = post.downCount,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = "Downvote ${post.text}",
                    onClick = onDownvote,
                )
                Spacer(Modifier.weight(1f))
                ConfidenceBadge(post)
            }
        }
    }
}

@Composable
private fun VoteButton(
    icon: ImageVector,
    count: Int,
    tint: androidx.compose.ui.graphics.Color,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = tint.copy(alpha = 0.10f),
        shape = RoundedCornerShape(50),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = count.toString(),
                color = tint,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Shows the two signals side by side: how confident the model was, and how the
 * crowd voted. Conflating them would hide the fact that a confident model can
 * still be wrong.
 */
@Composable
private fun ConfidenceBadge(post: Post, modifier: Modifier = Modifier) {
    val score = post.communityScore
    val (container, onContainer, label) = when {
        score >= 5 -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Community +$score",
        )
        score <= -2 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Disputed $score",
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Community $score",
        )
    }

    Surface(color = container, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            text = "$label · model ${(post.modelConfidence * 100).toInt()}%",
            color = onContainer,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun CategoryChip(post: Post, modifier: Modifier = Modifier) {
    val visual = post.category.visual()
    Surface(
        color = visual.container,
        shape = RoundedCornerShape(50),
        modifier = modifier,
    ) {
        Text(
            text = post.category.name,
            color = visual.onContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}