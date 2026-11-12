package co.jp.tv.themes.oceano

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.wallpaper.rememberFrameMillis

/**
 * The scene's animation step: [tick] advances 15 times a second (every [OceanoLayout.TICK_MS] ms), the
 * pixel-art cadence of every sprite. Read it only inside a draw block so a step redraws the scene without
 * recomposing; between steps nothing changes and nothing is redrawn.
 */
@Stable
class OceanoClock internal constructor(millis: LongState) {
    private val step: State<Int> = derivedStateOf { ((millis.longValue % WRAP_MS) / OceanoLayout.TICK_MS).toInt() }

    val tick: Int get() = step.value

    private companion object {
        /** Wraps every two hours to keep the step small. */
        const val WRAP_MS = 7_200_000L
    }
}

/** Two 30 fps frames per tick: the clock needs no more, and it keeps the choreographer asleep between them. */
private const val FRAME_CAP = 30

/** Ticks at 15 steps a second and does not tick at all while [LocalPaused] is true. */
@Composable
fun rememberOceanoClock(): OceanoClock {
    val millis = rememberFrameMillis(LocalPaused.current, FRAME_CAP)
    return remember(millis) { OceanoClock(millis) }
}
