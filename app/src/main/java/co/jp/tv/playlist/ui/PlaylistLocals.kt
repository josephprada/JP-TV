package co.jp.tv.playlist.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.jp.tv.playlist.data.BoundedBitmaps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.WallpaperMedia
import co.jp.tv.playlist.domain.PlaylistStateStore

/** Lazy preview source for the UI; Customize reads it from [LocalMediaThumbnails] and never decodes files itself. */
fun interface MediaThumbnails {
    suspend fun load(item: MediaItem): ImageBitmap?
}

val LocalMediaThumbnails = staticCompositionLocalOf<MediaThumbnails> { MediaThumbnails { null } }

/** Reads a picture of a file bounded to the screen; the default decodes off the main thread, tests plug in fixtures. */
fun interface ImageSource {
    suspend fun load(item: MediaItem, maxWidth: Int, maxHeight: Int): ImageBitmap?
}

val LocalImageSource = staticCompositionLocalOf<ImageSource> {
    ImageSource { item, maxWidth, maxHeight ->
        withContext(Dispatchers.IO) { BoundedBitmaps.decode(item.id, maxWidth, maxHeight)?.asImageBitmap() }
    }
}

/** Provided by the screen that draws a wallpaper (the home, or the Customize preview with the draft settings). */
val LocalWallpaperMedia = compositionLocalOf { WallpaperMedia.Empty }

/**
 * Where playlists remember their slot across process death. Null (the default) keeps it in memory only,
 * which is what previews and the screensaver want.
 */
val LocalPlaylistStateStore = staticCompositionLocalOf<PlaylistStateStore?> { null }
