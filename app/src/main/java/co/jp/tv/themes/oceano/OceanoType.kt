package co.jp.tv.themes.oceano

import androidx.compose.ui.text.font.FontFamily
import co.jp.tv.core.design.JpFonts

/** Fonts of the Océano home: one pixel font for every label, so the text matches the scene behind it. */
internal object OceanoType {
    /** VT323, a terminal pixel font that stays legible at 720p. */
    val Pixel: FontFamily get() = JpFonts.Vt323
}
