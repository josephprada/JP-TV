package co.jp.tv.core.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.os.SystemClock
import android.util.Log

/**
 * Cold-start timeline in logcat under the `JpStartup` tag, only in debuggable builds.
 * Each mark prints the milliseconds since the process was forked: `adb logcat -s JpStartup`.
 */
object StartupTrace {
    private const val TAG = "JpStartup"

    @Volatile
    private var enabled = false

    fun init(context: Context) {
        enabled = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    fun mark(label: String) {
        if (!enabled) return
        val sinceFork = SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()
        Log.d(TAG, "$label +${sinceFork}ms [${Thread.currentThread().name}]")
    }
}
