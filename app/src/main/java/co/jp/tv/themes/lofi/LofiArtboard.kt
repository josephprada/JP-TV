package co.jp.tv.themes.lofi

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale

/**
 * Draws [block] in artboard px, the units of design/project/ThemeLofi.dc.html: the 1920x1080 px
 * artboard maps onto the 960x540 dp TV screen (dp = px / 2), so design numbers are used as written.
 */
internal inline fun DrawScope.inArtboardPx(block: DrawScope.() -> Unit) {
    val k = density / 2f
    scale(scaleX = k, scaleY = k, pivot = Offset.Zero, block = block)
}
