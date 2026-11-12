package co.jp.tv.wallpaper.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * "Mi video": a muted playlist of the videos in `/sdcard/JPTV/fondos/` (plus the old `loop.mp4` and the same
 * folder on a USB drive). One file is a plain loop; several rotate (see [PlaylistVideoPlayer]). Shows a mono
 * placeholder when there is nothing to play.
 */
object VideoWallpaper : Wallpaper {
    override val id = ID
    override val displayName = "Mi video"

    const val ID = "video"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val media = LocalWallpaperMedia.current
        Box(modifier.fillMaxSize().background(JpColors.Ink), contentAlignment = Alignment.Center) {
            when {
                media.video.items.isNotEmpty() -> PlaylistVideoPlayer(media.video, paused, Modifier.fillMaxSize())
                media.loaded -> BasicText(
                    text = stringResource(R.string.wallpaper_video_hint),
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
