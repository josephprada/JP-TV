package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate

/**
 * Street splashes and the rain sheet, drawn over the signs. The rain container sits at
 * (-200, -200), 2320x1500, rotated 9deg around its centre, as in the artboard.
 */
@Composable
internal fun CyberpunkWeather(city: CyberpunkCity, phase: CyberpunkPalette.Phase, time: State<Float>) {
    val dropBrushes = remember(phase) {
        city.drops.map {
            Brush.verticalGradient(
                listOf(CyberpunkPalette.Rain.copy(alpha = 0f), CyberpunkPalette.Rain.copy(alpha = phase.rainAlpha)),
                startY = 0f,
                endY = it.h,
            )
        }
    }
    val ring = remember { Stroke(width = 1f) }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val t = time.value
                inDesignPx {
                    city.splashes.forEach { s ->
                        val p = CyberpunkKeyframes.progress(t, s.dur, s.delay)
                        val e = if (p == null) null else CyberpunkMotion.CssEaseOut.transform(p)
                        val k = if (e == null) 1f else 0.2f + 1.3f * e
                        val a = if (e == null) 1f else 0.8f * (1f - e)
                        scale(k, pivot = Offset(s.x + 8f, s.y + 2.5f)) {
                            drawOval(CyberpunkPalette.Splash, Offset(s.x, s.y), Size(16f, 5f), alpha = a, style = ring)
                        }
                    }
                    translate(-200f, -200f) {
                        rotate(9f, pivot = Offset(1160f, 750f)) {
                            city.drops.forEachIndexed { i, d ->
                                val p = CyberpunkKeyframes.progress(t, d.dur, d.delay)
                                val y = if (p == null) 0f else -120f + 1620f * p
                                translate(d.x, y) {
                                    drawRect(dropBrushes[i], size = Size(1f, d.h))
                                }
                            }
                        }
                    }
                }
            },
    )
}
