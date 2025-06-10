package com.pixelsface.towntalk.core.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A reusable empty view composable that provides common styling and functionality.
 *
 * @param message The message to display when there is no content
 * @param modifier Modifier for the empty view
 * @param actionText The text to display on the action button
 * @param onAction The callback that is triggered when the action button is clicked
 */
@Composable
fun AppEmptyView(
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                AppButton(
                    text = actionText,
                    onClick = onAction,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
} 