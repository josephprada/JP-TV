package co.jp.tv.homeoverride.data

import co.jp.tv.homeoverride.domain.RedirectPolicy

/** Records every uncaught crash for the safety guards, then lets the previous handler do its usual work. */
object CrashRecorder {
    fun install(policy: RedirectPolicy) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { policy.onCrash() }
            previous?.uncaughtException(thread, error)
        }
    }
}
