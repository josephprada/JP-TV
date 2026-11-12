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
    /** Eager: used on the default JP-TV home from the first frame. */
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

    /** Lazy: customize / alternate themes only. Avoids decoding every TTF on cold start. */
    val BebasNeue by lazy { FontFamily(Font(R.font.bebas_neue)) }
    val ArchivoBlack by lazy { FontFamily(Font(R.font.archivo_black)) }
    val SpaceGrotesk by lazy { FontFamily(weighted(R.font.space_grotesk, 700)) }
    val InstrumentSerif by lazy { FontFamily(Font(R.font.instrument_serif)) }

    val PressStart2P by lazy { FontFamily(Font(R.font.press_start_2p)) }
    val Vt323 by lazy { FontFamily(Font(R.font.vt323)) }
    val Limelight by lazy { FontFamily(Font(R.font.limelight)) }
    val Orbitron by lazy { FontFamily(Font(R.font.orbitron)) }
    val Righteous by lazy { FontFamily(Font(R.font.righteous)) }
    val SpecialElite by lazy { FontFamily(Font(R.font.special_elite)) }
    val Fredoka by lazy { FontFamily(weighted(R.font.fredoka, 600), weighted(R.font.fredoka, 700)) }
    val ShareTechMono by lazy { FontFamily(Font(R.font.share_tech_mono)) }
    val DmMono by lazy {
        FontFamily(
            Font(R.font.dm_mono, FontWeight.Normal),
            Font(R.font.dm_mono_medium, FontWeight.Medium),
        )
    }
    val ChakraPetch by lazy {
        FontFamily(
            Font(R.font.chakra_petch_medium, FontWeight.Medium),
            Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
            Font(R.font.chakra_petch_bold, FontWeight.Bold),
        )
    }
    val Caveat by lazy {
        FontFamily(
            weighted(R.font.caveat, 500),
            weighted(R.font.caveat, 700),
        )
    }

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
