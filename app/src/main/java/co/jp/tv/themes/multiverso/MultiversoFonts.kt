package co.jp.tv.themes.multiverso

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import co.jp.tv.R

/** Geist with the 300 weight the artboard's clock uses (JpFonts.Body starts at 400). */
@OptIn(ExperimentalTextApi::class)
internal object MultiversoFonts {
    val Geist: FontFamily by lazy {
        FontFamily(
            weighted(300),
            weighted(400),
            weighted(500),
            weighted(600),
        )
    }

    private fun weighted(weight: Int) = Font(
        resId = R.font.geist,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
}
