package co.jp.tv.themes.cyberpunk

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.sqrt

private const val SOFT_STEPS = 8

/** sqrt(2): CSS `radial-gradient(ellipse|circle, ...)` defaults to farthest-corner. */
internal val FARTHEST_CORNER = sqrt(2f)

/**
 * Approximates a CSS blur of a rectangle (box-shadow glow, `filter: blur`) without RenderEffect
 * (minSdk 24): stacked rounded rects from +[blur] to -[blur] around the edge. No allocation.
 */
internal fun DrawScope.softRect(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    color: Color,
    blur: Float,
    corner: Float = 0f,
    spread: Float = 0f,
    alpha: Float = 1f,
) {
    val target = (color.alpha * alpha).coerceIn(0f, 1f)
    if (target <= 0f) return
    // Overlapping layers give a linear fall-off across the edge, like a blur of a solid rect.
    val layer = (target * 1.5f / SOFT_STEPS).coerceAtMost(1f)
    for (i in 0 until SOFT_STEPS) {
        val e = spread + blur * (1f - (2f * i + 1f) / SOFT_STEPS)
        val w = width + 2f * e
        val h = height + 2f * e
        if (w <= 0f || h <= 0f) continue
        val r = (corner + e).coerceAtLeast(0f)
        drawRoundRect(
            color = color.copy(alpha = layer),
            topLeft = Offset(left - e, top - e),
            size = Size(w, h),
            cornerRadius = CornerRadius(r, r),
        )
    }
}

/**
 * CSS `inset 0 0 blur color`: rings fading inwards from the edge of a rounded rect. [ring] is a
 * remembered stroke whose width is `blur / INNER_GLOW_STEPS`, so nothing is allocated here.
 */
internal fun DrawScope.innerGlow(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    color: Color,
    ring: Stroke,
    corner: Float,
    alpha: Float = 1f,
) {
    val target = (color.alpha * alpha).coerceIn(0f, 1f)
    if (target <= 0f) return
    val step = ring.width
    for (i in 0 until INNER_GLOW_STEPS) {
        val inset = step * (i + 0.5f)
        val w = width - 2f * inset
        val h = height - 2f * inset
        if (w <= 0f || h <= 0f) break
        val r = (corner - inset).coerceAtLeast(0f)
        drawRoundRect(
            color = color.copy(alpha = target * (1f - i.toFloat() / INNER_GLOW_STEPS)),
            topLeft = Offset(left + inset, top + inset),
            size = Size(w, h),
            cornerRadius = CornerRadius(r, r),
            style = ring,
        )
    }
}

internal const val INNER_GLOW_STEPS = 8

/**
 * An element with `border-radius: 50%` and an elliptical radial gradient: [brush] must be a circular
 * radial gradient centred on ([cx], [cy]) with radius `rx * FARTHEST_CORNER`; it is squashed to [ry].
 */
internal fun DrawScope.ovalGradient(cx: Float, cy: Float, rx: Float, ry: Float, brush: Brush, alpha: Float = 1f) {
    if (alpha <= 0f || rx <= 0f) return
    val center = Offset(cx, cy)
    scale(scaleX = 1f, scaleY = ry / rx, pivot = center) {
        drawCircle(brush = brush, radius = rx, center = center, alpha = alpha)
    }
}
