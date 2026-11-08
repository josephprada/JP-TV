package co.jp.tv.themes.jptv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
internal fun Chevron(up: Boolean, color: Color, modifier: Modifier = Modifier) {
    val path = remember(up) {
        // design path "M6 15l6-6 6 6" on a 24 grid (flipped for "down")
        val y0 = if (up) 15f else 9f
        val y1 = if (up) 9f else 15f
        Path().apply {
            moveTo(6f, y0)
            lineTo(12f, y1)
            lineTo(18f, y0)
        }
    }
    Canvas(modifier.size(9.dp)) {
        val k = size.width / 24f
        scale(k, pivot = Offset.Zero) {
            drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
        }
    }
}
