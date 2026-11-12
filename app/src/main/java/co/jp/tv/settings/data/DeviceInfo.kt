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

    /** Music stream volume as 0..1, or null when the audio service is unavailable. */
    fun volume(): Float? = runCatching {
        val audio = appContext.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val max = audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        if (max <= 0) null else audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC).toFloat() / max
    }.getOrNull()

    /** Bluetooth on/off, or null when there is no adapter or the platform refuses to say. */
    @Suppress("DEPRECATION")
    fun bluetoothOn(): Boolean? = runCatching {
        android.bluetooth.BluetoothAdapter.getDefaultAdapter()?.isEnabled
    }.getOrNull()
}
