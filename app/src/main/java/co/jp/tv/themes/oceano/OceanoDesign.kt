package co.jp.tv.themes.oceano

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The artboard is 1920x1080 design px; on the TV one dp is two design px. */
internal object OceanoDesign {
    fun dp(px: Float): Dp = (px / 2f).dp
}
