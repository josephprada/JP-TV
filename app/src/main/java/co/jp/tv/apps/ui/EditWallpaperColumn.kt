package co.jp.tv.apps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.MediaLibraryState
import co.jp.tv.playlist.domain.PlaylistResolver
import co.jp.tv.playlist.ui.labelRes
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.wallpaper.WallpaperRegistry
import co.jp.tv.wallpaper.embers.EmbersWallpaper
import co.jp.tv.wallpaper.galaxy.GalaxyWallpaper
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper

/** Slots of the wallpaper column the editor's key handler walks: one per wallpaper chip, then dim, then pause. */
internal object WallSlot {
    val chips: Int get() = WallpaperRegistry.all.size
    val Dim: Int get() = chips
    val Pause: Int get() = chips + 1
}

private val InkGlass = Color(0xFF050506).copy(alpha = 0.7f)

/**
 * Third column of the app editor: live wallpaper preview, the wallpaper chips, dim and "pause in apps".
 * [focusSlot] is the [WallSlot] the editor's key handler is on (null when focus is in the app columns).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditWallpaperColumn(
    config: LauncherConfig,
    accent: Color,
    focusSlot: Int?,
    media: MediaLibraryState = MediaLibraryState.Empty,
    modifier: Modifier = Modifier,
) {
    val wallpapers = WallpaperRegistry.all
    val current = WallpaperRegistry.byId(config.wallpaperId)
    Column(
        modifier = modifier
            .fillMaxSize()
            .riseIn(delayMs = 260, durationMs = JpMotion.RiseMs, distanceDp = 9f)
            .glass(RoundedCornerShape(JpDimens.RailRadius))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        BasicText(
            stringResource(R.string.customize_wallpaper),
            style = TextStyle(JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        )
        Box(
            Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(13.dp)).background(JpColors.Ink),
        ) {
            // Only the cheap procedural wallpapers render live here; a video would start a second player.
            when (current) {
                EmbersWallpaper, GalaxyWallpaper -> current.Render(accent, LocalPaused.current, Modifier.fillMaxSize())
                else -> Unit
            }
            val default = if (current.id == LauncherConfig.DEFAULT_WALLPAPER_ID) stringResource(R.string.edit_wall_default) else ""
            BasicText(
                current.displayName.uppercase() + default,
                style = TextStyle(JpColors.Paper, 6.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 10.dp, bottom = 8.dp)
                    .background(Color(0xFF050506).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 7.dp, vertical = 4.dp),
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            wallpapers.forEachIndexed { i, wall ->
                WallChip(wall.displayName, selected = wall.id == config.wallpaperId, focused = focusSlot == i, accent = accent)
            }
        }
        val playlistKind = when (current.id) {
            VideoWallpaper.ID -> MediaKind.VIDEO
            StaticImageWallpaper.ID -> MediaKind.IMAGE
            else -> null
        }
        if (playlistKind != null) {
            // Mirrors what Customize edits; the files and the rotation are chosen there.
            val settings = config.playlist(playlistKind)
            val count = PlaylistResolver.resolve(media.of(playlistKind), settings).size
            BasicText(
                stringResource(R.string.edit_wall_list_summary, count, stringResource(settings.interval.labelRes())),
                style = TextStyle(JpColors.Muted, 8.sp, fontFamily = JpFonts.Body),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WallCell(
                label = stringResource(R.string.edit_wall_dim),
                value = "${config.wallpaperDim}%",
                focused = focusSlot == WallSlot.Dim,
                accent = accent,
                modifier = Modifier.weight(1f),
            )
            WallCell(
                label = stringResource(R.string.edit_wall_pause),
                value = stringResource(if (config.pauseWallpaperInApps) R.string.edit_yes else R.string.edit_no),
                focused = focusSlot == WallSlot.Pause,
                accent = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WallChip(label: String, selected: Boolean, focused: Boolean, accent: Color) {
    val shape = RoundedCornerShape(10.dp)
    val ring = when {
        focused -> accent.copy(alpha = 0.9f)
        selected -> JpColors.AccentLift.copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    BasicText(
        label,
        style = TextStyle(if (selected || focused) JpColors.Paper else JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
        modifier = Modifier
            .background(if (selected || focused) InkGlass else JpColors.Paper.copy(alpha = 0.06f), shape)
            .border(if (focused) 1.5.dp else JpDimens.Hairline, ring, shape)
            .padding(horizontal = 9.dp, vertical = 6.dp),
    )
}

@Composable
private fun WallCell(label: String, value: String, focused: Boolean, accent: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .background(if (focused) InkGlass else JpColors.Paper.copy(alpha = 0.05f), shape)
            .border(if (focused) 1.5.dp else JpDimens.Hairline, if (focused) accent.copy(alpha = 0.9f) else Color.Transparent, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(label, style = TextStyle(JpColors.Faint, 6.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em))
        BasicText(value, style = TextStyle(JpColors.Paper, 11.sp, fontFamily = JpFonts.Body))
    }
}
