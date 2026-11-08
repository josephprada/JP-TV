package co.jp.tv.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Entrance: fades in while rising [distanceDp] (use 0 for a plain fade). */
@Composable
fun Modifier.riseIn(
    delayMs: Int,
    durationMs: Int = JpMotion.RiseMs,
    distanceDp: Float = 12f,
    enabled: Boolean = true,
): Modifier {
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (enabled) {
            delay(delayMs.toLong())
            progress.animateTo(1f, tween(durationMs, easing = JpMotion.Entrance))
        }
    }
    val distancePx = with(LocalDensity.current) { distanceDp.dp.toPx() }
    return this.graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * distancePx
    }
}
