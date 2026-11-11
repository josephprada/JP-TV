package co.jp.tv.themes.pixel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Label colors of one cartridge: gradient [top] to [bottom], with [text] drawn over it. */
@Immutable
internal data class PixelTint(val top: Color, val bottom: Color, val text: Color)
