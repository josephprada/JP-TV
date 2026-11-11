package co.jp.tv.themes.pixel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * A CSS `linear-gradient(<angle>deg, ...)` over a box of [size] whose top-left corner is at
 * [origin]: the gradient line passes through the centre and is as long as the CSS spec says, so
 * the corners get exactly the first and last color.
 */
internal fun cssGradient(
    angleDeg: Float,
    size: Size,
    origin: Offset,
    stops: List<Pair<Float, Color>>,
): Brush {
    val rad = Math.toRadians(angleDeg.toDouble())
    val dx = sin(rad).toFloat()
    val dy = -cos(rad).toFloat()
    val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
    val center = origin + Offset(size.width / 2f, size.height / 2f)
    return Brush.linearGradient(
        colorStops = stops.toTypedArray(),
        start = center - Offset(dx * half, dy * half),
        end = center + Offset(dx * half, dy * half),
    )
}
