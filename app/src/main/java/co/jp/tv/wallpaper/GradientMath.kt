package co.jp.tv.wallpaper

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Start/end points of a CSS `linear-gradient(<angle>deg, ...)` for a box of the given size. */
internal fun cssGradientLine(angleDeg: Float, width: Float, height: Float): Pair<Offset, Offset> {
    val a = Math.toRadians(angleDeg.toDouble())
    val dx = sin(a).toFloat()
    val dy = -cos(a).toFloat()
    val length = abs(width * dx) + abs(height * dy)
    val cx = width / 2f
    val cy = height / 2f
    return Offset(cx - dx * length / 2f, cy - dy * length / 2f) to Offset(cx + dx * length / 2f, cy + dy * length / 2f)
}

/** 0 -> 1 -> 0 ease-in-out wave over [periodMs]. */
internal fun pingPong(timeMs: Long, periodMs: Float, phase: Float = 0f): Float {
    val t = (timeMs / periodMs + phase) * (2.0 * Math.PI)
    return (0.5 - 0.5 * cos(t)).toFloat()
}
