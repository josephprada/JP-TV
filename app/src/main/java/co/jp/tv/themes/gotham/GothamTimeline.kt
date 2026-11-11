package co.jp.tv.themes.gotham

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.wallpaper.rememberFrameMillis

private val StillTime: () -> Long = { 0L }

/**
 * Runs [content] with the elapsed milliseconds of the Gotham loops (rain, fog, sway, motes,
 * beacons). While paused it returns before creating the frame clock, so nothing ticks and
 * the scene shows its first frame.
 */
@Composable
internal fun GothamTimeline(content: @Composable (time: () -> Long) -> Unit) {
    if (LocalPaused.current) {
        content(StillTime)
        return
    }
    val context = LocalContext.current
    val fps = remember { DeviceProfile.wallpaperMaxFps(context) }
    val frame = rememberFrameMillis(paused = false, maxFps = fps)
    val time = remember(frame) { { frame.longValue } }
    content(time)
}
