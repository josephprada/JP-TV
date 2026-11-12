package co.jp.tv.screensaver.domain

/** The mode actually on screen. */
object ScreensaverModes {
    const val RANDOM_EVERY_MS = 10 * 60_000L

    private val concrete = listOf(ScreensaverMode.PHOTOS, ScreensaverMode.CLOCK, ScreensaverMode.LIVE, ScreensaverMode.AMBIENT)

    /**
     * [mode] itself, or for [ScreensaverMode.RANDOM] one of the others that has something to show,
     * never the [previous] one while another choice exists.
     */
    fun concrete(
        mode: ScreensaverMode,
        previous: ScreensaverMode?,
        hasPhotos: Boolean,
        hasVideos: Boolean,
        random: (bound: Int) -> Int,
    ): ScreensaverMode {
        if (mode != ScreensaverMode.RANDOM) return mode
        val available = concrete.filter {
            when (it) {
                ScreensaverMode.PHOTOS -> hasPhotos
                ScreensaverMode.LIVE -> hasVideos
                else -> true
            }
        }
        val others = available.filter { it != previous }.ifEmpty { available }
        return others[random(others.size)]
    }
}
