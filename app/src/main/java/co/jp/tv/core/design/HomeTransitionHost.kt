package co.jp.tv.core.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Wraps the theme's home. While a launch plays it zooms the home toward the focused tile and fades it
 * out, and an accent glow blooms over the tile. After a return it fades and scales the home in from 0.96.
 * Allocates nothing per frame: the glow brush is built once per size and accent.
 */
@Composable
fun HomeTransitionHost(
    transition: HomeTransition,
    accent: Color,
    lowEnd: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalHomeTransition provides transition) {
        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val out = transition.launchProgress
                        val back = transition.returnProgress
                        // The fade-in is quicker than the scale so the staggered rise is visible.
                        alpha = (1f - out) * (back * 2.5f).coerceAtMost(1f)
                        val s = (1f + 0.06f * out) * (0.96f + 0.04f * back)
                        scaleX = s
                        scaleY = s
                        translationY = (1f - back) * 10.dp.toPx()
                        transformOrigin = TransformOrigin(transition.pivotX, transition.pivotY)
                        // A fade of the whole home is cheaper without an offscreen buffer on a weak GPU.
                        compositingStrategy = if (lowEnd) CompositingStrategy.ModulateAlpha else CompositingStrategy.Auto
                    },
            ) { content() }
            if (transition.drawsGlow) {
                Box(Modifier.fillMaxSize().launchGlow(transition, accent))
            }
        }
    }
}

/** Accent bloom centred on the tile plus a ghost of the tile growing and fading; peaks mid-launch. */
private fun Modifier.launchGlow(transition: HomeTransition, accent: Color): Modifier = drawWithCache {
    val radius = size.maxDimension * 0.55f
    val brush = Brush.radialGradient(
        0f to accent.copy(alpha = 0.55f),
        0.45f to accent.copy(alpha = 0.18f),
        1f to accent.copy(alpha = 0f),
        center = Offset.Zero,
        radius = radius,
    )
    val corner = CornerRadius(14.dp.toPx())
    onDrawBehind {
        val out = transition.launchProgress
        if (out > 0.001f && out < 0.999f) {
            val amount = sin(PI * out).toFloat()
            translate(transition.pivotX * size.width, transition.pivotY * size.height) {
                scale(0.35f + 0.65f * out, pivot = Offset.Zero) {
                    drawCircle(brush, radius = radius, center = Offset.Zero, alpha = amount)
                }
                val w = transition.tileWidth
                if (w > 0f) {
                    val grow = 1f + 0.5f * out
                    val gw = w * grow
                    val gh = transition.tileHeight * grow
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(-gw / 2f, -gh / 2f),
                        size = Size(gw, gh),
                        cornerRadius = corner,
                        alpha = 0.5f * amount,
                    )
                }
            }
        }
    }
}
