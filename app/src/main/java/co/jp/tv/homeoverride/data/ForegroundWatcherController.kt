package co.jp.tv.homeoverride.data

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import co.jp.tv.homeoverride.domain.WatcherPlan

/** Starts or stops [ForegroundWatcherService] to match the toggle and the two special accesses. */
class ForegroundWatcherController(
    context: Context,
    private val permissions: WatcherPermissions,
) {
    private val appContext = context.applicationContext

    fun sync(toggleOn: Boolean) {
        val intent = Intent(appContext, ForegroundWatcherService::class.java)
        if (WatcherPlan.canRun(toggleOn, permissions.usageAccess(), permissions.overlay())) {
            try {
                ContextCompat.startForegroundService(appContext, intent)
            } catch (e: RuntimeException) {
                // Background start refused (no overlay exemption yet); the next resume of JP-TV tries again.
                Log.w(TAG, "Could not start the foreground watcher", e)
            }
        } else {
            appContext.stopService(intent)
        }
    }

    private companion object {
        const val TAG = "WatcherController"
    }
}
