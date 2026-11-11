package co.jp.tv.themes.oceano

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The artboard is 1920x1080 design px; on the TV one dp is two design px. */
internal object OceanoDesign {
    const val WIDTH = 1920f
    const val HEIGHT = 1080f

    fun dp(px: Float): Dp = (px / 2f).dp
}

/** Draws [block] in design px: the canvas is scaled so 1920 design px span its width. */
internal inline fun DrawScope.inDesignSpace(block: DrawScope.() -> Unit) {
    val k = size.width / OceanoDesign.WIDTH
    withTransform({ scale(k, k, Offset.Zero) }) { block() }
}
