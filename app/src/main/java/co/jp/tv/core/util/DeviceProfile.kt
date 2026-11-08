package co.jp.tv.core.util

import android.app.ActivityManager
import android.content.Context

/** Detects weak hardware so effects can scale down (frame cap, particle counts). */
object DeviceProfile {
    /** Devices advertising "2 GB" report a bit less than 2 GiB; allow some headroom. */
    private const val LOW_END_MAX_BYTES = 2_362_232_012L // 2.2 GiB

    fun isLowEnd(context: Context): Boolean {
        val am = context.getSystemService(ActivityManager::class.java) ?: return false
        val info = ActivityManager.MemoryInfo().also(am::getMemoryInfo)
        return am.isLowRamDevice || info.totalMem <= LOW_END_MAX_BYTES
    }
}
