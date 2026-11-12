package co.jp.tv.themes.oceano

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import co.jp.tv.apps.domain.TvApp
import kotlin.math.abs

/**
 * Cells farther than this from the focused one are invisible (a jelly 4 cells out sits past the screen edge,
 * where the edge fade is already 0) and not composed.
 */
private const val VISIBLE_RADIUS = 3

/** Where the jelly box sits inside its 280 px cell, and its top on the artboard (design px). */
private const val CELL_INSET = 40f
private const val JELLY_TOP = 230f

/** Offset from the cell's left edge to the centre of the jelly's glow, so the focused one centres. */
private const val CELL_CENTER = 140f

/** Width of the edge fade on each side: 10 % of the 1920 px artboard. */
private const val EDGE_FADE = 192f

/**
 * The row of moon jellies, one per app. [scroll] is the animated index of the focused app: the
 * track slides in 280 px steps so that jelly sits at the middle of the screen. The row fades
 * out over the outer 10 % of the screen on each side (the artboard's mask).
 */
@Composable
internal fun OceanoJellyTrack(
    apps: List<TvApp>,
    focusedIndex: Int,
    scroll: State<Float>,
    clock: OceanoClock,
    night: () -> Float,
    modifier: Modifier = Modifier,
) {
    val art = remember { OceanoJellyArt.build() }
    Box(modifier.fillMaxSize()) {
        for (i in apps.indices) {
            if (abs(i - focusedIndex) > VISIBLE_RADIUS) continue
            key(apps[i].packageName) {
                OceanoJellyCell(i, apps[i].label, focusedIndex, scroll, clock, art, night)
            }
        }
    }
}

@Composable
private fun OceanoJellyCell(
    index: Int,
    label: String,
    focusedIndex: Int,
    scroll: State<Float>,
    clock: OceanoClock,
    art: OceanoJellyArt,
    night: () -> Float,
) {
    val spec = tween<Float>(OceanoMotion.ScrollMs, easing = OceanoMotion.Scroll)
    val on = index == focusedIndex
    val focus = animateFloatAsState(if (on) 1f else 0f, spec, label = "oceanoJellyFocus")
    // opacity: 1 focused, .95 next to it, .85 elsewhere.
    val dim = animateFloatAsState(
        when (abs(index - focusedIndex)) {
            0 -> 1f
            1 -> 0.95f
            else -> 0.85f
        },
        spec,
        label = "oceanoJellyDim",
    )
    val designToPx = with(LocalDensity.current) { OceanoDesign.dp(1f).toPx() }
    Box(
        Modifier.graphicsLayer {
            val cellLeft = OceanoDesign.WIDTH / 2f - (scroll.value * OceanoMotion.StepPx + CELL_CENTER) + index * OceanoMotion.StepPx
            translationX = (cellLeft + CELL_INSET) * designToPx
            translationY = JELLY_TOP * designToPx
            // The artboard masks the outer 10 % of the screen; an offscreen mask layer is too costly on
            // the TV, so each jelly fades by how close its centre is to the edge instead.
            val centre = OceanoDesign.WIDTH / 2f + (index - scroll.value) * OceanoMotion.StepPx
            val edge = minOf(centre, OceanoDesign.WIDTH - centre)
            alpha = dim.value * (edge / EDGE_FADE).coerceIn(0f, 1f)
            compositingStrategy = CompositingStrategy.ModulateAlpha
        },
    ) {
        OceanoJelly(
            index = index,
            label = label,
            tint = OceanoPalette.jellyTint(index),
            art = art,
            clock = clock,
            focus = { focus.value },
            night = night,
            focusedNow = on,
        )
    }
}
