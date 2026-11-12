package co.jp.tv.core.util

/**
 * Decides when the home plays its return entrance: only on the resume that follows a launch from
 * the home during which the home really paused. A resume for any other reason (first start,
 * Home key, screen on) plays nothing.
 */
class LaunchReturnGate {
    private var armed = false

    /** True once the home paused after [launched]; the launch took effect. */
    var hasLeft = false
        private set

    /** The home started an app. */
    fun launched() {
        armed = true
        hasLeft = false
    }

    fun onPaused() {
        if (armed) hasLeft = true
    }

    /** True exactly once per launch: the home is back and should play its entrance. */
    fun onResumed(): Boolean {
        if (!armed || !hasLeft) return false
        cancel()
        return true
    }

    /** The launch did not happen (no intent, app crashed on start): forget it. */
    fun cancel() {
        armed = false
        hasLeft = false
    }
}
