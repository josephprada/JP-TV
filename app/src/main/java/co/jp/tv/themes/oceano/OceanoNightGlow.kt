package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Night: the dark tint over the scene, then glowing coral tips and bioluminescent plankton
 * twinkling (`sw-tw`, 0.25 -> 1 -> 0.25). Both fade in with [night] (0 day, 1 night); by day
 * nothing is drawn.
 */
@Composable
internal fun OceanoNightGlow(
    clock: OceanoClock,
    scenery: OceanoReefScenery,
    night: () -> Float,
    modifier: Modifier = Modifier,
) {
    val tint = remember { Brush.verticalGradient(listOf(OceanoPalette.NightTintTop, OceanoPalette.NightTintBottom)) }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val n = night()
        if (n <= 0.001f) return@Canvas
        drawRect(tint, alpha = n)
        val t = clock.seconds
        val lights = scenery.nightLights
        inDesignSpace {
            var i = 0
            while (i + 5 < lights.size) {
                val color = OceanoPalette.GlowColors[lights[i + 3].toInt()]
                val twinkle = 0.25f + 0.75f * OceanoMotion.pulse(t, lights[i + 4], lights[i + 5])
                val center = Offset(lights[i], lights[i + 1])
                val r = lights[i + 2]
                if (!clock.lite) drawCircle(color, radius = r + 5f, center = center, alpha = 0.18f * twinkle * n)
                drawCircle(color, radius = r + 2f, center = center, alpha = 0.4f * twinkle * n)
                drawCircle(color, radius = r, center = center, alpha = twinkle * n)
                i += 6
            }
        }
    }
}
