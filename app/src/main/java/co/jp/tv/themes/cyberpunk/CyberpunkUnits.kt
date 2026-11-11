package co.jp.tv.themes.cyberpunk

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** The artboard is 1920x1080 px; like every theme, dp (and sp) are half of the design px. */
internal const val DESIGN_WIDTH = 1920f
internal const val DESIGN_HEIGHT = 1080f

/** Design px to dp. */
internal val Int.dpx: Dp get() = (this / 2f).dp

/** Design px to dp. */
internal val Float.dpx: Dp get() = (this / 2f).dp

/** Design px font size to sp. */
internal val Int.spx: TextUnit get() = (this / 2f).sp

/** Draws [block] in design px (0..1920 x 0..1080) on a full-screen canvas. */
internal inline fun DrawScope.inDesignPx(block: DrawScope.() -> Unit) {
    scale(0.5.dp.toPx(), pivot = Offset.Zero, block = block)
}
