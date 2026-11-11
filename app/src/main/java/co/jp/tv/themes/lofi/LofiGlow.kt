package co.jp.tv.themes.lofi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Radial brush approximating CSS `box-shadow: 0 0 <blur> <spread> <color>` around a circle:
 * a disc of [radius] (spread included) blurred with sigma = blur / 2. Works on every API level.
 */
internal object LofiGlow {
    private const val STOPS = 12

    /** Outer radius the brush reaches; draw a circle of this radius around the centre. */
    fun extent(radius: Float, blur: Float): Float = radius + 1.5f * blur

    fun brush(center: Offset, radius: Float, blur: Float, color: Color): Brush {
        val sigma = blur / 2f
        val extent = extent(radius, blur)
        val stops = Array(STOPS + 1) { k ->
            val r = extent * k / STOPS
            val coverage = normalCdf((radius - r) / sigma)
            (k.toFloat() / STOPS) to color.copy(alpha = color.alpha * coverage)
        }
        return Brush.radialGradient(*stops, center = center, radius = extent)
    }

    private fun normalCdf(x: Float): Float = 0.5f * (1f + erf(x / sqrt(2f)))

    /** Abramowitz-Stegun 7.1.26, max error 1.5e-7. */
    private fun erf(x: Float): Float {
        val t = 1f / (1f + 0.3275911f * abs(x))
        val poly = ((((1.061405429f * t - 1.453152027f) * t + 1.421413741f) * t - 0.284496736f) * t + 0.254829592f) * t
        val y = 1f - poly * exp(-x * x)
        return if (x >= 0f) y else -y
    }
}
