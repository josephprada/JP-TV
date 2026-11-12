package co.jp.tv.playlist.domain

import kotlin.random.Random

/** Where a playlist is: the slot on screen and when it got there (wall-clock ms), enough to resume after process death. */
data class PlaylistPosition(val index: Int = 0, val changedAtMs: Long = 0L)

/** Port: remembers each playlist's [PlaylistPosition] across process death. */
interface PlaylistStateStore {
    fun load(kind: MediaKind): PlaylistPosition?

    fun save(kind: MediaKind, position: PlaylistPosition)
}

/** The rules of rotation. Pure: time and randomness come in as arguments. */
class PlaylistScheduler(private val random: (bound: Int) -> Int = { Random.nextInt(it) }) {

    /** The slot after [current]: the next one in order, or a random one that is never [current] itself. */
    fun next(current: Int, size: Int, order: PlaylistOrder): Int {
        if (size <= 1) return 0
        if (current !in 0 until size) return 0
        return when (order) {
            PlaylistOrder.SEQUENTIAL -> (current + 1) % size
            PlaylistOrder.SHUFFLE -> {
                val pick = random(size - 1)
                if (pick >= current) pick + 1 else pick
            }
        }
    }

    /** Milliseconds until the clock asks for the next item; null when time never does (never, or on home return). */
    fun remainingMs(interval: RotationInterval, lastChangeMs: Long, nowMs: Long): Long? {
        val minutes = interval.minutes ?: return null
        val elapsed = nowMs - lastChangeMs
        // A clock set backwards would stall the rotation for hours: treat it as due.
        if (elapsed < 0) return 0L
        return (minutes * MINUTE_MS - elapsed).coerceAtLeast(0L)
    }

    fun isDue(interval: RotationInterval, lastChangeMs: Long, nowMs: Long): Boolean =
        remainingMs(interval, lastChangeMs, nowMs) == 0L

    private fun advance(position: PlaylistPosition, size: Int, order: PlaylistOrder, nowMs: Long) =
        PlaylistPosition(next(position.index, size, order), nowMs)

    /** Moves on by the clock when the interval has elapsed. */
    fun tick(position: PlaylistPosition, size: Int, interval: RotationInterval, order: PlaylistOrder, nowMs: Long): PlaylistPosition =
        if (size > 1 && isDue(interval, position.changedAtMs, nowMs)) advance(position, size, order, nowMs) else position

    /** The launcher came back to the front: moves on in the "al volver al inicio" mode only. */
    fun onHomeReturn(position: PlaylistPosition, size: Int, interval: RotationInterval, order: PlaylistOrder, nowMs: Long): PlaylistPosition =
        if (size > 1 && interval == RotationInterval.ON_HOME_RETURN) advance(position, size, order, nowMs) else position

    /** A fresh process picks up from [saved]: the same slot unless its time is up (or it was a trip home), then one step. */
    fun resume(saved: PlaylistPosition?, size: Int, interval: RotationInterval, order: PlaylistOrder, nowMs: Long): PlaylistPosition {
        if (saved == null) return PlaylistPosition(0, nowMs)
        val clamped = if (saved.index in 0 until size) saved else PlaylistPosition(0, saved.changedAtMs)
        val afterTime = tick(clamped, size, interval, order, nowMs)
        return if (afterTime == clamped) onHomeReturn(clamped, size, interval, order, nowMs) else afterTime
    }

    private companion object {
        const val MINUTE_MS = 60_000L
    }
}
