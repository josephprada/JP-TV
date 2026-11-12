package co.jp.tv.themes.oceano

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.launchOrigin
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The app names under the jellies, in the pixel font at screen resolution. Each one is a text layer that
 * only moves: its position, scale and edge fade are set in a graphicsLayer block from [scroll], so a scroll
 * never re-lays-out or re-records the text. [pxPerPixel] is how many screen pixels one virtual pixel covers.
 * An empty box over the focused jelly marks where the launch animation should grow from.
 */
@Composable
internal fun OceanoJellyLabels(
    apps: List<TvApp>,
    focusedIndex: Int,
    scroll: State<Float>,
    pxPerPixel: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val shadowPx = with(density) { 2.dp.toPx() }
    val style = remember(shadowPx) {
        TextStyle(
            color = OceanoPalette.White,
            fontSize = 34.sp,
            lineHeight = 34.sp,
            fontFamily = OceanoType.Pixel,
            shadow = Shadow(OceanoPalette.TextShadow, Offset(shadowPx, shadowPx), 0f),
        )
    }
    Box(modifier.fillMaxSize()) {
        for (i in apps.indices) {
            if (abs(i - focusedIndex) > OceanoLayout.VISIBLE_RADIUS) continue
            key(apps[i].packageName) {
                BasicText(
                    apps[i].label,
                    modifier = Modifier.graphicsLayer {
                        val s = scroll.value
                        val focus = OceanoLayout.focusOf(i, s)
                        val cx = OceanoLayout.jellyX(i, s)
                        translationX = cx * pxPerPixel - size.width / 2f
                        translationY = OceanoLayout.labelY(focus) * pxPerPixel
                        val k = 0.6f + 0.4f * focus
                        scaleX = k
                        scaleY = k
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        alpha = ((minOf(cx, OceanoPixels.W - 1 - cx) - 4) / 30f).coerceIn(0f, 1f) * (0.8f + 0.2f * focus)
                        compositingStrategy = CompositingStrategy.ModulateAlpha
                    },
                    style = style,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        if (focusedIndex in apps.indices) {
            val big = OceanoSprites.JELLY_SIZES[2]
            val width = with(density) { (big.bellW * pxPerPixel).toDp() }
            val height = with(density) { ((big.bellH + big.tentacles) * pxPerPixel).toDp() }
            Box(
                Modifier
                    .offset { IntOffset(((OceanoLayout.jellyX(focusedIndex, scroll.value) - big.bellW / 2f) * pxPerPixel).roundToInt(), (OceanoLayout.bellTop(2) * pxPerPixel).roundToInt()) }
                    .size(width, height)
                    .launchOrigin(true),
            )
        }
    }
}

/** "03 / 10" at the bottom right, in the pixel font. */
@Composable
internal fun OceanoPosition(index: Int, count: Int, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val shadowPx = with(LocalDensity.current) { 1.5.dp.toPx() }
    BasicText(
        "%02d / %02d".format(index + 1, count),
        modifier = modifier,
        style = TextStyle(
            color = OceanoPalette.ScrubPosition,
            fontSize = 18.sp,
            fontFamily = OceanoType.Pixel,
            shadow = Shadow(OceanoPalette.TextShadow, Offset(shadowPx, shadowPx), 0f),
        ),
    )
}
