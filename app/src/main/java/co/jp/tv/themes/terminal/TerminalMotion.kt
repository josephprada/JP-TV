package co.jp.tv.themes.terminal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.jp.tv.core.util.LocalPaused
import kotlin.math.floor
import kotlinx.coroutines.delay

/** CSS steps(3, end). */
private val TerminalSteps = Easing { f -> if (f >= 1f) 1f else floor(f * 3f) / 3f }

/** The design's t-line entrance: 12px slide plus fade in 3 steps, after [delayMs]. */
@Composable
internal fun Modifier.terminalLineIn(delayMs: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, tween(300, easing = TerminalSteps))
    }
    return graphicsLayer {
        alpha = progress.value
        translationX = -6.dp.toPx() * (1f - progress.value)
    }
}

/** Whole-screen CRT flicker (t-flicker). No transition is created while paused. */
@Composable
internal fun rememberTerminalFlicker(): State<Float> {
    val paused = LocalPaused.current
    return if (paused) {
        remember { mutableStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "terminalFlicker").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 6000
                    1f at 0
                    1f at 5520
                    0.85f at 5580
                    1f at 5640
                    1f at 6000
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "flicker",
        )
    }
}

/** Cursor blink (t-cursor): visible for the first half of every second. Static while paused. */
@Composable
internal fun rememberTerminalBlink(): State<Float> {
    val paused = LocalPaused.current
    return if (paused) {
        remember { mutableStateOf(1f) }
    } else {
        rememberInfiniteTransition(label = "terminalBlink").animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    1f at 0
                    1f at 499
                    0f at 500
                    0f at 1000
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "blink",
        )
    }
}

/** The phosphor text glow (0 0 6px). */
@Composable
internal fun rememberTerminalGlow(): Shadow {
    val density = LocalDensity.current
    return remember(density) { Shadow(TerminalPalette.Glow, Offset.Zero, with(density) { 3.dp.toPx() }) }
}
