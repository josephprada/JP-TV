package co.jp.tv.themes.lofi

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.roundToInt

// lo-grain, 0.8 s steps(4): translate in artboard px at 0 / 25 / 50 / 75 %.
private const val GRAIN_PERIOD_MS = 800L
private const val GRAIN_STEP_MS = 200L
private val GrainDx = floatArrayOf(0f, -8f, 6f, -4f)
private val GrainDy = floatArrayOf(0f, 6f, -4f, -8f)
private const val GRAIN_INSET = 40f

/**
 * Film grain (1 px white dots at 6 % every 4 px, jittering; skipped when [showGrain] is false, as on low-end
 * boxes where it costs a full-screen textured pass) and the vignette
 * `radial-gradient(120% 100% at 50% 50%, transparent 55%, rgba(0,0,0,0.45) 100%)`.
 */
@Composable
internal fun LofiFilmGrain(millis: LongState, showGrain: Boolean = true) {
    // Redraw only when the step changes, not on every frame.
    val step by remember(millis) {
        derivedStateOf { (millis.longValue.mod(GRAIN_PERIOD_MS) / GRAIN_STEP_MS).toInt() }
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val k = density / 2f
                val cell = (4f * k).roundToInt().coerceAtLeast(2)
                val tile = ImageBitmap(cell, cell)
                val dotPaint = Paint().apply {
                    color = LofiPalette.Grain
                    isAntiAlias = true
                }
                Canvas(tile).drawCircle(Offset(cell / 2f, cell / 2f), cell / 4f, dotPaint)
                val grain = ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated))
                val vignette = Brush.radialGradient(
                    0.55f to LofiPalette.VignetteClear,
                    1f to LofiPalette.VignetteEdge,
                    center = size.center,
                    radius = size.height,
                )
                val stretch = 1.2f * size.width / size.height
                val inset = GRAIN_INSET * k
                onDrawBehind {
                    if (showGrain) translate(left = GrainDx[step] * k, top = GrainDy[step] * k) {
                        drawRect(
                            grain,
                            topLeft = Offset(-inset, -inset),
                            size = Size(size.width + 2 * inset, size.height + 2 * inset),
                        )
                    }
                    scale(scaleX = stretch, scaleY = 1f, pivot = center) {
                        drawRect(vignette)
                    }
                }
            },
    )
}
