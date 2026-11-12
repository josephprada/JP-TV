package co.jp.tv.wallpaper.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.playlist.ui.LocalWallpaperMedia
import co.jp.tv.wallpaper.Wallpaper

/**
 * "Imagen fija": the pictures in `/sdcard/JPTV/fondos/` (plus the old `fondo.jpg` and the same folder on a USB
 * drive). One picture is a still wallpaper; several rotate with a crossfade (see [PlaylistImageView]).
 * Without a readable file it shows a neutral gradient with a hint, like the video wallpaper's placeholder.
 */
object StaticImageWallpaper : Wallpaper {
    override val id = ID
    override val displayName = "Imagen fija"

    const val ID = "image"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val media = LocalWallpaperMedia.current
        Box(
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(JpColors.Ink, JpColors.GlassFake, JpColors.Ink))),
            contentAlignment = Alignment.Center,
        ) {
            when {
                media.image.items.isNotEmpty() -> PlaylistImageView(media.image, paused, Modifier.fillMaxSize())
                media.loaded -> BasicText(
                    text = stringResource(R.string.wallpaper_image_hint),
                    style = TextStyle(
                        color = JpColors.Faint,
                        fontSize = 13.sp,
                        fontFamily = JpFonts.Mono,
                        letterSpacing = 0.18.em,
                    ),
                )
            }
        }
    }
}
