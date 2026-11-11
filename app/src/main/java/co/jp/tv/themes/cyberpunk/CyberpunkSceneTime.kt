package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import co.jp.tv.core.util.LocalPaused

private const val LOOP_SECONDS = 3600f

/**
 * Scene clock in seconds driving every looping layer (rain, neon, vehicles, steam). Read it only
 * inside draw or graphicsLayer lambdas. While paused, the infinite transition is not created at
 * all and the scene freezes at 0 (the ZenRipples early-return pattern).
 */
@Composable
internal fun rememberSceneTime(): State<Float> {
    if (LocalPaused.current) return remember { mutableFloatStateOf(0f) }
    val transition = rememberInfiniteTransition(label = "cyberpunkScene")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = LOOP_SECONDS,
        animationSpec = infiniteRepeatable(tween((LOOP_SECONDS * 1000).toInt(), easing = LinearEasing), RepeatMode.Restart),
        label = "time",
    )
}
