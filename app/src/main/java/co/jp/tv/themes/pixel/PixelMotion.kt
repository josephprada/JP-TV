package co.jp.tv.themes.pixel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import co.jp.tv.core.design.JpMotion
import kotlinx.coroutines.delay

/** The shelf's move and focus easing, cubic-bezier(0.32, 0.72, 0, 1), over 550 ms. */
internal val PixelShelfEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
internal const val PixelShelfMs = 550

/** The design's pk-in: fade in and rise 16px, over 0.7 s after [delayMs]. */
@Composable
internal fun Modifier.pixelEntrance(delayMs: Int): Modifier {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        progress.animateTo(1f, tween(JpMotion.RiseMs, easing = JpMotion.Entrance))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = 8.dp.toPx() * (1f - progress.value)
    }
}

/** A hard (unblurred) text shadow [dyDp] below the glyphs. */
@Composable
internal fun rememberPixelShadow(color: androidx.compose.ui.graphics.Color, dyDp: Float): Shadow {
    val density = LocalDensity.current
    return remember(color, dyDp, density) { Shadow(color, Offset(0f, with(density) { dyDp.dp.toPx() }), 0f) }
}
