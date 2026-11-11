package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import co.jp.tv.themes.ui.drawEllipseGradient
import co.jp.tv.themes.ui.unit

private val WallInner = Color(0xFF3A2418)
private val WallOuter = Color(0xFF24160F)
private val Orange = Color(0xFFE8743B)
private val Teal = Color(0xFF2F7F7A)
private val Groove = Color(0xFF111111)
private val GrooveLight = Color(0xFF222222)
private val Label = Color(0xFFE0B23A)
private val Shelf = Color(0xFF6B4226)

/**
 * Thumbnail of the Vinilo home: sleeves on a shelf and a record. The design spins the record, but its
 * grooves are concentric around a centered label, so the rotation is invisible and no transition is created.
 */
@Composable
internal fun ViniloPreview(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val u = unit()
        drawEllipseGradient(
            center = Offset(size.width * 0.30f, size.height * 0.20f),
            rx = size.width * 0.9f,
            ry = size.height * 0.8f,
            stops = listOf(0f to WallInner, 0.7f to WallOuter),
        )
        drawRoundRect(Orange, Offset(20f * u, 40f * u), Size(120f * u, 120f * u), CornerRadius(8f * u))
        drawRoundRect(Teal, Offset(150f * u, 80f * u), Size(80f * u, 80f * u), CornerRadius(6f * u))
        val disc = Offset(size.width - 20f * u - 75f * u, 30f * u + 75f * u)
        drawCircle(Groove, radius = 75f * u, center = disc)
        var r = 3.5f
        while (r < 75f) {
            drawCircle(GrooveLight, radius = r * u, center = disc, style = Stroke(1f * u))
            r += 4f
        }
        drawCircle(Label, radius = 25f * u, center = disc)
        drawRoundRect(Shelf, Offset(10f * u, size.height - 30f * u - 14f * u), Size(size.width - 20f * u, 14f * u), CornerRadius(4f * u))
    }
}
