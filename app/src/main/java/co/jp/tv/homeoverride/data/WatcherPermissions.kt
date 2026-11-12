package co.jp.tv.homeoverride.data

import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings

/** Adapter: read-only checks of the two special accesses the usage-stats backend needs. Never changes a setting. */
class WatcherPermissions(context: Context) {
    private val appContext = context.applicationContext

    /** Settings > Special app access > Usage access. */
    fun usageAccess(): Boolean {
        val ops = appContext.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = try {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
            } else {
                ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), appContext.packageName)
            }
        } catch (e: RuntimeException) {
            return false
        }
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            appContext.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    /** Settings > Special app access > Display over other apps (also lets the app start activities from the background). */
    fun overlay(): Boolean = try {
        Settings.canDrawOverlays(appContext)
    } catch (e: RuntimeException) {
        false
    }
}
