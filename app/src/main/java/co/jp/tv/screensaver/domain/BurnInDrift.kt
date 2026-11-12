package co.jp.tv.screensaver.domain

/** Where a drifting element sits inside its free room: -1..1 on each axis. */
data class Drift(val x: Float, val y: Float)

/**
 * Burn-in protection for the clock mode: the clock hops to another of nine places once a minute,
 * in a fixed order that never stays put and visits every place before coming back.
 */
object BurnInDrift {
    private val places = listOf(
        Drift(-1f, -1f), Drift(0f, -1f), Drift(1f, -1f),
        Drift(-1f, 0f), Drift(0f, 0f), Drift(1f, 0f),
        Drift(-1f, 1f), Drift(0f, 1f), Drift(1f, 1f),
    )

    /** Stride 4 is coprime with 9: every place gets visited and neighbours in time are far apart in space. */
    private const val STRIDE = 4

    fun stepOf(epochSecond: Long): Long = Math.floorDiv(epochSecond, 60L)

    fun offset(step: Long): Drift = places[Math.floorMod(step * STRIDE, places.size.toLong()).toInt()]
}
