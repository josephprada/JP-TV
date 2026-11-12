package co.jp.tv.wallpaper.image

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistSelection
import co.jp.tv.playlist.ui.LocalImageSource
import co.jp.tv.playlist.ui.rememberPlaylistCursor

private const val MAX_WIDTH = 1920
private const val MAX_HEIGHT = 1080
private const val CROSSFADE_MS = 700

/**
 * Plays a playlist of still images. At most two bitmaps exist at once: the one fading out and the one
 * fading in, each decoded bounded to the screen ([BoundedBitmaps]); when the fade ends the old one is
 * dropped. A single file just stays on screen.
 */
@Composable
fun PlaylistImageView(selection: PlaylistSelection, paused: Boolean, modifier: Modifier = Modifier) {
    val items = selection.items
    val ids = remember(items) { items.map { it.id } }
    val cursor = rememberPlaylistCursor(MediaKind.IMAGE, ids, selection.interval, selection.order, active = !paused)
    val current = items.getOrNull(cursor.index)
    val skip by rememberUpdatedState(cursor.skip)
    var failures by remember { mutableIntStateOf(0) }
    val imageSource = LocalImageSource.current

    var shown by remember { mutableStateOf<ImageBitmap?>(null) }
    var leaving by remember { mutableStateOf<ImageBitmap?>(null) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(current?.id) {
        val item = current ?: return@LaunchedEffect
        val next = imageSource.load(item, MAX_WIDTH, MAX_HEIGHT)
        if (next == null) {
            // Give up once every file has failed in a row, instead of spinning on a list of broken ones.
            if (++failures < items.size) skip()
            return@LaunchedEffect
        }
        failures = 0
        leaving = shown
        shown = next
        progress.snapTo(0f)
        progress.animateTo(1f, tween(CROSSFADE_MS))
        leaving = null
    }

    Box(modifier.fillMaxSize()) {
        leaving?.let { old ->
            Image(bitmap = old, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        shown?.let { image ->
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = if (leaving == null) 1f else progress.value },
                contentScale = ContentScale.Crop,
            )
        }
    }
}
