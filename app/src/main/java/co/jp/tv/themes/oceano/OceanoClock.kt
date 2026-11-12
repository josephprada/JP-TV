package co.jp.tv.themes.oceano

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.wallpaper.rememberFrameMillis

/**
 * One frame clock shared by every looping layer of the scene, instead of one infinite transition
 * per fish or particle. Read [seconds] only inside draw or graphicsLayer blocks so a tick
 * redraws without recomposing. [lite] marks a low-end box: layers drop detail that costs fill rate.
 */
@Stable
class OceanoClock internal constructor(private val millis: LongState, val lite: Boolean = false) {
    /** Scene time in seconds; wraps every two hours to keep Float precision. */
    val seconds: Float get() = (millis.longValue % WRAP_MS) / 1_000f

    private companion object {
        const val WRAP_MS = 7_200_000L
    }
}

/** Ticks at the device's wallpaper frame rate and does not tick at all while [LocalPaused] is true. */
@Composable
fun rememberOceanoClock(): OceanoClock {
    val context = LocalContext.current
    val maxFps = remember { DeviceProfile.wallpaperMaxFps(context) }
    val millis = rememberFrameMillis(LocalPaused.current, maxFps)
    val lite = remember { DeviceProfile.isLowEnd(context) }
    return remember(millis, lite) { OceanoClock(millis, lite) }
}
