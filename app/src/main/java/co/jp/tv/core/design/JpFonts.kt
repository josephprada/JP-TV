package co.jp.tv.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import co.jp.tv.R
import co.jp.tv.preferences.domain.TitleFonts

/** JP design tokens: bundled fonts (all OFL, see assets/licenses). */
@OptIn(ExperimentalTextApi::class)
object JpFonts {
    val Display = FontFamily(Font(R.font.anton))

    val BebasNeue = FontFamily(Font(R.font.bebas_neue))
    val ArchivoBlack = FontFamily(Font(R.font.archivo_black))
    val SpaceGrotesk = FontFamily(weighted(R.font.space_grotesk, 700))
    val InstrumentSerif = FontFamily(Font(R.font.instrument_serif))

    val Body = FontFamily(
        weighted(R.font.geist, 400),
        weighted(R.font.geist, 500),
        weighted(R.font.geist, 600),
    )

    val Mono = FontFamily(
        weighted(R.font.geist_mono, 400),
        weighted(R.font.geist_mono, 500),
    )

    fun title(id: String): FontFamily = when (id) {
        "bebas_neue" -> BebasNeue
        "archivo_black" -> ArchivoBlack
        "space_grotesk" -> SpaceGrotesk
        "instrument_serif" -> InstrumentSerif
        else -> Display
    }

    private fun weighted(res: Int, weight: Int) = Font(
        resId = res,
        weight = FontWeight(weight),
        variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
    )
}

@Composable
fun rememberTitleFont(id: String): FontFamily {
    val resolved = TitleFonts.byId(id).id
    return remember(resolved) { JpFonts.title(resolved) }
}
