package co.jp.tv.themes.oceano

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import co.jp.tv.core.design.bakedLayer

/** Which static reef band a [OceanoReefLayer] draws. */
internal enum class OceanoReefBand { Far, Mid, Fore }

private val RippleStroke = Stroke(width = 1.8f)

/**
 * One static band of the reef, drawn once into a bitmap and blitted afterwards, so the looping
 * layers around it never pay for its paths again.
 */
@Composable
internal fun OceanoReefLayer(scenery: OceanoReefScenery, band: OceanoReefBand, modifier: Modifier = Modifier) {
    Spacer(
        modifier.fillMaxSize().bakedLayer {
            inDesignSpace {
                when (band) {
                    OceanoReefBand.Far -> drawFar(scenery)
                    OceanoReefBand.Mid -> drawMid(scenery)
                    OceanoReefBand.Fore -> drawFore(scenery)
                }
            }
        },
    )
}

private fun DrawScope.drawFar(s: OceanoReefScenery) {
    drawPath(s.farBack, OceanoPalette.FarRidgeBack)
    drawBumps(s.farBackBumps, OceanoPalette.FarRidgeBack)
    drawPath(s.farFront, OceanoPalette.FarRidgeFront)
    drawBumps(s.farFrontBumps, OceanoPalette.FarRidgeFront)
    s.farBommies.forEach { drawPath(it, OceanoPalette.FarBommie) }
}

private fun DrawScope.drawBumps(bumps: FloatArray, color: Color) {
    var i = 0
    while (i + 2 < bumps.size) {
        drawCircle(color, radius = bumps[i + 2], center = Offset(bumps[i], bumps[i + 1]))
        i += 3
    }
}

private fun DrawScope.drawMid(s: OceanoReefScenery) {
    drawRect(s.sandBrush, topLeft = Offset(-20f, 840f), size = Size(1960f, 260f))
    drawPath(s.rippleDark, OceanoPalette.SandRippleDark, alpha = 0.2f, style = RippleStroke)
    drawPath(s.rippleLight, OceanoPalette.SandRippleLight, alpha = 0.18f, style = RippleStroke)
    drawPath(s.midReef, s.midReefBrush)
    s.midDressing.draw(this)
}

private fun DrawScope.drawFore(s: OceanoReefScenery) {
    s.foreMounds.forEachIndexed { i, mound ->
        drawPath(mound, s.foreBrushes[i])
        s.foreDressings[i].draw(this)
    }
}
