package co.jp.tv.screensaver.domain

import kotlin.math.abs

/** When the in-app screensaver starts. Pure: times are monotonic milliseconds handed in by the caller. */
object IdleRules {
    const val DEFAULT_MINUTES = 10

    /** The idle times the settings offer; 0 is never. */
    val CHOICES = listOf(0, 5, 10, 15, 30)

    /** The closest offered idle time, so a hand-edited or old value still lands on a chip. */
    fun snap(minutes: Int): Int = CHOICES.minBy { abs(it - minutes) }

    /** Milliseconds of silence still needed before the saver starts; null when it never does. */
    fun msUntilStart(lastInputMs: Long, nowMs: Long, minutes: Int): Long? {
        if (minutes <= 0) return null
        val idleFor = (nowMs - lastInputMs).coerceAtLeast(0L)
        return (minutes * MINUTE_MS - idleFor).coerceAtLeast(0L)
    }

    fun shouldStart(lastInputMs: Long, nowMs: Long, minutes: Int, homeVisible: Boolean, saverShowing: Boolean): Boolean =
        homeVisible && !saverShowing && msUntilStart(lastInputMs, nowMs, minutes) == 0L

    private const val MINUTE_MS = 60_000L
}
