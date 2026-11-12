package co.jp.tv.wallpaper.video

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistSelection
import co.jp.tv.playlist.ui.rememberPlaylistCursor
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

private const val FADE_MS = 400
private const val FIRST_FRAME_TIMEOUT_MS = 2_000L

/**
 * Plays a playlist of videos, muted, with exactly ONE [ExoPlayer]: the next file replaces the media item of
 * the same player, hidden behind a ~400 ms fade to black (out, swap, wait for the first frame, in). There are
 * never two decoders, which a 2 GB box would not survive. A single file loops forever; with several, the
 * [PlaylistSelection]'s interval and order decide when to move on.
 *
 * While [paused] it stops playing; if the launcher is also stopped ([LocalPaused]) the player is released
 * and rebuilt when it comes back, so a hidden launcher never holds a hardware decoder.
 */
@Composable
fun PlaylistVideoPlayer(selection: PlaylistSelection, paused: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val items = selection.items
    val ids = remember(items) { items.map { it.id } }
    val cursor = rememberPlaylistCursor(MediaKind.VIDEO, ids, selection.interval, selection.order, active = !paused)
    val current = items.getOrNull(cursor.index)

    val release = paused && LocalPaused.current
    val player = remember(release) {
        if (release) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f
            }
        }
    }
    DisposableEffect(player) { onDispose { player?.release() } }

    val pausedNow by rememberUpdatedState(paused)
    LaunchedEffect(paused, player) { player?.playWhenReady = !paused }

    // A file that cannot be played is skipped; if every file fails in a row, stop trying.
    val skip by rememberUpdatedState(cursor.skip)
    val count by rememberUpdatedState(items.size)
    DisposableEffect(player) {
        var failures = 0
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                failures = 0
            }

            override fun onPlayerError(error: PlaybackException) {
                failures++
                if (count > 1 && failures < count) skip()
            }
        }
        player?.addListener(listener)
        onDispose { player?.removeListener(listener) }
    }

    val fade = remember { Animatable(0f) }
    val shownId = remember(player) { arrayOfNulls<String>(1) }
    LaunchedEffect(player, current?.id) {
        val exo = player ?: return@LaunchedEffect
        val item = current ?: return@LaunchedEffect
        if (shownId[0] == item.id) return@LaunchedEffect
        val first = shownId[0] == null
        if (!first) fade.animateTo(1f, tween(FADE_MS))
        val rendered = CompletableDeferred<Unit>()
        val frameListener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                rendered.complete(Unit)
            }

            override fun onPlayerError(error: PlaybackException) {
                rendered.complete(Unit)
            }
        }
        exo.addListener(frameListener)
        try {
            exo.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(item.id))))
            exo.prepare()
            exo.playWhenReady = !pausedNow
            shownId[0] = item.id
            withTimeoutOrNull(FIRST_FRAME_TIMEOUT_MS) { rendered.await() }
        } finally {
            exo.removeListener(frameListener)
        }
        if (!first) fade.animateTo(0f, tween(FADE_MS))
    }

    Box(modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize(),
        )
        // Read in the draw phase only: the fade never recomposes anything.
        Box(Modifier.fillMaxSize().drawBehind { drawRect(Color.Black, alpha = fade.value) })
    }
}
