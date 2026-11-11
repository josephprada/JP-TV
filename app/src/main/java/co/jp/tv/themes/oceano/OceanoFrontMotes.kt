package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Out-of-focus particles in front of the jellies (`sw-snow`), then the bottom fade and the
 * vignette that frame the artboard. The design blurs each mote by 3 px; the radial falloff
 * already reads as out of focus.
 */
@Composable
internal fun OceanoFrontMotes(clock: OceanoClock, modifier: Modifier = Modifier) {
    val mote = remember {
        Brush.radialGradient(
            0f to OceanoPalette.FrontParticle,
            0.7f to OceanoPalette.FrontParticleEdge,
            center = Offset.Zero,
            radius = 1f,
        )
    }
    val bottomFade = remember {
        Brush.verticalGradient(
            listOf(OceanoPalette.BottomFadeTop, OceanoPalette.BottomFadeBottom),
            startY = OceanoDesign.HEIGHT - 200f,
            endY = OceanoDesign.HEIGHT,
        )
    }
    val vignette = remember {
        // radial-gradient(85% 80% at 50% 38%): stretched horizontally by drawing under a scale.
        Brush.radialGradient(
            0.5f to OceanoPalette.VignetteClear,
            1f to OceanoPalette.VignetteEdge,
            center = Offset.Zero,
            radius = 1f,
        )
    }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        inDesignSpace {
            var i = 0
            while (i + 3 < Motes.size) {
                val p = OceanoMotion.alternate(t, Motes[i + 3], i * 0.37f)
                val r = Motes[i + 2] / 2f
                translate(Motes[i] + r + 26f * p, Motes[i + 1] + r + 70f * p) {
                    scale(r, r, Offset.Zero) { drawCircle(mote, radius = 1f, center = Offset.Zero) }
                }
                i += 4
            }
            drawRect(bottomFade, topLeft = Offset(0f, OceanoDesign.HEIGHT - 200f), size = Size(OceanoDesign.WIDTH, 200f))
            translate(OceanoDesign.WIDTH * 0.5f, OceanoDesign.HEIGHT * 0.38f) {
                scale(OceanoDesign.WIDTH * 0.85f, OceanoDesign.HEIGHT * 0.8f, Offset.Zero) {
                    drawRect(vignette, topLeft = Offset(-1f, -1f), size = Size(2f, 2f))
                }
            }
        }
    }
}

/** The artboard's fourteen motes: left, top, size, `sw-snow` period. */
private val Motes = floatArrayOf(
    1731f, 542f, 29f, 19.1f, 1571f, 487f, 29f, 13.7f, 1766f, 485f, 30f, 13.7f, 1798f, 661f, 29f, 16.8f,
    1504f, 869f, 39f, 10.6f, 20f, 375f, 39f, 16.5f, 649f, 67f, 26f, 13.1f, 555f, 190f, 23f, 13.3f,
    1551f, 698f, 15f, 16.4f, 10f, 393f, 20f, 10.6f, 466f, 338f, 20f, 13.1f, 1242f, 28f, 40f, 17.0f,
    113f, 188f, 38f, 12.8f, 1287f, 141f, 36f, 19.4f,
)
