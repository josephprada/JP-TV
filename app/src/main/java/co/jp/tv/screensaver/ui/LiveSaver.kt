package co.jp.tv.screensaver.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.screensaver.domain.ScreensaverState
import co.jp.tv.wallpaper.video.PlaylistVideoPlayer

/** How often "Live" moves to the next video when there are several. */
private val LiveInterval = RotationInterval.MIN_5

/** "Live": the video playlist full screen through the one player, with a small clock. Stops and releases the player while paused. */
@Composable
internal fun LiveSaver(state: ScreensaverState) {
    val selection = remember(state.videos) {
        state.videos.copy(interval = if (state.videos.items.size > 1) LiveInterval else RotationInterval.NEVER)
    }
    Box(Modifier.fillMaxSize()) {
        PlaylistVideoPlayer(selection, paused = LocalPaused.current, modifier = Modifier.fillMaxSize())
        CornerClock(state)
    }
}
