package co.jp.tv.themes.hud

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import co.jp.tv.core.util.LocalPaused

/** Continuous rotation in degrees. No transition is created while paused. */
@Composable
private fun rememberHudSpin(durationMs: Int, reverse: Boolean): State<Float> {
    val paused = LocalPaused.current
    return if (paused) {
        remember { mutableStateOf(0f) }
    } else {
        rememberInfiniteTransition(label = "hudSpin").animateFloat(
            initialValue = if (reverse) 360f else 0f,
            targetValue = if (reverse) 0f else 360f,
            animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing), RepeatMode.Restart),
            label = "spin",
        )
    }
}

/** Blink of the REC dot (hud-blink): full for half a second, then 20%. Static while paused. */
@Composable
internal fun rememberHudBlink(): State<Float> {
    val paused = LocalPaused.current
    return if (paused) {
        remember { mutableStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "hudBlink").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at 0
                    1f at 499
                    0.2f at 500
                    0.2f at 1000
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "blink",
        )
    }
}

/** The three reticle rings around the clock: dashed (slow), top/bottom-lit (fast, reverse) and static. */
@Composable
internal fun HudRings() {
    val spinA by rememberHudSpin(40_000, reverse = false)
    val spinB by rememberHudSpin(12_000, reverse = true)
    Box(
        Modifier
            .size(302.dp)
            .graphicsLayer { rotationZ = spinA }
            .drawWithCache {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())),
                )
                onDrawBehind { drawCircle(HudPalette.RingDashed, radius = 150.5.dp.toPx(), style = stroke) }
            },
    )
    Box(
        Modifier
            .size(251.dp)
            .graphicsLayer { rotationZ = spinB }
            .drawWithCache {
                val stroke = Stroke(width = 0.5.dp.toPx())
                val radius = 125.25.dp.toPx()
                val topLeft = Offset(size.width / 2f - radius, size.height / 2f - radius)
                val arc = Size(radius * 2f, radius * 2f)
                onDrawBehind {
                    // A CSS circle border splits at the diagonals: lit top and bottom, dim sides.
                    drawArc(HudPalette.Cyan, -135f, 90f, false, topLeft, arc, style = stroke)
                    drawArc(HudPalette.RingMid, -45f, 90f, false, topLeft, arc, style = stroke)
                    drawArc(HudPalette.Cyan, 45f, 90f, false, topLeft, arc, style = stroke)
                    drawArc(HudPalette.RingMid, 135f, 90f, false, topLeft, arc, style = stroke)
                }
            },
    )
    Box(
        Modifier
            .size(441.dp)
            .drawWithCache {
                val stroke = Stroke(width = 0.5.dp.toPx())
                onDrawBehind { drawCircle(HudPalette.RingOuter, radius = 220.25.dp.toPx(), style = stroke) }
            },
    )
}

/** The scan line sweeping down the screen (hud-scan). Not drawn while paused. */
@Composable
internal fun HudScanLine() {
    if (LocalPaused.current) return
    val transition = rememberInfiniteTransition(label = "hudScan")
    val sweep by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "sweep",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer {
                val from = -60.dp.toPx()
                val to = 600.dp.toPx()
                translationY = from + (to - from) * sweep
            }
            .background(
                Brush.verticalGradient(
                    0f to HudPalette.ScanClear,
                    0.85f to HudPalette.ScanMid,
                    1f to HudPalette.ScanHead,
                ),
            ),
    )
}
