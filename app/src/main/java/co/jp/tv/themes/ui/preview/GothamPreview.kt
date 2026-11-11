package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.drawGlowRect
import co.jp.tv.themes.ui.unit

private val SkyTop = Color(0xFF05070B)
private val SkyBottom = Color(0xFF1A2330)
private val Beam = Color(0xFFFFF6D2)
private val Building = Color(0xFF0A0E14)
private val SidePaper = Color(0xFFCDBD90).copy(alpha = 0.7f)
private val Paper = Color(0xFFE0D3AE)
private val Pin = Color(0xFFC0272D)
private val Photo = Color(0xFF262B33)
private val Scribble = Color(0xFF1A140A)

/** Thumbnail of the Gotham home: noir skyline with a searchlight and case files pinned to a wall. */
@Composable
internal fun GothamPreview(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val u = unit()
        drawRect(cssLinear(180f, listOf(0f to SkyTop, 1f to SkyBottom)))
        // Searchlight cone: polygon(47% 100%, 53% 100%, 100% 0, 0 0) over a 130x210 box, bottom 36 px.
        val coneLeft = 24f * u
        val coneTop = size.height - 36f * u - 210f * u
        val cone = Path().apply {
            moveTo(coneLeft + 130f * u * 0.47f, coneTop + 210f * u)
            lineTo(coneLeft + 130f * u * 0.53f, coneTop + 210f * u)
            lineTo(coneLeft + 130f * u, coneTop)
            lineTo(coneLeft, coneTop)
            close()
        }
        drawPath(
            cone,
            Brush.verticalGradient(
                0f to Beam.copy(alpha = 0.05f),
                1f to Beam.copy(alpha = 0.5f),
                startY = coneTop,
                endY = coneTop + 210f * u,
            ),
        )
        val skyline = listOf(0f to 46f to 90f, 46f to 60f to 130f, 106f to 40f to 70f, 146f to 70f to 110f, 216f to 50f to 150f, 266f to 80f to 84f, 346f to 80f to 120f)
        skyline.forEach { (xw, h) ->
            val (x, w) = xw
            drawRect(Building, Offset(x * u, size.height - h * u), Size(w * u, h * u))
        }
        sidePaper(left = 96f * u, deg = -7f, u = u)
        sidePaper(left = size.width - 82f * u - 54f * u, deg = 6f, u = u)
        // Center file: 68x100 with a red pin, a photo and a scribble line.
        val left = size.width / 2f - 34f * u
        val top = 34f * u
        drawGlowRect(Offset(left, top + 10f * u), Size(68f * u, 100f * u), 0f, 18f * u, Color.Black.copy(alpha = 0.5f))
        drawRect(Paper, Offset(left, top), Size(68f * u, 100f * u))
        drawCircle(Pin, radius = 6f * u, center = Offset(left + 34f * u, top))
        drawRect(Photo, Offset(left + 8f * u, top + 14f * u), Size(52f * u, 38f * u))
        drawRect(Scribble, Offset(left + 8f * u, top + 64f * u), Size(52f * u, 6f * u))
    }
}

private fun DrawScope.sidePaper(left: Float, deg: Float, u: Float) {
    val top = 52f * u
    rotate(deg, pivot = Offset(left + 27f * u, top + 40f * u)) {
        drawRect(SidePaper, Offset(left, top), Size(54f * u, 80f * u))
    }
}
