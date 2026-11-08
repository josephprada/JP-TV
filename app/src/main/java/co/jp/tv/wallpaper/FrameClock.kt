package co.jp.tv.wallpaper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.LongState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos

/**
 * Milliseconds since the first frame, advanced once per drawn frame. Read it only inside draw
 * lambdas so a tick redraws the canvas without recomposing. Stops while [paused]; [maxFps]
 * skips vsyncs to cap the draw rate (0 = uncapped).
 */
@Composable
fun rememberFrameMillis(paused: Boolean, maxFps: Int): LongState {
    val millis = remember { mutableLongStateOf(0L) }
    LaunchedEffect(paused, maxFps) {
        if (paused) return@LaunchedEffect
        // Allow a little slack so a 30 fps cap still draws on every second 60 Hz vsync.
        val minGapNanos = if (maxFps > 0) 1_000_000_000L / maxFps - 3_000_000L else 0L
        var last = 0L
        var origin = -1L
        val base = millis.longValue
        while (true) {
            withFrameNanos { now ->
                if (origin < 0) origin = now
                if (now - last >= minGapNanos) {
                    last = now
                    millis.longValue = base + (now - origin) / 1_000_000L
                }
            }
        }
    }
    return millis
}
