package co.jp.tv.themes.terminal

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import co.jp.tv.core.design.bakedLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import co.jp.tv.core.util.LocalPaused

/**
 * Elliptical radial gradient centred on the box. The vertical radius is [heightFactor] of the
 * height and the horizontal radius [widthFactor] of the width, like CSS `radial-gradient(W% H% ...)`.
 */
internal fun Modifier.terminalEllipse(
    colors: List<Color>,
    stops: List<Float>,
    widthFactor: Float,
    heightFactor: Float,
): Modifier = drawWithCache {
    val ry = size.height * heightFactor
    val sx = (size.width * widthFactor) / ry
    val brush = Brush.radialGradient(
        colorStops = Array(colors.size) { stops[it] to colors[it] },
        center = size.center,
        radius = ry,
    )
    onDrawBehind {
        scale(sx, 1f, pivot = size.center) { drawRect(brush, size = size) }
    }
}

/**
 * Rolling bar, then scanlines and vignette. The scanlines (hundreds of 1 dp rects) and the vignette
 * never change, so they are baked into one bitmap instead of being replayed every frame; the roll
 * bar sits under them (a faint band, so the order is hardly visible).
 */
@Composable
internal fun TerminalCrtOverlay() {
    Box(Modifier.fillMaxSize()) {
        TerminalRollBar()
        Box(
            Modifier
                .fillMaxSize()
                .bakedLayer {
                    // 2px dark every 4px of the 1920x1080 design, counted from the bottom edge.
                    val dark = 1.dp.toPx()
                    val period = 2.dp.toPx()
                    var top = size.height - dark
                    while (top > -dark) {
                        drawRect(TerminalPalette.Scanline, Offset(0f, top), Size(size.width, dark))
                        top -= period
                    }
                    val ry = size.height
                    val sx = (size.width * 1.2f) / ry
                    val vignette = Brush.radialGradient(
                        0f to TerminalPalette.VignetteClear,
                        0.6f to TerminalPalette.VignetteClear,
                        1f to TerminalPalette.Vignette,
                        center = size.center,
                        radius = ry,
                    )
                    scale(sx, 1f, pivot = size.center) { drawRect(vignette, size = size) }
                },
        )
    }
}

@Composable
private fun TerminalRollBar() {
    if (LocalPaused.current) return
    val transition = rememberInfiniteTransition(label = "terminalRoll")
    val roll by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "roll",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp)
            .graphicsLayer {
                val from = -80.dp.toPx()
                val to = 600.dp.toPx()
                translationY = from + (to - from) * roll
            }
            .background(
                Brush.verticalGradient(
                    listOf(TerminalPalette.RollClear, TerminalPalette.RollPeak, TerminalPalette.RollClear),
                ),
            ),
    )
}
