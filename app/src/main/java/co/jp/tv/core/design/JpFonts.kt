package co.jp.tv.core.design

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import co.jp.tv.R

/** JP design tokens: bundled fonts (all OFL, see assets/licenses). */
@OptIn(ExperimentalTextApi::class)
object JpFonts {
    val Display = FontFamily(Font(R.font.anton))

    val Body = FontFamily(
        weighted(R.font.geist, 400),
        weighted(R.font.geist, 500),
        weighted(R.font.geist, 600),
    )

    val Mono = FontFamily(
        weighted(R.font.geist_mono, 400),
        weighted(R.font.geist_mono, 500),
    )

    private fun weighted(res: Int, weight: Int) = Font(
        resId = res,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
}
