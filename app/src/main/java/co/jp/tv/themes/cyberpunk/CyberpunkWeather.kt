package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import co.jp.tv.core.util.rememberLowEnd
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import co.jp.tv.themes.shared.Lightning

/** Cyberpunk's storms flash every 17 s, so they never line up with another theme's cycle; the flash is a soft wash. */
private const val LIGHTNING_PERIOD_MS = 17_000
private const val FLASH_ALPHA = 0.2f

/**
 * Street splashes and the rain sheet, drawn over the signs, thinned out to the weather's [sky] rain (none
 * when it is dry), plus its lightning. The rain container sits at (-200, -200), 2320x1500, rotated 9deg
 * around its centre, as in the artboard.
 */
@Composable
internal fun CyberpunkWeather(
    city: CyberpunkCity,
    phase: CyberpunkPalette.Phase,
    time: State<Float>,
    sky: CyberpunkSky = CyberpunkSky.Design,
) {
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
    val lowEnd = rememberLowEnd()
    // Low-end: two drops in three, drawn as flat lines instead of a gradient each.
    val flatDrop = remember(phase) { CyberpunkPalette.Rain.copy(alpha = phase.rainAlpha * 0.6f) }
    val splashCount = CyberpunkSky.count(sky.rain, city.splashes.size)
    val dropCount = CyberpunkSky.count(sky.rain, city.drops.size)
    if (splashCount == 0 && dropCount == 0 && !sky.lightning) return
    Spacer(
        Modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawBehind {
                val t = time.value
                inDesignPx {
                    for (n in 0 until splashCount) {
                        val s = city.splashes[n]
                        val p = CyberpunkKeyframes.progress(t, s.dur, s.delay)
                        val e = if (p == null) null else CyberpunkMotion.CssEaseOut.transform(p)
                        val k = if (e == null) 1f else 0.2f + 1.3f * e
                        val a = if (e == null) 1f else 0.8f * (1f - e)
                        scale(k, pivot = Offset(s.x + 8f, s.y + 2.5f)) {
                            drawOval(CyberpunkPalette.Splash, Offset(s.x, s.y), Size(16f, 5f), alpha = a, style = ring)
                        }
                    }
                    if (sky.lightning) {
                        val flash = Lightning.flash((t * 1000f).toLong(), LIGHTNING_PERIOD_MS)
                        if (flash > 0f) drawRect(CyberpunkPalette.Flash, Offset.Zero, Size(DESIGN_WIDTH, DESIGN_HEIGHT), alpha = FLASH_ALPHA * flash)
                    }
                    translate(-200f, -200f) {
                        rotate(9f, pivot = Offset(1160f, 750f)) {
                            for (i in 0 until dropCount) {
                                val d = city.drops[i]
                                if (lowEnd && i % 3 == 2) continue
                                val p = CyberpunkKeyframes.progress(t, d.dur, d.delay)
                                val y = if (p == null) 0f else -120f + 1620f * p
                                translate(d.x, y) {
                                    if (lowEnd) drawRect(flatDrop, size = Size(1f, d.h)) else drawRect(dropBrushes[i], size = Size(1f, d.h))
                                }
                            }
                        }
                    }
                }
            },
    )
}
