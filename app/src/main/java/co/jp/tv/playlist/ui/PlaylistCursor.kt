package co.jp.tv.playlist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistPosition
import co.jp.tv.playlist.domain.PlaylistScheduler
import co.jp.tv.playlist.domain.RotationInterval
import kotlinx.coroutines.delay

/** The slot a playlist is on, and a way to jump to the next one (for a file that can't be played). */
class PlaylistCursor internal constructor(val index: Int, val skip: () -> Unit)

/**
 * Walks a playlist of [size] files by the [PlaylistScheduler] rules. The slot is resumed from
 * [LocalPlaylistStateStore] and saved back on every change; the clock only runs while [active], and a trip
 * out to another app and back (the [LocalPaused] edge) is a "home return".
 */
@Composable
fun rememberPlaylistCursor(
    kind: MediaKind,
    ids: List<String>,
    interval: RotationInterval,
    order: PlaylistOrder,
    active: Boolean,
): PlaylistCursor {
    val store = LocalPlaylistStateStore.current
    val scheduler = remember { PlaylistScheduler() }
    val size = ids.size
    var position by remember(kind, ids) {
        // An empty list (the scan has not finished) must neither resume nor overwrite the saved slot.
        mutableStateOf(
            if (size == 0) PlaylistPosition() else scheduler.resume(store?.load(kind), size, interval, order, System.currentTimeMillis()),
        )
    }
    LaunchedEffect(position) { if (size > 0) store?.save(kind, position) }

    val stopped = LocalPaused.current
    var wasStopped by remember { mutableStateOf(stopped) }
    LaunchedEffect(stopped) {
        if (wasStopped && !stopped) {
            position = scheduler.onHomeReturn(position, size, interval, order, System.currentTimeMillis())
        }
        wasStopped = stopped
    }

    LaunchedEffect(position, interval, order, size, active) {
        if (!active || size < 2) return@LaunchedEffect
        val current = position
        while (true) {
            val wait = scheduler.remainingMs(interval, current.changedAtMs, System.currentTimeMillis()) ?: return@LaunchedEffect
            delay(wait.coerceAtLeast(MIN_WAIT_MS))
            val next = scheduler.tick(current, size, interval, order, System.currentTimeMillis())
            if (next != current) {
                position = next
                return@LaunchedEffect
            }
        }
    }

    return PlaylistCursor(index = position.index.coerceIn(0, (size - 1).coerceAtLeast(0))) {
        if (size > 1) {
            position = PlaylistPosition(scheduler.next(position.index, size, order), System.currentTimeMillis())
        }
    }
}

private const val MIN_WAIT_MS = 1_000L
