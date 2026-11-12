package co.jp.tv.homeoverride.data

import android.content.Context
import android.provider.Settings
import co.jp.tv.homeoverride.domain.AccessibilityServices

/** Adapter: read-only check of whether the user has switched the home override service on. */
class AccessibilityStatusReader(context: Context) {
    private val appContext = context.applicationContext

    fun isServiceEnabled(): Boolean {
        val resolver = appContext.contentResolver
        val list = Settings.Secure.getString(resolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val masterOn = Settings.Secure.getInt(resolver, Settings.Secure.ACCESSIBILITY_ENABLED, 1) != 0
        return masterOn && AccessibilityServices.isEnabled(list, appContext.packageName, HomeOverrideService::class.java.name)
    }
}
