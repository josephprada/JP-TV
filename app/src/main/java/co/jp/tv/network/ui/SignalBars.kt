package co.jp.tv.network.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.State
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.LocalPaused

/** Four signal bars; [level] (0..4) of them are lit and the third one breathes softly, per the design. */
@Composable
fun SignalBars(
    level: Int,
    modifier: Modifier = Modifier,
    barWidth: Dp = 2.5.dp,
    heights: List<Dp> = listOf(3.5.dp, 6.dp, 8.5.dp, 11.dp),
    gap: Dp = 2.dp,
    paused: Boolean = LocalPaused.current,
) {
    val blink = softBlink(paused)
    val description = stringResource(R.string.cd_signal, level)
    Row(
        modifier = modifier.height(heights.last()).semantics { contentDescription = description },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        heights.forEachIndexed { i, h ->
            val lit = i < level
            val breathes = lit && i == BREATHING_BAR
            Box(
                Modifier
                    .width(barWidth)
                    .height(h)
                    .graphicsLayer { alpha = if (!lit) 0.25f else if (breathes) blink.value else 1f }
                    .background(JpColors.Paper, RoundedCornerShape(barWidth / 2)),
            )
        }
    }
}

private const val BREATHING_BAR = 2

/** 1 -> 0.35 -> 1 over 2s; idles at 1 while [paused]. */
@Composable
private fun softBlink(paused: Boolean): State<Float> {
    if (paused) return remember { mutableStateOf(1f) }
    val transition = rememberInfiniteTransition(label = "signal")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1000, easing = JpMotion.InOut), RepeatMode.Reverse),
        label = "signalBlink",
    )
}
