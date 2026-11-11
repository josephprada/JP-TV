package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.random.Random

/**
 * Bubbles rising from three vents on the reef (`sw-rise`, linear, 8-13 s): fade in by 8 %,
 * wobble 14 px right by mid-way, end 10 px left at the top while fading out.
 */
@Composable
internal fun OceanoBubbles(clock: OceanoClock, modifier: Modifier = Modifier) {
    val highlight = remember {
        Brush.radialGradient(
            0f to OceanoPalette.BubbleHighlight,
            0.6f to OceanoPalette.BubbleClear,
            center = Offset(-0.3f, -0.4f),
            radius = 1.2f,
        )
    }
    // x, size, period, delay.
    val bubbles = remember {
        val random = Random(9)
        val vents = floatArrayOf(108f, 14f, 550f, 6f, 1762f, 12f)
        val out = ArrayList<Float>()
        var v = 0
        while (v < vents.size) {
            repeat(vents[v + 1].toInt()) {
                out += vents[v] + (random.nextFloat() - 0.5f) * 30f
                out += 4f + random.nextInt(10)
                out += 8.2f + random.nextFloat() * 5f
                out += random.nextFloat() * 13f
            }
            v += 2
        }
        out.toFloatArray()
    }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        inDesignSpace {
            var i = 0
            while (i + 3 < bubbles.size) {
                val p = OceanoMotion.loop(t, bubbles[i + 2], bubbles[i + 3])
                val dx = if (p < 0.5f) 28f * p else OceanoMotion.lerp(14f, -10f, (p - 0.5f) * 2f)
                val dy = if (p < 0.5f) -1040f * p else OceanoMotion.lerp(-520f, -1080f, (p - 0.5f) * 2f)
                val alpha = if (p < 0.08f) 0.9f * p / 0.08f else 0.9f * (1f - (p - 0.08f) / 0.92f)
                val r = bubbles[i + 1] / 2f
                translate(bubbles[i] + r + dx, 1000f + r + dy) {
                    drawCircle(OceanoPalette.BubbleRim, radius = r, center = Offset.Zero, alpha = alpha, style = RimStroke)
                    scale(r, r, Offset.Zero) {
                        drawCircle(highlight, radius = 1f, center = Offset.Zero, alpha = alpha)
                    }
                }
                i += 4
            }
        }
    }
}

private val RimStroke = Stroke(width = 1f)
