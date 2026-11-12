package co.jp.tv.homeoverride.domain

/**
 * Follows which package was the last to come to the front, fed with the "activity resumed" usage events
 * of overlapping polling windows. Only the latest resumed package counts: a stock launcher that was
 * quickly replaced by Settings or by JP-TV itself is not a reason to redirect. Allocation-free.
 */
class ForegroundTracker {
    private var latestPackage: String? = null
    private var latestTime = Long.MIN_VALUE
    private var handledTime = Long.MIN_VALUE

    /** Feed events in the order the system reports them; older or repeated ones from an overlapping window change nothing. */
    fun onResumed(packageName: String, timeMs: Long) {
        if (timeMs >= latestTime) {
            latestPackage = packageName
            latestTime = timeMs
        }
    }

    /** The stock home now in front that has not been handled yet, or null. */
    fun pendingStock(resolved: Set<String>, ownPackage: String): String? {
        val pkg = latestPackage
        if (latestTime <= handledTime) return null
        return if (StockHomes.isStockHome(pkg, resolved, ownPackage)) pkg else null
    }

    /** The current foreground event needs no further action (redirected, or stood down on purpose). */
    fun markHandled() {
        handledTime = latestTime
    }

    fun reset() {
        latestPackage = null
        latestTime = Long.MIN_VALUE
        handledTime = Long.MIN_VALUE
    }
}
