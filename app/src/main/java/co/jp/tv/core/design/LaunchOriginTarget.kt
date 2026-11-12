package co.jp.tv.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Optional theme hook: mark the focused app's tile ([active] true only for it). The launch animation then
 * zooms into the tile and the window animation grows from it; without it the animation uses the screen centre.
 */
@Composable
fun Modifier.launchOrigin(active: Boolean): Modifier {
    val transition = LocalHomeTransition.current ?: return this
    if (!active) return this
    val holder = remember { OriginHolder() }
    SideEffect { transition.registerOrigin(holder) }
    return onGloballyPositioned { holder.coordinates = it }
}

/**
 * Optional theme hook: this block rises into place after a return from an app, [order] steps after the
 * previous one (the design's jp-rise: fade and a short upward move). Without it the block just rides the
 * home-wide fade and scale.
 */
@Composable
fun Modifier.returnStagger(order: Int, distanceDp: Float = 10f): Modifier {
    val transition = LocalHomeTransition.current ?: return this
    val distancePx = with(LocalDensity.current) { distanceDp.dp.toPx() }
    return graphicsLayer {
        val back = transition.returnProgress
        if (back < 1f) {
            val p = ((back * HomeTransition.ReturnMs - order * HomeTransition.StaggerMs) / StaggerSpanMs).coerceIn(0f, 1f)
            alpha = p
            translationY = (1f - p) * distancePx
        }
    }
}

private const val StaggerSpanMs = 260f
