package com.pixelsface.towntalk.core.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp

/**
 * Common animation durations used throughout the app.
 */
object AnimationDuration {
    const val Short = 150
    const val Medium = 300
    const val Long = 450
}

/**
 * Common animation curves used throughout the app.
 */
object AnimationCurve {
    val Standard = FastOutSlowInEasing
    val Linear = LinearEasing
    val Decelerate = LinearOutSlowInEasing
}

/**
 * A reusable animated visibility composable that provides common animation functionality.
 *
 * @param visible Whether the content should be visible
 * @param modifier Modifier for the animated content
 * @param content The content to animate
 */
@Composable
fun AppAnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        content = content
    )
}

/**
 * Common animation functions for the app.
 */

@Composable
fun fadeInOutAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
                ),
        exit = fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)) +
                slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
                ),
        content = content
    )
}

@Composable
fun slideInOutAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        content = content
    )
}

@Composable
fun scaleInOutAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard),
            transformOrigin = TransformOrigin(0.5f, 0.5f)
        ) + fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        content = content
    )
}

@Composable
fun infinitePulseAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        exit = fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        content = {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            content()
        }
    )
}

@Composable
fun slideInFromBottomAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        content = content
    )
}

@Composable
fun slideInFromRightAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeIn(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)
        ) + fadeOut(animationSpec = tween(AnimationDuration.Medium, easing = AnimationCurve.Standard)),
        content = content
    )
} 