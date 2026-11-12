package co.jp.tv.homeoverride.domain

enum class WatcherMode {
    /** The foreground watcher has nothing to do: shut it down. */
    STOPPED,

    /** Running but not polling: the screen is off or the accessibility backend is already bound. */
    IDLE,

    /** Polling the usage events. */
    POLLING,
}

/** When the usage-stats backend (the foreground watcher service) runs and what window it queries. */
object WatcherPlan {
    const val POLL_MS = 450L
    const val IDLE_RECHECK_MS = 2_000L
    const val WINDOW_MS = 2_000L
    private const val OVERLAP_MS = 150L

    /** Both special accesses are needed: usage access to see the foreground, overlay to start JP-TV from the background. */
    fun canRun(toggleOn: Boolean, usageAccess: Boolean, overlay: Boolean) = toggleOn && usageAccess && overlay

    fun mode(
        toggleOn: Boolean,
        usageAccess: Boolean,
        overlay: Boolean,
        screenInteractive: Boolean,
        accessibilityBound: Boolean,
    ): WatcherMode = when {
        !canRun(toggleOn, usageAccess, overlay) -> WatcherMode.STOPPED
        !screenInteractive || accessibilityBound -> WatcherMode.IDLE
        else -> WatcherMode.POLLING
    }

    /** Start of the next usage query: the last [WINDOW_MS], or a little before the previous query ended if that is more recent. */
    fun queryStart(now: Long, lastQueryEnd: Long): Long = maxOf(now - WINDOW_MS, lastQueryEnd - OVERLAP_MS)
}
