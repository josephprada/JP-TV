package co.jp.tv.themes.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import co.jp.tv.R

/** Card description for a theme id, or an empty string for an id with no entry yet. */
@Composable
internal fun themeBlurb(id: String): String {
    @StringRes val res = when (id) {
        "jptv" -> R.string.theme_blurb_jptv
        "pixel" -> R.string.theme_blurb_pixel
        "terminal" -> R.string.theme_blurb_terminal
        "cartelera" -> R.string.theme_blurb_cartelera
        "zen" -> R.string.theme_blurb_zen
        "hud" -> R.string.theme_blurb_hud
        "vinilo" -> R.string.theme_blurb_vinilo
        "lofi" -> R.string.theme_blurb_lofi
        "gotham" -> R.string.theme_blurb_gotham
        "multiverso" -> R.string.theme_blurb_multiverso
        "cyberpunk" -> R.string.theme_blurb_cyberpunk
        "oceano" -> R.string.theme_blurb_oceano
        else -> return ""
    }
    return stringResource(res)
}
