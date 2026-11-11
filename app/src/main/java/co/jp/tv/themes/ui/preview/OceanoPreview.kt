package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.drawEllipseGradient
import co.jp.tv.themes.ui.unit

private val WaterTop = Color(0xFF0B6A8A)
private val WaterMid = Color(0xFF063A5C)
private val WaterBottom = Color(0xFF021A33)
private val Ray = Color(0xFFAAEBFF)
private val Seabed = Color(0xFF041526)
private val Pink = Color(0xFFFF8AD8)
private val Cyan = Color(0xFF7AF0FF)
private val Lilac = Color(0xFFB28CFF)
private val Gold = Color(0xFFFFD75A)
private val SmallFish = Color(0xFFA9CFE6)

/**
 * Thumbnail of the Oceano home: light rays, jellyfish, fish and a reef. The artboard shows one static
 * underwater scene, so no day or night variant is drawn.
 */
@Composable
internal fun OceanoPreview(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize()) {
        val u = unit()
        drawRect(cssLinear(180f, listOf(0f to WaterTop, 0.45f to WaterMid, 1f to WaterBottom)))
        ray(60f * u, 60f * u, -12f, 0.35f, u)
        ray(230f * u, 80f * u, 10f, 0.30f, u)
        seabed(u)
        val centerBell = Offset(size.width / 2f - 46f * u, 40f * u)
        drawGlowBell(centerBell, Size(92f * u, 72f * u), Pink.copy(alpha = 0.8f), 30f * u)
        bell(centerBell, Size(92f * u, 72f * u)) {
            drawEllipseGradient(
                center = Offset(centerBell.x + 46f * u, centerBell.y + 0.26f * 72f * u),
                rx = 65f * u,
                ry = 75f * u,
                stops = listOf(0f to Color.White.copy(alpha = 0.85f), 0.3f to Pink.copy(alpha = 0.6f), 0.75f to Pink.copy(alpha = 0.2f)),
                topLeft = centerBell,
                area = Size(92f * u, 72f * u),
            )
        }
        // Tentacles: 2 px lines every 12 px, fading downwards.
        val tentacleLeft = size.width / 2f - 35f * u
        val fade = Brush.verticalGradient(
            0f to Pink.copy(alpha = 0.7f),
            1f to Pink.copy(alpha = 0f),
            startY = 108f * u,
            endY = 178f * u,
        )
        var x = 0f
        while (x < 70f) {
            drawRect(fade, Offset(tentacleLeft + x * u, 108f * u), Size(2f * u, 70f * u))
            x += 12f
        }
        bell(Offset(40f * u, 120f * u), Size(46f * u, 64f * u)) { drawRect(Cyan.copy(alpha = 0.28f)) }
        bell(Offset(size.width - 44f * u - 46f * u, 110f * u), Size(46f * u, 40f * u)) { drawRect(Lilac.copy(alpha = 0.4f)) }
        drawOval(Gold, Offset(270f * u, 160f * u), Size(30f * u, 14f * u))
        drawOval(SmallFish, Offset(110f * u, 70f * u), Size(20f * u, 10f * u))
        drawOval(SmallFish, Offset(134f * u, 80f * u), Size(20f * u, 10f * u))
        drawOval(SmallFish, Offset(118f * u, 92f * u), Size(20f * u, 10f * u))
    }
}

/** Soft light shaft hanging from the top edge, tilted around its top centre. */
private fun DrawScope.ray(left: Float, width: Float, degrees: Float, alpha: Float, u: Float) {
    val top = -20f * u
    rotate(degrees, pivot = Offset(left + width / 2f, top)) {
        drawRect(
            Brush.verticalGradient(0f to Ray.copy(alpha = alpha), 1f to Ray.copy(alpha = 0f), startY = top, endY = top + 260f * u),
            Offset(left, top),
            Size(width, 260f * u),
        )
    }
}

/** Sea floor: 46 px tall, top corners 60% / 40% wide and 30 / 22 px tall. */
private fun DrawScope.seabed(u: Float) {
    val top = size.height - 46f * u
    val w = size.width
    val path = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, top + 30f * u)
        arcTo(Rect(0f, top, w * 1.2f, top + 60f * u), 180f, 90f, false)
        arcTo(Rect(w * 0.2f, top, w, top + 44f * u), 270f, 90f, false)
        lineTo(w, size.height)
        close()
    }
    drawPath(path, Seabed)
}

/** Jellyfish bell: 50% / 50% / 22% / 22% horizontal and 64% / 64% / 36% / 36% vertical corner radii. */
private fun bellPath(topLeft: Offset, size: Size): Path = Path().apply {
    addRoundRect(
        RoundRect(
            left = topLeft.x,
            top = topLeft.y,
            right = topLeft.x + size.width,
            bottom = topLeft.y + size.height,
            topLeftCornerRadius = CornerRadius(size.width * 0.5f, size.height * 0.64f),
            topRightCornerRadius = CornerRadius(size.width * 0.5f, size.height * 0.64f),
            bottomRightCornerRadius = CornerRadius(size.width * 0.22f, size.height * 0.36f),
            bottomLeftCornerRadius = CornerRadius(size.width * 0.22f, size.height * 0.36f),
        ),
    )
}

private fun DrawScope.bell(topLeft: Offset, size: Size, fill: DrawScope.() -> Unit) {
    clipPath(bellPath(topLeft, size)) { fill() }
}

private fun DrawScope.drawGlowBell(topLeft: Offset, size: Size, color: Color, spread: Float) {
    val layers = 8
    for (i in layers downTo 1) {
        val grow = spread * i / layers
        val alpha = color.alpha * (1f - (i - 1f) / layers) / 5f
        drawPath(
            bellPath(Offset(topLeft.x - grow, topLeft.y - grow), Size(size.width + 2f * grow, size.height + 2f * grow)),
            color.copy(alpha = alpha),
        )
    }
}
