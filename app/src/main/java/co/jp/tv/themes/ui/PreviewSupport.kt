package co.jp.tv.themes.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.jp.tv.core.util.LocalPaused
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/*
 * Helpers shared by the theme preview thumbnails. The artboards are 1920x1080, so one design px is
 * half a dp; previews are drawn in design px and converted with these helpers.
 */

/** Design px to dp (dp = design px / 2). */
internal val Int.px: Dp get() = (this / 2f).dp

/** One design px in device pixels. */
internal fun DrawScope.unit(): Float = 0.5.dp.toPx()

/**
 * Brush equivalent to CSS `linear-gradient(angle, stops)` over a box of [area] at [origin].
 * [angleDeg] follows CSS: 0 points up, 90 points right, 180 points down.
 */
internal fun DrawScope.cssLinear(
    angleDeg: Float,
    stops: List<Pair<Float, Color>>,
    area: Size = size,
    origin: Offset = Offset.Zero,
): Brush {
    val a = Math.toRadians(angleDeg.toDouble())
    val dx = sin(a).toFloat()
    val dy = -cos(a).toFloat()
    val length = abs(area.width * dx) + abs(area.height * dy)
    val c = Offset(origin.x + area.width / 2f, origin.y + area.height / 2f)
    return Brush.linearGradient(
        *stops.toTypedArray(),
        start = Offset(c.x - dx * length / 2f, c.y - dy * length / 2f),
        end = Offset(c.x + dx * length / 2f, c.y + dy * length / 2f),
    )
}

/**
 * Fills [area] (default: the whole canvas) with an elliptical gradient centred on [center], like CSS
 * `radial-gradient(rx ry at x y, stops)`. Stop positions are fractions of the radii; the last color extends.
 */
internal fun DrawScope.drawEllipseGradient(
    center: Offset,
    rx: Float,
    ry: Float,
    stops: List<Pair<Float, Color>>,
    topLeft: Offset = Offset.Zero,
    area: Size = size,
) {
    val k = ry / rx
    val brush = Brush.radialGradient(*stops.toTypedArray(), center = center, radius = rx)
    scale(1f, k, pivot = center) {
        drawRect(
            brush,
            topLeft = Offset(topLeft.x, center.y + (topLeft.y - center.y) / k),
            size = Size(area.width, area.height / k),
        )
    }
}

/** Soft halo around a rounded rectangle, standing in for CSS `box-shadow: 0 0 blur color` (no blur in draw scope). */
internal fun DrawScope.drawGlowRect(topLeft: Offset, size: Size, corner: Float, spread: Float, color: Color) {
    val layers = 8
    for (i in layers downTo 1) {
        val grow = spread * i / layers
        val alpha = color.alpha * (1f - (i - 1f) / layers) / 5f
        drawRoundRect(
            color.copy(alpha = alpha),
            topLeft = Offset(topLeft.x - grow, topLeft.y - grow),
            size = Size(size.width + 2f * grow, size.height + 2f * grow),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner + grow),
        )
    }
}

/** Rectangle with two chamfered corners (top-right and bottom-left), like the cyberpunk signs. */
internal fun chamferPath(x: Float, y: Float, w: Float, h: Float, c: Float): Path = Path().apply {
    moveTo(x, y)
    lineTo(x + w - c, y)
    lineTo(x + w, y + c)
    lineTo(x + w, y + h)
    lineTo(x + c, y + h)
    lineTo(x, y + h - c)
    close()
}

/** CSS `th-twinkle`: opacity 0.2 -> 1 -> 0.2 over one cycle; [phase] shifts a star within the loop. */
internal fun twinkleAlpha(progress: Float, phase: Float): Float {
    val p = (progress + phase) % 1f
    return 0.2f + 0.8f * (0.5f - 0.5f * cos(2f * Math.PI.toFloat() * p))
}

/**
 * Looping 0..1 progress for a preview animation. While paused it creates no transition at all and
 * returns the constant [rest], like the other themes' infinite animations.
 */
@Composable
internal fun rememberLoop(durationMs: Int, rest: Float = 0.5f): State<Float> {
    if (LocalPaused.current) return remember { mutableFloatStateOf(rest) }
    val transition = rememberInfiniteTransition(label = "previewLoop")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMs, easing = LinearEasing)),
        label = "previewLoopValue",
    )
}
