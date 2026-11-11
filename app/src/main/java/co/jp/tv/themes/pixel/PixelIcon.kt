package co.jp.tv.themes.pixel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal enum class PixelIconKind { Wifi, Gear, Palette, ChevronLeft, ChevronRight }

/** The design's 24x24 line icons, redrawn as strokes. Gear and palette are simplified outlines. */
@Composable
internal fun PixelIcon(kind: PixelIconKind, color: Color, strokeUnits: Float, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .drawWithCache {
                val u = this.size.width / 24f
                val stroke = Stroke(width = strokeUnits * u, cap = StrokeCap.Round)
                onDrawBehind {
                    when (kind) {
                        PixelIconKind.Wifi -> drawWifi(color, stroke, u)
                        PixelIconKind.Gear -> drawGear(color, stroke, u)
                        PixelIconKind.Palette -> drawPalette(color, stroke, u)
                        PixelIconKind.ChevronLeft -> {
                            drawLine(color, Offset(15f * u, 5f * u), Offset(8f * u, 12f * u), stroke.width, StrokeCap.Round)
                            drawLine(color, Offset(8f * u, 12f * u), Offset(15f * u, 19f * u), stroke.width, StrokeCap.Round)
                        }
                        PixelIconKind.ChevronRight -> {
                            drawLine(color, Offset(9f * u, 5f * u), Offset(16f * u, 12f * u), stroke.width, StrokeCap.Round)
                            drawLine(color, Offset(16f * u, 12f * u), Offset(9f * u, 19f * u), stroke.width, StrokeCap.Round)
                        }
                    }
                }
            },
    )
}

private fun DrawScope.drawWifi(color: Color, stroke: Stroke, u: Float) {
    val cx = 12f * u
    val cy = 19.6f * u
    // radius, half opening angle in degrees (from the svg arc chords)
    val arcs = listOf(15f to 41.8f, 10f to 40.5f, 5f to 36.9f)
    arcs.forEach { (r, half) ->
        drawArc(
            color,
            startAngle = -90f - half,
            sweepAngle = half * 2f,
            useCenter = false,
            topLeft = Offset(cx - r * u, cy - r * u),
            size = androidx.compose.ui.geometry.Size(2f * r * u, 2f * r * u),
            style = stroke,
        )
    }
    drawCircle(color, radius = 2f * u, center = Offset(12f * u, 19f * u))
}

private fun DrawScope.drawGear(color: Color, stroke: Stroke, u: Float) {
    val c = Offset(12f * u, 12f * u)
    drawCircle(color, radius = 3f * u, center = c, style = stroke)
    drawCircle(color, radius = 7f * u, center = c, style = stroke)
    for (i in 0 until 8) {
        val a = i * PI.toFloat() / 4f
        val from = Offset(c.x + cos(a) * 7f * u, c.y + sin(a) * 7f * u)
        val to = Offset(c.x + cos(a) * 9.6f * u, c.y + sin(a) * 9.6f * u)
        drawLine(color, from, to, stroke.width, StrokeCap.Round)
    }
}

private fun DrawScope.drawPalette(color: Color, stroke: Stroke, u: Float) {
    drawCircle(color, radius = 9.5f * u, center = Offset(12f * u, 12f * u), style = stroke)
    listOf(13.5f to 6.5f, 17.5f to 10.5f, 8.5f to 7.5f, 6.5f to 12.5f).forEach { (x, y) ->
        drawCircle(color, radius = 1.5f * u, center = Offset(x * u, y * u), style = stroke)
    }
}
