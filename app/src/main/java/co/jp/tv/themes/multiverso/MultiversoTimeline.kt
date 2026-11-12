package co.jp.tv.themes.multiverso

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import co.jp.tv.core.design.bakedLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Horizon haze and the braided main timeline across the screen at y = 600, plus the white pulse
 * that flows along its trunk (`mv-flow-trunk`, 6 s). The braid is static and kept in its own layer;
 * only the pulse redraws with [frame].
 */
@Composable
internal fun MultiversoTimeline(frame: LongState, modifier: Modifier = Modifier) {
    val strands = remember { MultiversoGeometry.strands() }
    val res = remember { TimelineResources() }
    Box(modifier.fillMaxSize()) {
        // Static: baked once into a band around y = 600 (haze, glow and strands all stay within +-270 design px).
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = 330f / 1080f, bottomFraction = 870f / 1080f) {
            scale(density / 2f, pivot = Offset.Zero) {
                drawEllipseGradient(res.haze, Offset(960f, 600f), 1160f * Sqrt2, 130f * Sqrt2)
                val trunk = strands[0]
                res.trunkViolet.draw(this, trunk, MultiversoPalette.TrunkViolet, 0.45f)
                res.trunkCyan.draw(this, trunk, MultiversoPalette.TrunkCyan, 0.5f)
                res.trunkCore.draw(this, trunk, MultiversoPalette.TrunkCore, 0.7f)
                translate(top = 7f) {
                    res.fringe.draw(this, strands[2], MultiversoPalette.Fringe, 0.5f)
                }
                for (k in strands.indices) {
                    res.strands[k].draw(this, strands[k], MultiversoPalette.White, MultiversoGeometry.StrandAlphas[k])
                }
            }
        })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = frame.longValue
            scale(density / 2f, pivot = Offset.Zero) {
                res.flow.at(t, MultiversoMotion.TrunkFlowMs).draw(this, strands[0], MultiversoPalette.White, 1f)
            }
        }
    }
}

private const val Sqrt2 = 1.4142135f

private class TimelineResources {
    val haze = unitRadial(0f to MultiversoPalette.Haze, 0.68f to MultiversoPalette.Haze.copy(alpha = 0f))
    val trunkViolet = SoftStroke(width = 90f, sigma = 26f)
    val trunkCyan = SoftStroke(width = 22f, sigma = 8f)
    val trunkCore = SoftStroke(width = 6f, sigma = 3.5f)
    val fringe = SoftStroke(width = 1.4f, sigma = 0f)
    val strands = MultiversoGeometry.StrandWidths.map { SoftStroke(width = it, sigma = 0f) }

    /** `stroke-dasharray: 130 590`, width 5, blurred 1.2; 180 phase slots of 4 px. */
    val flow = FlowDash(dash = 130f, gap = 590f, width = 5f, sigma = 1.2f, steps = 180)
}
