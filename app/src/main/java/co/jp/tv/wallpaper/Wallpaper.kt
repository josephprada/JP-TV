package co.jp.tv.wallpaper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** A live wallpaper drawn behind the theme. Implementations must idle when [paused]. */
interface Wallpaper {
    val id: String
    val displayName: String

    @Composable
    fun Render(accent: Color, paused: Boolean, modifier: Modifier)
}
