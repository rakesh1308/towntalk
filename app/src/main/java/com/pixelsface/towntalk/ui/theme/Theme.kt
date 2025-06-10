package com.pixelsface.towntalk.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TownTalkColors.Primary,
    onPrimary = TownTalkColors.OnPrimary,
    primaryContainer = TownTalkColors.PrimaryLight,
    secondary = TownTalkColors.Secondary,
    onSecondary = TownTalkColors.OnSecondary,
    secondaryContainer = TownTalkColors.SecondaryLight,
    background = TownTalkColors.Background,
    onBackground = TownTalkColors.OnBackground,
    surface = TownTalkColors.Surface,
    onSurface = TownTalkColors.OnSurface,
    error = TownTalkColors.Error,
    onError = TownTalkColors.OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = TownTalkColors.PrimaryLight,
    onPrimary = TownTalkColors.OnPrimary,
    primaryContainer = TownTalkColors.PrimaryDark,
    secondary = TownTalkColors.SecondaryLight,
    onSecondary = TownTalkColors.OnSecondary,
    secondaryContainer = TownTalkColors.SecondaryDark,
    background = TownTalkColors.Background,
    onBackground = TownTalkColors.OnBackground,
    surface = TownTalkColors.Surface,
    onSurface = TownTalkColors.OnSurface,
    error = TownTalkColors.Error,
    onError = TownTalkColors.OnError
)

@Composable
fun TownTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TownTalkTypography,
        content = content
    )
} 