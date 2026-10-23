package com.locup.mvp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * TownTalk visual identity.
 *
 * A single seed colour drives the scheme so the app reads as one product
 * rather than a stock-Material demo. Category colours live in
 * `CategoryPalette` because they need to stay distinguishable from the
 * surface, not just from each other.
 */
private val TownTalkBlue = Color(0xFF1B6FE0)
private val TownTalkBlueDark = Color(0xFF0D3F82)
private val TownTalkAmber = Color(0xFFF2A03D)

private val LightColors = lightColorScheme(
    primary = TownTalkBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = TownTalkBlueDark,
    secondary = TownTalkAmber,
    onSecondary = Color(0xFF2B1700),
    secondaryContainer = Color(0xFFFFE2BE),
    onSecondaryContainer = Color(0xFF2B1700),
    background = Color(0xFFFBFCFF),
    surface = Color(0xFFFBFCFF),
    surfaceVariant = Color(0xFFE7EBF3),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF00306B),
    primaryContainer = Color(0xFF00458F),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFFFC078),
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF613F00),
    onSecondaryContainer = Color(0xFFFFE2BE),
)

private val AppTypography = Typography(
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium),
)

/**
 * Follows the system dark-mode setting so the demo looks native on any device.
 */
@Composable
fun TownTalkTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}