package co.jp.tv.themes.shared

/** Lightning for the stormy scenes, as a pure function of a frame clock so a paused scene simply holds one frame. */
object Lightning {
    const val DEFAULT_PERIOD_MS = 13_000

    /**
     * Flash strength 0..1 at [ms]: a double flash at the start of every [periodMs] cycle that dies away in
     * under half a second, and dark the rest of the time (about 3 % of a 13 s cycle is lit).
     */
    fun flash(ms: Long, periodMs: Int = DEFAULT_PERIOD_MS): Float {
        val t = (ms % periodMs).toInt()
        return when {
            t < 60 -> 1f
            t < 120 -> 0.15f
            t < 180 -> 0.8f
            t < 400 -> 0.8f * (1f - (t - 180) / 220f)
            else -> 0f
        }
    }
}
