package co.jp.tv.themes.pixel

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import co.jp.tv.core.util.LocalPaused
import kotlin.math.cos
import kotlin.math.sin

private const val TileDesignPx = 96f
private const val PerspectiveDesignPx = 640f
private const val FloorHeightDesignPx = 450f
private const val TiltDegrees = 62.0

/**
 * The scrolling perspective grid at the bottom. The design tilts a flat grid 62 degrees about its
 * top edge under a 640px perspective whose vanishing point is that edge; the lines are projected
 * by hand here (straight lines stay straight), so nothing is allocated per frame.
 */
@Composable
internal fun BoxScope.PixelGridFloor() {
    val paused = LocalPaused.current
    val phase: State<Float> = if (paused) {
        remember { mutableStateOf(0f) }
    } else {
        rememberInfiniteTransition(label = "pixelGrid").animateFloat(
            initialValue = 0f,
            targetValue = TileDesignPx,
            animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
            label = "gridPhase",
        )
    }
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(225.dp)
            .clipToBounds()
            .drawWithCache {
                val unit = 0.5.dp.toPx() // one design px
                val sinT = sin(Math.toRadians(TiltDegrees)).toFloat()
                val cosT = cos(Math.toRadians(TiltDegrees)).toFloat()
                // Screen y (design px) of a plane row [y] below the top edge, and its x scale.
                fun scaleAt(y: Float) = PerspectiveDesignPx / (PerspectiveDesignPx - y * sinT)
                fun screenY(y: Float) = y * cosT * scaleAt(y)
                // Plane row that reaches the bottom of the 450px container.
                val yMax = FloorHeightDesignPx * PerspectiveDesignPx /
                    (cosT * PerspectiveDesignPx + FloorHeightDesignPx * sinT)
                val centerX = size.width / 2f
                val line = 1.dp.toPx()
                val fade = Brush.verticalGradient(
                    0f to PixelPalette.FloorFadeTop,
                    0.38f to PixelPalette.FloorFadeTopClear,
                    0.62f to PixelPalette.FloorFadeBottomClear,
                    1f to PixelPalette.FloorFadeBottom,
                )
                onDrawBehind {
                    val scroll = phase.value
                    var y = scroll
                    while (y < yMax) {
                        val sy = screenY(y) * unit
                        drawLine(PixelPalette.FloorLine, Offset(0f, sy), Offset(size.width, sy), line)
                        y += TileDesignPx
                    }
                    val bottomScale = scaleAt(yMax)
                    val bottomY = screenY(yMax) * unit
                    for (j in -12..12) {
                        val x = j * TileDesignPx * unit
                        drawLine(
                            PixelPalette.FloorLine,
                            Offset(centerX + x, 0f),
                            Offset(centerX + x * bottomScale, bottomY),
                            line,
                        )
                    }
                    drawRect(fade)
                }
            },
    )
}
