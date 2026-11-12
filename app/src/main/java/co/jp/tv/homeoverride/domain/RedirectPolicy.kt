package co.jp.tv.homeoverride.domain

enum class Trigger { HOME_KEY, STOCK_WINDOW, BOOT }

enum class Reason { DISABLED, GRACE, TOO_MANY_FAILURES, DEBOUNCED, BOOTING }

sealed interface Decision {
    data object Redirect : Decision
    data class PassThrough(val reason: Reason) : Decision
}

/**
 * Decides whether JP-TV may take the screen over from the stock launcher, and keeps the safety guards:
 * the override stands down when it is off, when the user opened the stock launcher on purpose, or when
 * JP-TV crashed or failed to show [MAX_TROUBLES] times in [FAILURE_WINDOW_MS]. Passing through always
 * leaves the user on the stock launcher, which is never touched.
 */
class RedirectPolicy(
    private val store: GuardStateStore,
    /** Wall-clock ms. */
    private val clock: () -> Long,
    /** ms since the device booted. */
    private val uptime: () -> Long,
) {
    private var lastRedirectAt: Long? = null

    @Synchronized
    fun decide(enabled: Boolean, trigger: Trigger): Decision {
        if (!enabled) return Decision.PassThrough(Reason.DISABLED)
        evaluatePending()
        val now = clock()
        val state = store.load()
        if (state.graceUntil > now && state.graceUntil - now <= GRACE_MS) return Decision.PassThrough(Reason.GRACE)
        if (recentTroubles(state, now).size >= MAX_TROUBLES) return Decision.PassThrough(Reason.TOO_MANY_FAILURES)
        if (trigger == Trigger.STOCK_WINDOW) {
            if (uptime() < MIN_UPTIME_MS) return Decision.PassThrough(Reason.BOOTING)
            val last = lastRedirectAt
            if (last != null && now - last in 0 until DEBOUNCE_MS) return Decision.PassThrough(Reason.DEBOUNCED)
        }
        return Decision.Redirect
    }

    /** JP-TV was asked to come to the front: it must report drawn within [DRAW_TIMEOUT_MS]. */
    @Synchronized
    fun onRedirected() {
        val now = clock()
        lastRedirectAt = now
        store.save(store.load().copy(pendingSince = now))
    }

    /** Heartbeat: the home is on screen. */
    @Synchronized
    fun onHomeDrawn() {
        val state = store.load()
        if (state.pendingSince != null) store.save(state.copy(pendingSince = null))
    }

    @Synchronized
    fun onCrash() {
        val now = clock()
        val state = store.load()
        // The crash is the failure of any redirect in flight: do not count it twice.
        store.save(state.copy(troubleAt = recentTroubles(state, now) + now, pendingSince = null))
    }

    /** The user opened the stock launcher deliberately. */
    @Synchronized
    fun startGrace() {
        store.save(store.load().copy(graceUntil = clock() + GRACE_MS, pendingSince = null))
    }

    /** Counts a failure when the last redirect is overdue. Returns true when it did. */
    @Synchronized
    fun evaluatePending(): Boolean {
        val now = clock()
        val state = store.load()
        val since = state.pendingSince ?: return false
        if (now - since < DRAW_TIMEOUT_MS) return false
        store.save(state.copy(troubleAt = recentTroubles(state, now) + now, pendingSince = null))
        return true
    }

    private fun recentTroubles(state: GuardState, now: Long) =
        state.troubleAt.filter { now - it <= FAILURE_WINDOW_MS && it - now <= FAILURE_WINDOW_MS }.takeLast(MAX_TROUBLES)

    companion object {
        const val DEBOUNCE_MS = 1_500L
        const val DRAW_TIMEOUT_MS = 5_000L
        const val FAILURE_WINDOW_MS = 10 * 60_000L
        const val MAX_TROUBLES = 3
        const val GRACE_MS = 10 * 60_000L

        /** Stock windows in the first seconds after boot belong to the system settling in. */
        const val MIN_UPTIME_MS = 15_000L
    }
}
