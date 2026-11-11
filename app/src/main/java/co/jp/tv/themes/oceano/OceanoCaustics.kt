package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

/**
 * Shimmering caustic light in a horizontal band (design px [top] to [bottom]). The artboard
 * renders two `feTurbulence` noise fields crossfading (`sw-fadeA` / `sw-fadeB`) and, at the
 * surface, drifting 160 px. Turbulence filters are too costly per frame on a TV, so each field
 * is a fixed scatter of soft light cells with the same size, crossfade and drift.
 *
 * [fadeDown] fades the band out towards its bottom (surface); otherwise it fades in from its top (sand).
 */
@Composable
internal fun OceanoCaustics(
    clock: OceanoClock,
    top: Float,
    bottom: Float,
    fadePeriodS: Float,
    driftPeriodsS: Pair<Float, Float>?,
    cellWidth: Float,
    fadeDown: Boolean,
    alpha: () -> Float,
    modifier: Modifier = Modifier,
) {
    val cellBrush = remember {
        Brush.radialGradient(
            0f to OceanoPalette.Caustic,
            0.45f to OceanoPalette.Caustic.copy(alpha = 0.35f),
            1f to Color.Transparent,
            center = Offset.Zero,
            radius = 1f,
        )
    }
    val fields = remember(top, bottom, cellWidth, fadeDown) {
        listOf(cells(17, top, bottom, cellWidth, fadeDown), cells(29, top, bottom, cellWidth, fadeDown))
    }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        val bandAlpha = alpha()
        if (bandAlpha <= 0.001f) return@Canvas
        inDesignSpace {
            clipRect(0f, top, OceanoDesign.WIDTH, bottom) {
                for (f in fields.indices) {
                    val field = fields[f]
                    // sw-fadeA: 0.9 -> 0.1 -> 0.9; sw-fadeB is its mirror.
                    val beat = OceanoMotion.pulse(t, fadePeriodS)
                    val fieldAlpha = if (f == 0) 0.9f - 0.8f * beat else 0.1f + 0.8f * beat
                    val drift = when {
                        driftPeriodsS == null -> 0f
                        f == 0 -> -160f * OceanoMotion.alternate(t, driftPeriodsS.first)
                        else -> -160f * (1f - OceanoMotion.alternate(t, driftPeriodsS.second))
                    }
                    var i = 0
                    while (i + 3 < field.size) {
                        val a = (field[i + 3] * fieldAlpha * bandAlpha).coerceIn(0f, 1f)
                        withTransform({
                            translate(field[i] + drift, field[i + 1])
                            scale(field[i + 2], field[i + 2] * 0.38f, Offset.Zero)
                        }) {
                            drawCircle(cellBrush, radius = 1f, center = Offset.Zero, alpha = a)
                        }
                        i += 4
                    }
                }
            }
        }
    }
}

/** Light cells packed as x, y, half-width, alpha (alpha already includes the band's fade). */
private fun cells(seed: Int, top: Float, bottom: Float, cellWidth: Float, fadeDown: Boolean): FloatArray {
    val random = Random(seed)
    val height = bottom - top
    val out = ArrayList<Float>()
    var y = top + cellWidth * 0.1f
    while (y < bottom) {
        var x = -cellWidth + random.nextFloat() * cellWidth
        while (x < OceanoDesign.WIDTH + 200f) {
            val depth = ((y - top) / height).coerceIn(0f, 1f)
            val fade = if (fadeDown) 1f - depth else (depth / 0.4f).coerceAtMost(1f)
            out += x
            out += y + (random.nextFloat() - 0.5f) * cellWidth * 0.2f
            out += cellWidth * (0.35f + random.nextFloat() * 0.3f)
            out += fade * (0.25f + random.nextFloat() * 0.35f)
            x += cellWidth * (0.6f + random.nextFloat() * 0.5f)
        }
        y += cellWidth * 0.28f
    }
    return out.toFloatArray()
}
