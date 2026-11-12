package co.jp.tv.core.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.ceil

/**
 * Renders [draw] once into an offscreen bitmap of [widthPx] x [heightPx] and returns it. Static
 * scenery that costs many path/gradient ops becomes a single textured quad per frame.
 */
fun CacheDrawScope.bakeToBitmap(widthPx: Int, heightPx: Int, draw: DrawScope.() -> Unit): ImageBitmap {
    val bitmap = ImageBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1))
    CanvasDrawScope().draw(
        this,
        layoutDirection,
        Canvas(bitmap),
        Size(widthPx.toFloat(), heightPx.toFloat()),
    ) { draw() }
    return bitmap
}

/**
 * Draws [content] once into a bitmap and only blits that bitmap afterwards. Use it for layers that
 * never change after the first frame; [content] must not read state and draws in this node's pixel
 * space. [topFraction]..[bottomFraction] (of the node's height) limit the bitmap, and the blit, to a
 * horizontal band when the layer only covers part of the screen.
 */
fun Modifier.bakedLayer(
    topFraction: Float = 0f,
    bottomFraction: Float = 1f,
    content: DrawScope.() -> Unit,
): Modifier = drawWithCache {
    val top = (size.height * topFraction).toInt()
    val w = ceil(size.width).toInt()
    val h = ceil(size.height * bottomFraction).toInt() - top
    val baked = if (w > 0 && h > 0) {
        bakeToBitmap(w, h) { translate(top = -top.toFloat()) { content() } }
    } else {
        null
    }
    onDrawBehind {
        if (baked != null) drawImage(baked, topLeft = Offset(0f, top.toFloat()))
    }
}

/**
 * Like [bakedLayer], for scenery written in design px (the 1920x1080 artboard, one design px = half
 * a dp). Only the design-px rectangle [left], [top], [right], [bottom] is baked, which also lets a
 * layer that slides sideways keep the part that starts off screen. [content] draws in design px.
 */
fun Modifier.bakedDesignRegion(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    content: DrawScope.() -> Unit,
): Modifier = drawWithCache {
    val k = density / 2f
    val baked = bakeToBitmap(ceil((right - left) * k).toInt(), ceil((bottom - top) * k).toInt()) {
        scale(k, k, Offset.Zero) { translate(-left, -top) { content() } }
    }
    onDrawBehind { drawImage(baked, topLeft = Offset(left * k, top * k)) }
}
