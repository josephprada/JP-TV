package co.jp.tv.themes.pixel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.jp.tv.apps.domain.TvApp
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/** Cartridges kept composed on each side of the shelf position (the screen shows about three). */
private const val Margin = 4

/**
 * The cartridge shelf. The focused cartridge is always centred; the track slides there with the
 * design's 550 ms curve, and the first and last 12% of the width fade out.
 */
@Composable
internal fun PixelShelf(apps: List<TvApp>, focusedIndex: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val stepPx = with(density) { 140.dp.toPx() } // 240px cartridge + 40px gap in the design
    val cartridgePx = with(density) { 120.dp.toPx() }
    val topPx = with(density) { 75.dp.roundToPx() }
    val position by animateFloatAsState(
        focusedIndex.toFloat(),
        tween(PixelShelfMs, easing = PixelShelfEase),
        label = "pixelShelf",
    )
    val first by remember { derivedStateOf { floor(position).toInt() - Margin } }
    val last by remember { derivedStateOf { ceil(position).toInt() + Margin } }
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val centerPx = constraints.maxWidth / 2f
        for (index in first..last) {
            val app = apps.getOrNull(index) ?: continue
            key(app.packageName) {
                PixelCartridge(
                    label = app.label,
                    tint = PixelPalette.Tints[index % PixelPalette.Tints.size],
                    focused = index == focusedIndex,
                    distance = abs(index - focusedIndex),
                    modifier = Modifier
                        .offset {
                            IntOffset((centerPx - cartridgePx / 2f + (index - position) * stepPx).roundToInt(), topPx)
                        }
                        // The design masks the outer 12 % of the width; an offscreen mask layer is too costly on the
                        // TV, so each cartridge fades by how close its centre is to the edge instead.
                        .graphicsLayer {
                            val centre = centerPx + (index - position) * stepPx
                            alpha = (minOf(centre, 2f * centerPx - centre) / (0.12f * 2f * centerPx)).coerceIn(0f, 1f)
                        },
                )
            }
        }
    }
}
