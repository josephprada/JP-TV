package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private const val TRACK_WIDTH = 800f
private const val TRACK_HEIGHT = 48f

/**
 * The line of ticks along the bottom, one per app: 6 px dots that swell to a 14 px glowing dot
 * on the focused one. [scroll] is the animated focus index, so the swell travels with the track.
 */
@Composable
internal fun OceanoScrubber(count: Int, scroll: State<Float>, modifier: Modifier = Modifier) {
    val line = remember {
        Brush.horizontalGradient(
            0f to OceanoPalette.ScrubLineClear,
            0.15f to OceanoPalette.ScrubLine,
            0.85f to OceanoPalette.ScrubLine,
            1f to OceanoPalette.ScrubLineClear,
            startX = -40f,
            endX = TRACK_WIDTH + 40f,
        )
    }
    Canvas(modifier.size(OceanoDesign.dp(TRACK_WIDTH), OceanoDesign.dp(TRACK_HEIGHT))) {
        scale(size.width / TRACK_WIDTH, pivot = Offset.Zero) {
            drawRect(line, topLeft = Offset(-40f, TRACK_HEIGHT / 2f - 0.5f), size = Size(TRACK_WIDTH + 80f, 1f))
            val position = scroll.value
            for (i in 0 until count) {
                val x = if (count > 1) i * TRACK_WIDTH / (count - 1) else TRACK_WIDTH / 2f
                val on = (1f - abs(position - i)).coerceIn(0f, 1f)
                val radius = OceanoMotion.lerp(6f, 14f, on) / 2f
                val center = Offset(x, TRACK_HEIGHT / 2f)
                if (on > 0f) {
                    val tint = OceanoPalette.jellyTint(i)
                    drawCircle(tint, radius = radius + 10f, center = center, alpha = 0.12f * on)
                    drawCircle(tint, radius = radius + 6f, center = center, alpha = 0.25f * on)
                    drawCircle(tint, radius = radius + 3f, center = center, alpha = 0.5f * on)
                }
                drawCircle(lerp(OceanoPalette.ScrubTick, OceanoPalette.White, on), radius = radius, center = center)
            }
        }
    }
}

/** "03 / 10", Geist Mono 14 px with 0.22em tracking. */
@Composable
internal fun OceanoPosition(index: Int, count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    BasicText(
        "%02d / %02d".format(index + 1, count),
        modifier = modifier,
        style = TextStyle(
            color = OceanoPalette.ScrubPosition,
            fontSize = 7.sp,
            fontFamily = OceanoType.Mono,
            letterSpacing = 0.22.em,
        ),
    )
}
