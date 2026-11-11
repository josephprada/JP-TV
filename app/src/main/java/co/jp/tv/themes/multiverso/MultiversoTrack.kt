package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.jp.tv.apps.domain.TvApp
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The app list: one nexus node every 300 design px along the timeline, the focused one centered at
 * x = 960 (`translateX(960 - (focus * 300 + 150))`, 0.7 s). Edges fade out like the artboard's
 * `mask-image` (transparent, 10 %, 90 %, transparent). Only nodes near the track position are
 * composed, so long app lists stay cheap.
 */
@Composable
internal fun MultiversoTrack(
    apps: List<TvApp>,
    focusedIndex: Int,
    frame: LongState,
    modifier: Modifier = Modifier,
) {
    val position = animateFloatAsState(
        focusedIndex.toFloat(),
        tween(MultiversoMotion.FocusMs, easing = MultiversoMotion.Swift),
        label = "multiversoTrack",
    )
    val center by remember { derivedStateOf { position.value.roundToInt() } }
    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithCache {
                val mask = Brush.horizontalGradient(
                    0f to Color.Transparent,
                    0.1f to Color.Black,
                    0.9f to Color.Black,
                    1f to Color.Transparent,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(mask, blendMode = BlendMode.DstIn)
                }
            },
    ) {
        val first = (center - VisibleReach).coerceAtLeast(0)
        val last = (center + VisibleReach).coerceAtMost(apps.lastIndex)
        for (i in first..last) {
            key(i) {
                MultiversoNode(
                    index = i,
                    label = apps[i].label,
                    selected = i == focusedIndex,
                    neighbor = abs(i - focusedIndex) == 1,
                    frame = frame,
                    // Node box left edge: screen center (480 dp) minus half the box (175 dp).
                    modifier = Modifier.offset {
                        IntOffset((305f + (i - position.value) * 150f).dp.roundToPx(), 0)
                    },
                )
            }
        }
    }
}

/** Nodes composed on each side of the track position; a node and its streak reach about 4.4 slots. */
private const val VisibleReach = 5
