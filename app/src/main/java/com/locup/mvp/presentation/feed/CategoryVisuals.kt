package com.locup.mvp.presentation.feed

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.locup.mvp.domain.model.PostCategory

/**
 * Per-category colours.
 *
 * Kept next to the UI rather than in `domain` so the domain layer stays free
 * of presentation concerns. Each colour is paired with a tinted container so
 * chips stay legible in both light and dark themes.
 */
data class CategoryVisual(val accent: Color, val container: Color, val onContainer: Color)

@Composable
fun PostCategory.visual(): CategoryVisual = when (this) {
    PostCategory.Emergency -> CategoryVisual(
        accent = Color(0xFFC62828),
        container = Color(0xFFFFE0E0),
        onContainer = Color(0xFF7F1616),
    )
    PostCategory.Traffic -> CategoryVisual(
        accent = Color(0xFFE65100),
        container = Color(0xFFFFE8D6),
        onContainer = Color(0xFF8A3D00),
    )
    PostCategory.Event -> CategoryVisual(
        accent = Color(0xFF1565C0),
        container = Color(0xFFDDEBFF),
        onContainer = Color(0xFF0D4A8F),
    )
    PostCategory.Civic -> CategoryVisual(
        accent = Color(0xFF6A4C93),
        container = Color(0xFFEDE3F8),
        onContainer = Color(0xFF4A3269),
    )
    PostCategory.General -> CategoryVisual(
        accent = Color(0xFF00796B),
        container = Color(0xFFD5F0EB),
        onContainer = Color(0xFF00544A),
    )
}

/** Uncategorised / error accent, used for banners. */
@Composable
fun noticeColors(): Pair<Color, Color> =
    MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer