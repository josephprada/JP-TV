package co.jp.tv.themes.multiverso

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * A stroke with the artboard's `feGaussianBlur` look. Real blur filters are not portable to API 24,
 * so a blurred stroke of standard deviation `sigma` is drawn as three nested strokes
 * (width + 4 sigma, width + 2 sigma, width) with decreasing spread. `sigma = 0` draws one sharp stroke.
 * Build once; [draw] allocates nothing.
 */
internal class SoftStroke(width: Float, sigma: Float, pathEffect: PathEffect? = null) {
    private val strokes: Array<Stroke>
    private val weights: FloatArray

    init {
        fun stroke(w: Float) = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = pathEffect)
        if (sigma <= 0f) {
            strokes = arrayOf(stroke(width))
            weights = floatArrayOf(1f)
        } else {
            strokes = arrayOf(stroke(width + 4 * sigma), stroke(width + 2 * sigma), stroke(width))
            weights = floatArrayOf(0.2f, 0.3f, 0.45f)
        }
    }

    fun draw(scope: DrawScope, path: Path, color: Color, alpha: Float) {
        if (alpha <= MinAlpha) return
        for (k in strokes.indices) scope.drawPath(path, color, alpha = alpha * weights[k], style = strokes[k])
    }

    fun draw(scope: DrawScope, path: Path, brush: Brush, alpha: Float) {
        if (alpha <= MinAlpha) return
        for (k in strokes.indices) scope.drawPath(path, brush, alpha = alpha * weights[k], style = strokes[k])
    }
}

/**
 * A dash that flows along a path, like `stroke-dashoffset: 0 -> -(dash + gap)`. The dash phases are
 * precomputed in [steps] slots so the draw loop never allocates a path effect.
 */
internal class FlowDash(dash: Float, gap: Float, width: Float, sigma: Float, private val steps: Int) {
    private val strokes = Array(steps) { k ->
        SoftStroke(width, sigma, PathEffect.dashPathEffect(floatArrayOf(dash, gap), (dash + gap) * k / steps))
    }

    /** Stroke for [elapsedMs] into a loop of [periodMs]. */
    fun at(elapsedMs: Long, periodMs: Long): SoftStroke {
        val progress = (elapsedMs % periodMs).toFloat() / periodMs
        // A negative dash offset of `progress * period` equals a phase of `(1 - progress) * period`.
        val slot = Math.floorMod(((1f - progress) * steps).toInt(), steps)
        return strokes[slot]
    }
}

private const val MinAlpha = 0.004f

/** Radial brush of radius 1 at the origin; draw it with [drawEllipseGradient]. */
internal fun unitRadial(vararg stops: Pair<Float, Color>): Brush =
    Brush.radialGradient(*stops, center = Offset.Zero, radius = 1f)

/**
 * Draws a [unitRadial] brush stretched to an ellipse of radii [rx], [ry] around [center]. With
 * [fill] the clamped last stop also covers everything outside the ellipse (page background, vignette).
 */
internal fun DrawScope.drawEllipseGradient(
    brush: Brush,
    center: Offset,
    rx: Float,
    ry: Float,
    alpha: Float = 1f,
    fill: Boolean = false,
) {
    if (alpha <= MinAlpha) return
    withTransform({
        translate(center.x, center.y)
        scale(rx, ry, Offset.Zero)
    }) {
        if (fill) {
            drawRect(brush, topLeft = Offset(-FillReach, -FillReach), size = Size(2 * FillReach, 2 * FillReach), alpha = alpha)
        } else {
            drawCircle(brush, radius = 1f, center = Offset.Zero, alpha = alpha)
        }
    }
}

private const val FillReach = 4f

/** `mv-twinkle`: 0.15 -> 1 -> 0.15, ease-in-out per half; fully visible while its delay runs. */
internal fun twinkleAlpha(elapsedMs: Long, durationMs: Long, delayMs: Long): Float {
    if (elapsedMs < delayMs) return 1f
    val p = ((elapsedMs - delayMs) % durationMs).toFloat() / durationMs
    return if (p < 0.5f) {
        0.15f + 0.85f * MultiversoMotion.CssEaseInOut.transform(p * 2f)
    } else {
        1f - 0.85f * MultiversoMotion.CssEaseInOut.transform((p - 0.5f) * 2f)
    }
}

/** Eased progress of an `ease-in-out infinite alternate` (or `alternate-reverse`) animation. */
internal fun alternateProgress(elapsedMs: Long, durationMs: Long, reverse: Boolean): Float {
    val cycle = elapsedMs / durationMs
    val frac = (elapsedMs % durationMs).toFloat() / durationMs
    val forward = (cycle % 2L == 0L) != reverse
    return MultiversoMotion.CssEaseInOut.transform(if (forward) frac else 1f - frac)
}

/** Linear progress in [0, 1) of a loop, or -1 while its [delayMs] has not elapsed yet. */
internal fun loopProgress(elapsedMs: Long, periodMs: Long, delayMs: Long = 0L): Float =
    if (elapsedMs < delayMs) -1f else ((elapsedMs - delayMs) % periodMs).toFloat() / periodMs

internal fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
