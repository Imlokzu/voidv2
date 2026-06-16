package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs

/**
 * Remembers the scroll velocity of a [LazyListState].
 *
 * Uses total scroll position (firstVisibleItemIndex * itemHeight + scrollOffset) to compute
 * velocity in pixels per frame. The value is smoothed via [animateFloatAsState].
 *
 * Note: This only tracks vertical scroll velocity. For horizontal lists, a similar
 * approach with [androidx.compose.foundation.lazy.LazyListState.firstVisibleItemScrollOffset]
 * can be adapted, or use the total scroll position approach.
 *
 * @param listState The LazyListState to track
 * @param smoothingFactor Controls how quickly the animated value tracks the raw velocity
 *                        (lower = more smoothing, default 0.3f)
 * @return A Float representing the current scroll velocity magnitude (always positive)
 */
@Composable
fun rememberScrollVelocity(
    listState: LazyListState,
    smoothingFactor: Float = 0.3f,
): Float {
    var previous by remember { mutableIntStateOf(0) }
    var rawVelocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow {
            // Use a composite scroll position: itemIndex * 10000 + offset
            // 10000 is an arbitrary multiplier sufficient to separate items
            listState.firstVisibleItemIndex * 10_000 + listState.firstVisibleItemScrollOffset
        }.collect { current ->
            rawVelocity = abs(current - previous).toFloat()
            previous = current
        }
    }

    // Animate the velocity for smooth visual effect transitions
    val animatedVelocity by animateFloatAsState(
        targetValue = rawVelocity,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 250f),
        label = "scrollVelocity",
    )

    return animatedVelocity * smoothingFactor
}

/**
 * Creates a motion blur effect modifier based on scroll velocity.
 *
 * When scrolling fast:
 * - Items scale down slightly (scaleY decreases up to 4%)
 * - Items become slightly transparent (alpha decreases up to 15%)
 *
 * The effect is smoothed via spring animation to prevent jarring transitions.
 *
 * Note: This is a "compression" style motion blur - it doesn't use actual blur rendering.
 * On Android 12+ (API 31+), actual blur could be applied via RenderEffect but that
 * requires platform-specific code. This implementation uses scale/alpha compression
 * which creates a similar perceptual effect with better performance.
 *
 * @param velocityProvider Lambda that provides the current scroll velocity.
 *                        Higher values mean faster scrolling.
 * @param maxScaleEffect Maximum scale reduction when at full velocity (default 0.04f = 4%)
 * @param maxAlphaEffect Maximum alpha reduction when at full velocity (default 0.15f = 15%)
 * @return Modifier with motion blur graphics layer
 */
fun Modifier.motionBlur(
    velocityProvider: () -> Float,
    maxScaleEffect: Float = 0.04f,
    maxAlphaEffect: Float = 0.15f,
): Modifier = graphicsLayer {
    val velocity = velocityProvider()
    // Normalize velocity - at 1000+ pixels/frame we want full effect
    // Adjust the divisor based on typical scroll speeds in your app
    val normalizedVelocity = (velocity / 1000f).coerceIn(0f, 1f)

    // Scale down slightly on fast scroll - creates compression illusion
    val scale = 1f - (normalizedVelocity * maxScaleEffect)
    scaleY = scale
    scaleX = scale

    // Slight alpha reduction on fast scroll
    alpha = 1f - (normalizedVelocity * maxAlphaEffect)
}

/**
 * Applies motion blur to a composable based on [LazyListState] scroll velocity.
 *
 * Convenience overload that manages its own velocity state.
 *
 * @param listState The LazyListState to track
 * @param maxScaleEffect Maximum scale reduction (default 0.04f)
 * @param maxAlphaEffect Maximum alpha reduction (default 0.15f)
 * @return Modifier with motion blur effect
 */
fun Modifier.motionBlur(
    listState: LazyListState,
    maxScaleEffect: Float = 0.04f,
    maxAlphaEffect: Float = 0.15f,
): Modifier {
    val velocity = rememberScrollVelocity(listState)
    return this.motionBlur(
        velocityProvider = { velocity },
        maxScaleEffect = maxScaleEffect,
        maxAlphaEffect = maxAlphaEffect,
    )
}