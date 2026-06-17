package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
 * Returns an animated Float that smoothly tracks scroll speed.
 */
@Composable
fun rememberScrollVelocity(listState: LazyListState): Float {
    var previous by remember { mutableIntStateOf(0) }
    var rawVelocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex * 10_000 + listState.firstVisibleItemScrollOffset
        }.collect { current ->
            rawVelocity = abs(current - previous).toFloat()
            previous = current
        }
    }

    val animatedVelocity by animateFloatAsState(
        targetValue = rawVelocity,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 250f),
        label = "scrollVelocity",
    )

    return animatedVelocity
}

/**
 * Creates a motion blur effect modifier based on scroll velocity.
 *
 * When scrolling fast items compress slightly (scale) and fade a bit (alpha).
 *
 * @param velocity Current scroll velocity in pixels/frame.
 * @param maxScaleEffect Maximum scale reduction (default 0.04f = 4%).
 * @param maxAlphaEffect Maximum alpha reduction (default 0.15f = 15%).
 */
fun Modifier.motionBlur(
    velocity: Float,
    maxScaleEffect: Float = 0.04f,
    maxAlphaEffect: Float = 0.15f,
): Modifier = graphicsLayer {
    val normalized = (velocity / 1000f).coerceIn(0f, 1f)
    val scale = 1f - (normalized * maxScaleEffect)
    scaleY = scale
    scaleX = scale
    alpha = 1f - (normalized * maxAlphaEffect)
}
