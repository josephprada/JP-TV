package co.jp.tv.playlist.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.PlaylistSelection
import co.jp.tv.playlist.domain.PlaylistStateStore

/** Lazy preview source for the UI; Customize reads it from [LocalMediaThumbnails] and never decodes files itself. */
fun interface MediaThumbnails {
    suspend fun load(item: MediaItem): ImageBitmap?
}

val LocalMediaThumbnails = staticCompositionLocalOf<MediaThumbnails> { MediaThumbnails { null } }

/** What the playlist wallpapers play: the resolved files of both kinds. [loaded] is false until the first scan ends. */
data class WallpaperMedia(
    val video: PlaylistSelection = PlaylistSelection.Empty,
    val image: PlaylistSelection = PlaylistSelection.Empty,
    val loaded: Boolean = false,
) {
    companion object {
        val Empty = WallpaperMedia()
    }
}

/** Provided by the screen that draws a wallpaper (the home, or the Customize preview with the draft settings). */
val LocalWallpaperMedia = compositionLocalOf { WallpaperMedia.Empty }

/**
 * Where playlists remember their slot across process death. Null (the default) keeps it in memory only,
 * which is what previews and the screensaver want.
 */
val LocalPlaylistStateStore = staticCompositionLocalOf<PlaylistStateStore?> { null }
