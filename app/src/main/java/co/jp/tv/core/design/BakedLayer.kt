package co.jp.tv.core.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * Draws [content] once into a bitmap the size of this node and only blits that bitmap afterwards.
 * Use it for layers that never change after the first frame; [content] must not read state.
 */
fun Modifier.bakedLayer(content: DrawScope.() -> Unit): Modifier = drawWithCache {
    val w = ceil(size.width).toInt()
    val h = ceil(size.height).toInt()
    val baked = if (w > 0 && h > 0) bakeToBitmap(w, h, content) else null
    onDrawBehind {
        if (baked != null) drawImage(baked)
    }
}
