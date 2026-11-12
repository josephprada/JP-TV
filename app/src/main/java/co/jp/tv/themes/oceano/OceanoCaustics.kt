package co.jp.tv.themes.oceano

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import co.jp.tv.core.design.bakeToBitmap
import kotlin.math.ceil
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.drawscope.withTransform
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
    Spacer(
        modifier
            .fillMaxSize()
            .drawWithCache {
                // Each field is a static scatter of cells: bake it once at half resolution (the light is
                // soft anyway) and per frame only crossfade and drift two textured quads.
                val k = size.width / OceanoDesign.WIDTH
                val bakeScale = k * BAKE_RES
                val bitmaps = fields.map { field ->
                    bakeToBitmap(
                        ceil(BAKE_WIDTH * bakeScale).toInt(),
                        ceil((bottom - top) * bakeScale).toInt(),
                    ) {
                        withTransform({
                            scale(bakeScale, bakeScale, Offset.Zero)
                            translate(-BAKE_LEFT, -top)
                        }) {
                            var i = 0
                            while (i + 3 < field.size) {
                                withTransform({
                                    translate(field[i], field[i + 1])
                                    scale(field[i + 2], field[i + 2] * 0.38f, Offset.Zero)
                                }) {
                                    drawCircle(cellBrush, radius = 1f, center = Offset.Zero, alpha = field[i + 3].coerceIn(0f, 1f))
                                }
                                i += 4
                            }
                        }
                    }
                }
                val dstSize = IntSize(ceil(BAKE_WIDTH * k).toInt(), ceil((bottom - top) * k).toInt())
                onDrawBehind {
                    val bandAlpha = alpha()
                    if (bandAlpha <= 0.001f) return@onDrawBehind
                    val t = clock.seconds
                    // sw-fadeA: 0.9 -> 0.1 -> 0.9; sw-fadeB is its mirror.
                    val beat = OceanoMotion.pulse(t, fadePeriodS)
                    for (f in bitmaps.indices) {
                        val fieldAlpha = if (f == 0) 0.9f - 0.8f * beat else 0.1f + 0.8f * beat
                        val drift = when {
                            driftPeriodsS == null -> 0f
                            f == 0 -> -160f * OceanoMotion.alternate(t, driftPeriodsS.first)
                            else -> -160f * (1f - OceanoMotion.alternate(t, driftPeriodsS.second))
                        }
                        drawImage(
                            bitmaps[f],
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(bitmaps[f].width, bitmaps[f].height),
                            dstOffset = IntOffset(((BAKE_LEFT + drift) * k).roundToInt(), (top * k).roundToInt()),
                            dstSize = dstSize,
                            alpha = (fieldAlpha * bandAlpha).coerceIn(0f, 1f),
                            filterQuality = FilterQuality.Low,
                        )
                    }
                }
            },
    )
}

/** Baked at this fraction of device resolution. */
private const val BAKE_RES = 0.5f

/** Design px covered by a baked field: from [BAKE_LEFT] (cells start off screen) past the right edge plus the drift. */
private const val BAKE_LEFT = -260f
private const val BAKE_WIDTH = OceanoDesign.WIDTH + 160f + 260f

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
