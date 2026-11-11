package co.jp.tv.themes.cyberpunk

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import co.jp.tv.R

/**
 * The design uses Chakra Petch, which is not bundled; Space Grotesk (variable, 300-700) is the
 * closest bundled squared grotesque. "Share Tech Mono" maps to JpFonts.Mono (Geist Mono).
 */
@OptIn(ExperimentalTextApi::class)
internal object CyberpunkFonts {
    val Display by lazy {
        FontFamily(
            weighted(500),
            weighted(600),
            weighted(700),
        )
    }

    private fun weighted(weight: Int) = Font(
        resId = R.font.space_grotesk,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
}
