package com.pixelsface.towntalk.core.common.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A reusable vertical spacing composable.
 *
 * @param height The height of the spacing
 */
@Composable
fun VerticalSpacer(height: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.height(height))
}

/**
 * A reusable horizontal spacing composable.
 *
 * @param width The width of the spacing
 */
@Composable
fun HorizontalSpacer(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}

/**
 * Common spacing values used throughout the app.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
} 