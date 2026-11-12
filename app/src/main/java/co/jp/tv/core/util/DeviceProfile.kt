package co.jp.tv.core.util

import android.app.ActivityManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Detects weak hardware so effects can scale down (frame cap, particle counts). */
object DeviceProfile {
    /** Devices advertising "2 GB" report a bit less than 2 GiB; allow some headroom. */
    private const val LOW_END_MAX_BYTES = 2_362_232_012L // 2.2 GiB

    fun isLowEnd(context: Context): Boolean {
        val am = context.getSystemService(ActivityManager::class.java) ?: return false
        val info = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
        return am.isLowRamDevice || info.totalMem <= LOW_END_MAX_BYTES
    }

    /** Galaxy star-field multiplier; half the particles on low-end boxes. */
    fun galaxyStarBudget(context: Context): Float = if (isLowEnd(context)) 0.5f else 1f

    /** Target wallpaper FPS; PLAN caps low-end at 30. */
    fun wallpaperMaxFps(context: Context): Int = if (isLowEnd(context)) 30 else 60

    /** Soft orb glow under the galaxy; skipped on low-end to save fill rate. */
    fun galaxyOrbsEnabled(context: Context): Boolean = !isLowEnd(context)
}

/** True on low-end boxes; themes use it to drop effects that cost fill rate. Computed once per composition. */
@Composable
fun rememberLowEnd(): Boolean {
    val context = LocalContext.current
    return remember { DeviceProfile.isLowEnd(context) }
}
