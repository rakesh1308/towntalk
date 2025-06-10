package com.pixelsface.towntalk.core.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable list item composable that provides common styling and functionality.
 *
 * @param modifier Modifier for the list item
 * @param leadingContent The content to display at the start of the list item
 * @param headlineContent The main content of the list item
 * @param supportingContent The supporting content of the list item
 * @param trailingContent The content to display at the end of the list item
 * @param onClick The callback that is triggered when the list item is clicked
 */
@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                leadingContent()
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (leadingContent != null) 16.dp else 0.dp)
            ) {
                headlineContent()
                if (supportingContent != null) {
                    supportingContent()
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
} 