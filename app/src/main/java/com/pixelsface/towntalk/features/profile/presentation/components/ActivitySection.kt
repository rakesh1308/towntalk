package com.pixelsface.towntalk.features.profile.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pixelsface.towntalk.features.profile.domain.model.Activity
import com.pixelsface.towntalk.features.profile.domain.model.ActivityType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity section component.
 * Displays a list of the user's recent activities.
 *
 * @param activities The list of activities to display
 * @param onActivityClick Callback to handle activity click
 * @param modifier Modifier for the section
 */
@Composable
fun ActivitySection(
    activities: List<Activity>,
    onActivityClick: (Activity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically()
        ) {
            if (activities.isEmpty()) {
                EmptyActivity()
            } else {
                ActivityList(
                    activities = activities,
                    onActivityClick = onActivityClick
                )
            }
        }
    }
}

@Composable
private fun EmptyActivity() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .scale(0.8f)
                .alpha(0.5f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No recent activity",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your recent actions will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActivityList(
    activities: List<Activity>,
    onActivityClick: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        activities.take(5).forEach { activity ->
            ActivityItem(
                activity = activity,
                onClick = { onActivityClick(activity) }
            )
            if (activity != activities.take(5).last()) {
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )
            }
        }
    }
}

/**
 * Activity item component.
 * Displays a single activity with an icon, description, and timestamp.
 *
 * @param activity The activity to display
 * @param onClick Callback to handle activity click
 */
@Composable
private fun ActivityItem(
    activity: Activity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isHovered = event.type == PointerEventType.Enter
                    }
                }
            },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon based on type
            val (icon, iconTint) = when (activity.type) {
                ActivityType.POST_CREATED -> Icons.Default.Create to MaterialTheme.colorScheme.primary
                ActivityType.POST_COMMENTED -> Icons.Default.Comment to MaterialTheme.colorScheme.secondary
                ActivityType.POST_LIKED -> Icons.Default.Favorite to MaterialTheme.colorScheme.error
                ActivityType.ACHIEVEMENT_EARNED -> Icons.Default.EmojiEvents to MaterialTheme.colorScheme.tertiary
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatTimestamp(activity.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (activity.postId != null) {
                    Text(
                        text = "View Post",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Formats a timestamp into a human-readable string.
 *
 * @param timestamp The timestamp to format
 * @return A formatted string representing the timestamp
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        diff < 2592000000 -> "${diff / 604800000}w" // 30 days
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}

enum class ActivityType {
    POST_CREATED,
    POST_COMMENTED,
    POST_LIKED,
    ACHIEVEMENT_EARNED
} 