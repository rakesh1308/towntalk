package com.pixelsface.towntalk.core.common.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable divider composable that provides common styling and functionality.
 *
 * @param modifier Modifier for the divider
 * @param thickness The thickness of the divider
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        thickness = thickness,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
} 