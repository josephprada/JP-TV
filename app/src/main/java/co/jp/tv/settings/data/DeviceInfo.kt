package co.jp.tv.settings.data

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs

/** Cheap, permission-free facts shown on the quick settings tiles. */
class DeviceInfo(context: Context) {
    private val appContext = context.applicationContext

    data class Storage(val freeBytes: Long, val totalBytes: Long)

    val launcherVersion: String = runCatching {
        appContext.packageManager.getPackageInfo(appContext.packageName, 0).versionName
    }.getOrNull().orEmpty()

    val androidRelease: String = Build.VERSION.RELEASE
    val deviceModel: String = Build.MODEL

    fun storage(): Storage? = runCatching {
        val stat = StatFs(Environment.getDataDirectory().path)
        Storage(freeBytes = stat.availableBytes, totalBytes = stat.totalBytes)
    }.getOrNull()

    /** Height in pixels and refresh rate of the main display. */
    @Suppress("DEPRECATION")
    fun display(): Pair<Int, Int>? = runCatching {
        val wm = appContext.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager
        wm.defaultDisplay.refreshRate.let { appContext.resources.displayMetrics.heightPixels to Math.round(it) }
    }.getOrNull()
}
