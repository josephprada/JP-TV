package co.jp.tv.themes.gotham

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.jp.tv.apps.domain.TvApp
import kotlin.math.roundToInt

/** Card pitch: 260 px card + 40 px gap. The focused card is centred on x = 960. */
private const val StepPx = 300f
private const val CardHalfPx = 130f
private const val CenterPx = 960f

/** Cards further than this from focus are off screen even mid-slide, so they are not composed. */
private const val VisibleReach = 4

/** Width of the edge fade on each side: 12 % of the 1920 px artboard. */
private const val EdgeFadePx = 1920f * 0.12f

/**
 * The case-file strip: a 600 px band at y = 250 whose edges fade out
 * (`mask-image: linear-gradient(90deg, transparent, #000 12%, #000 88%, transparent)`), with the
 * cards on a track 110 px down that slides to keep the focused card centred.
 */
@Composable
internal fun GothamCaseFiles(apps: List<TvApp>, focusedIndex: Int, modifier: Modifier = Modifier) {
    val track by animateFloatAsState(
        focusedIndex.toFloat(),
        tween(GothamMotion.FocusMs, easing = GothamMotion.Focus),
        label = "track",
    )
    Box(
        modifier
            .offset(y = 125.dp)
            .fillMaxWidth()
            .height(300.dp)
            .clipToBounds()
    ) {
        if (apps.isEmpty()) return@Box
        val first = (focusedIndex - VisibleReach).coerceAtLeast(0)
        val last = (focusedIndex + VisibleReach).coerceAtMost(apps.lastIndex)
        for (index in first..last) {
            val app = apps[index]
            key(app.packageName) {
                GothamCaseFile(
                    name = app.label,
                    index = index,
                    focusedIndex = focusedIndex,
                    modifier = Modifier
                        .offset {
                            val k = 0.5.dp.toPx()
                            val x = CenterPx - (track * StepPx + CardHalfPx) + index * StepPx
                            IntOffset((x * k).roundToInt(), 55.dp.roundToPx())
                        }
                        // The artboard masks the outer 12 % of the screen; an offscreen mask layer is too costly on
                        // the TV, so each card fades by how close its centre is to the edge instead.
                        .graphicsLayer {
                            val centre = CenterPx + (index - track) * StepPx
                            alpha = (minOf(centre, 1920f - centre) / EdgeFadePx).coerceIn(0f, 1f)
                        },
                )
            }
        }
    }
}
