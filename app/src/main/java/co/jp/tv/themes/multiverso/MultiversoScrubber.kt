package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.riseIn

/**
 * Timeline scrubber at the bottom: a 1000 px rail with one tick per app (the focused one tall and
 * tinted) and the "02 / 10" position to its right. Ticks follow the focus with the 0.7 s transition.
 */
@Composable
internal fun MultiversoScrubber(appCount: Int, focusedIndex: Int, modifier: Modifier = Modifier) {
    val spec = tween<Float>(MultiversoMotion.FocusMs, easing = MultiversoMotion.Swift)
    val ticks = List(appCount) { i ->
        key(i) { animateFloatAsState(if (i == focusedIndex) 1f else 0f, spec, label = "multiversoTick") }
    }
    val rail = remember {
        Brush.horizontalGradient(
            0f to MultiversoPalette.ScrubLine.copy(alpha = 0f),
            0.15f to MultiversoPalette.ScrubLine,
            0.85f to MultiversoPalette.ScrubLine,
            1f to MultiversoPalette.ScrubLine.copy(alpha = 0f),
            startX = RailLeft,
            endX = RailRight,
        )
    }
    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .riseIn(delayMs = 250, durationMs = MultiversoMotion.EntranceMs, distanceDp = 7f),
        ) {
            scale(density / 2f, pivot = Offset.Zero) {
                drawRect(rail, topLeft = Offset(RailLeft, RailY), size = Size(RailRight - RailLeft, 1f))
                for (i in ticks.indices) {
                    val x = if (appCount > 1) TicksLeft + i * (TicksWidth / (appCount - 1)) else TicksLeft + TicksWidth / 2f
                    drawTick(x, ticks[i].value, MultiversoPalette.tint(i))
                }
            }
        }
        if (appCount > 0) {
            BasicText(
                "${pad2(focusedIndex.coerceIn(0, appCount - 1) + 1)} / ${pad2(appCount)}",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 760.dp, bottom = 39.dp),
                style = TextStyle(
                    color = MultiversoPalette.TextPosition,
                    fontSize = 7.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.22.em,
                ),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/** Mark 2x10 idle, 4x30 focused, radius 2, with `0 0 14px tint, 0 0 4px #fff` while focused. */
private fun DrawScope.drawTick(x: Float, p: Float, tint: Color) {
    val w = lerp(2f, 4f, p)
    val h = lerp(10f, 30f, p)
    if (p > 0.004f) {
        glowRect(x, w, h, 7f, tint, 0.25f * p)
        glowRect(x, w, h, 3.5f, tint, 0.4f * p)
        glowRect(x, w, h, 2f, MultiversoPalette.White, 0.5f * p)
    }
    val topLeft = Offset(x - w / 2f, RailY - h / 2f)
    val size = Size(w, h)
    drawRoundRect(MultiversoPalette.TickIdle, topLeft, size, TickCorner, alpha = 1f - p)
    drawRoundRect(tint, topLeft, size, TickCorner, alpha = p)
}

private fun DrawScope.glowRect(x: Float, w: Float, h: Float, spread: Float, color: Color, alpha: Float) {
    drawRoundRect(
        color,
        topLeft = Offset(x - w / 2f - spread, RailY - h / 2f - spread),
        size = Size(w + 2 * spread, h + 2 * spread),
        cornerRadius = CornerRadius(2f + spread),
        alpha = alpha,
    )
}

private fun pad2(value: Int): String = value.toString().padStart(2, '0')

// Design px: the scrubber box is left 460, width 1000, bottom 64, height 48 (center y = 992).
private const val TicksLeft = 460f
private const val TicksWidth = 1000f
private const val RailLeft = 420f
private const val RailRight = 1500f
private const val RailY = 992f
private val TickCorner = CornerRadius(2f)
