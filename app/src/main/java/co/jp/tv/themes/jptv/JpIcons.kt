package co.jp.tv.themes.jptv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp

/** Stroke icons from the design, as SVG path data on a 24x24 grid. */
internal class JpIcon(val paths: List<String>, val strokeWidth: Float = 1.8f)

private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

private fun rrect(x: Float, y: Float, w: Float, h: Float, r: Float) =
    "M${x + r} ${y}h${w - 2 * r}a$r $r 0 0 1 $r ${r}v${h - 2 * r}a$r $r 0 0 1 ${-r} ${r}h${-(w - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(h - 2 * r)}a$r $r 0 0 1 $r ${-r}z"

internal object JpIcons {
    val Bluetooth = JpIcon(listOf("M6.5 7.5l11 9-5.5 4.5v-18l5.5 4.5-11 9"), 1.7f)
    val Display = JpIcon(listOf(rrect(3f, 4f, 18f, 12f, 2f), "M8 20h8", "M12 16v4"), 1.7f)
    val Sound = JpIcon(listOf("M11 5L6 9H3v6h3l5 4V5z", "M15.5 8.5a5 5 0 0 1 0 7", "M18.5 5.5a9 9 0 0 1 0 13"), 1.7f)
    val Apps = JpIcon(
        listOf(rrect(3f, 3f, 7.5f, 7.5f, 2f), rrect(13.5f, 3f, 7.5f, 7.5f, 2f), rrect(3f, 13.5f, 7.5f, 7.5f, 2f), rrect(13.5f, 13.5f, 7.5f, 7.5f, 2f)),
        1.7f,
    )
    val Storage = JpIcon(
        listOf("M4 15l2.4-8A2 2 0 0 1 8.3 5.5h7.4a2 2 0 0 1 1.9 1.5L20 15", rrect(4f, 15f, 16f, 5f, 2f), circle(16.5f, 17.5f, 0.7f)),
        1.7f,
    )
    val Clock = JpIcon(listOf(circle(12f, 12f, 9f), "M12 7v5l3 2"), 1.7f)

    val Wifi = JpIcon(
        listOf("M2 8.5a15 15 0 0 1 20 0", "M5.5 12a10 10 0 0 1 13 0", "M9 15.5a5 5 0 0 1 6 0", circle(12f, 19f, 1f)),
    )
    val Settings = JpIcon(
        listOf(
            circle(12f, 12f, 3f),
            "M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z",
        ),
    )
    val Palette = JpIcon(
        listOf(
            circle(13.5f, 6.5f, 1.5f), circle(17.5f, 10.5f, 1.5f), circle(8.5f, 7.5f, 1.5f), circle(6.5f, 12.5f, 1.5f),
            "M12 2a10 10 0 0 0 0 20c1.1 0 2-.9 2-2 0-.5-.2-1-.5-1.3-.3-.4-.5-.8-.5-1.3 0-1.1.9-2 2-2h2.3A4.7 4.7 0 0 0 22 10.7C22 5.9 17.5 2 12 2z",
        ),
    )
}

@Composable
internal fun JpIconView(icon: JpIcon, size: Dp, color: Color, modifier: Modifier = Modifier) {
    val parsed = remember(icon) { icon.paths.map { PathParser().parsePathString(it).toPath() } }
    Canvas(modifier.size(size)) {
        scale(this.size.width / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(icon.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            parsed.forEach { drawPath(it, color, style = stroke) }
        }
    }
}
