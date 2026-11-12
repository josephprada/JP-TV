package co.jp.tv.themes.gotham

import androidx.compose.foundation.Canvas
import co.jp.tv.core.util.rememberLowEnd
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer

/** The rain sheet sits at (-200, -200), is 2320x1500 and is tilted 10deg about its centre. */
private const val SheetLeft = -200f
private const val SheetTop = -200f
private const val SheetTiltDeg = 10f
private val SheetCenter = Offset(SheetLeft + 2320f / 2f, SheetTop + 1500f / 2f)

/** Slanted rain over the whole home, in front of the case files (`ng-rain`, linear); [amount] is the share of drops falling. */
@Composable
internal fun GothamRain(drops: List<RainDrop>, time: () -> Long, modifier: Modifier = Modifier, amount: Float = 1f, color: Color = GothamPalette.Rain) {
    val lowEnd = rememberLowEnd()
    val flat = remember(color) { color.copy(alpha = 0.2f) }
    val count = GothamSky.drops(amount, drops.size)
    if (count == 0) return
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = time()
        inDesignSpace {
            rotate(SheetTiltDeg, pivot = SheetCenter) {
                for (i in 0 until count) {
                    // Low-end: two drops in three, as flat lines instead of a gradient each.
                    if (lowEnd && i % 3 == 2) continue
                    val d = drops[i]
                    val p = GothamMotion.loop(t, d.durationMs, d.delayMs)
                    val fall = if (p < 0f) 0f else GothamMotion.RainFromPx + (GothamMotion.RainToPx - GothamMotion.RainFromPx) * p
                    translate(SheetLeft + d.left, SheetTop + fall) {
                        if (lowEnd) drawRect(flat, size = d.size) else drawRect(d.brush, size = d.size)
                    }
                }
            }
        }
    }
}
