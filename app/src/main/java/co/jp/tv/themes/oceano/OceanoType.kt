package co.jp.tv.themes.oceano

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts

/** Fonts of the Océano home: Geist for text and Geist Mono for the position counter. */
@OptIn(ExperimentalTextApi::class)
internal object OceanoType {
    /** Geist at 400, 500 and 600, as registered by the design tokens. */
    val Geist: FontFamily = JpFonts.Body

    /** Geist Mono, used by the "03 / 10" counter. */
    val Mono: FontFamily = JpFonts.Mono

    /** Geist at weight 300 (the top-right clock), which [JpFonts.Body] does not register. */
    val Light: FontFamily by lazy {
        FontFamily(
            Font(
                resId = R.font.geist,
                weight = FontWeight.Light,
                variationSettings = FontVariation.Settings(FontVariation.weight(300)),
            ),
        )
    }
}
