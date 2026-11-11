package co.jp.tv.themes.gotham

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp

private fun circle(cx: Float, cy: Float, r: Float) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

/** Stroke icons of the artboard, as SVG path data on a 24x24 grid with their stroke widths. */
internal enum class GothamGlyph(val paths: List<String>, val strokeWidth: Float) {
    Wifi(
        listOf("M2 8.5a15 15 0 0 1 20 0", "M5.5 12a10 10 0 0 1 13 0", "M9 15.5a5 5 0 0 1 6 0", circle(12f, 19f, 1f)),
        1.8f,
    ),
    Settings(
        listOf(
            circle(12f, 12f, 3f),
            "M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z",
        ),
        1.7f,
    ),
    Palette(
        listOf(
            circle(13.5f, 6.5f, 1.5f), circle(17.5f, 10.5f, 1.5f), circle(8.5f, 7.5f, 1.5f), circle(6.5f, 12.5f, 1.5f),
            "M12 2a10 10 0 0 0 0 20c1.1 0 2-.9 2-2 0-.5-.2-1-.5-1.3-.3-.4-.5-.8-.5-1.3 0-1.1.9-2 2-2h2.3A4.7 4.7 0 0 0 22 10.7C22 5.9 17.5 2 12 2z",
        ),
        1.7f,
    ),
    ChevronLeft(listOf("M15 5l-7 7 7 7"), 2.2f),
    ChevronRight(listOf("M9 5l7 7-7 7"), 2.2f),
}

@Composable
internal fun GothamIcon(glyph: GothamGlyph, size: Dp, color: Color, modifier: Modifier = Modifier) {
    val parsed = remember(glyph) { glyph.paths.map { PathParser().parsePathString(it).toPath() } }
    val stroke = remember(glyph) {
        Stroke(glyph.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    }
    Canvas(modifier.size(size)) {
        scale(this.size.width / 24f, pivot = Offset.Zero) {
            for (i in parsed.indices) drawPath(parsed[i], color, style = stroke)
        }
    }
}
