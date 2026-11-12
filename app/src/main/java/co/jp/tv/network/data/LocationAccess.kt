package co.jp.tv.network.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Whether Android will hand out the Wi-Fi name: it needs the location permission and the system location switch. */
class LocationAccess(context: Context) {
    private val appContext = context.applicationContext

    /** Fine location always works; coarse is enough on Android 8.1 to 9 only, so fine is the one we ask for. */
    fun permissionGranted(): Boolean =
        granted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && granted(Manifest.permission.ACCESS_COARSE_LOCATION))

    @Suppress("DEPRECATION")
    fun locationOn(): Boolean = try {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        when {
            manager == null -> false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> manager.isLocationEnabled
            else -> Settings.Secure.getInt(appContext.contentResolver, Settings.Secure.LOCATION_MODE, 0) != Settings.Secure.LOCATION_MODE_OFF
        }
    } catch (_: SecurityException) {
        false
    }

    private fun granted(permission: String) =
        ContextCompat.checkSelfPermission(appContext, permission) == PackageManager.PERMISSION_GRANTED
}
