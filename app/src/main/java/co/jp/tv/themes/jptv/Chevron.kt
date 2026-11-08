package co.jp.tv.themes.jptv

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun Chevron(up: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(9.dp)) {
        val w = size.width
        val h = size.height
        // design path "M6 15l6-6 6 6" on a 24 grid
        val y0 = if (up) 15f else 9f
        val y1 = if (up) 9f else 15f
        val p = Path().apply {
            moveTo(6f / 24 * w, y0 / 24 * h)
            lineTo(12f / 24 * w, y1 / 24 * h)
            lineTo(18f / 24 * w, y0 / 24 * h)
        }
        drawPath(p, color, style = Stroke(width = 1f.dp.toPx(), cap = StrokeCap.Round))
    }
}
