package co.jp.tv.settings.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget

/** Opens native Android settings screens, falling back to the main settings screen when a handler is missing. */
class SettingsLauncher(context: Context) {
    private val appContext = context.applicationContext

    fun open(target: ShortcutTarget.Native): Boolean = open(target.action, target.fallbackAction)

    fun open(action: String, fallbackAction: String): Boolean =
        start(action) || start(fallbackAction)

    /** Opens a per-app special access screen for this app; older firmwares only have the generic list, so [action] is tried bare next. */
    fun openForThisApp(action: String, fallbackAction: String): Boolean =
        start(action, Uri.parse("package:${appContext.packageName}")) || open(action, fallbackAction)

    /** Android's screensaver settings, else Display, else the main settings. Only opens screens: it never changes a setting. */
    fun openScreensaverSettings(): Boolean =
        open(SettingsShortcuts.DREAM_SETTINGS_ACTION, SettingsShortcuts.DISPLAY_SETTINGS_ACTION) ||
            open(SettingsShortcuts.ALL_SETTINGS_ACTION, SettingsShortcuts.ALL_SETTINGS_ACTION)

    private fun start(action: String, data: Uri? = null): Boolean = try {
        appContext.startActivity(Intent(action, data).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No handler for $action")
        false
    } catch (e: SecurityException) {
        Log.w(TAG, "Not allowed to open $action", e)
        false
    }

    private companion object {
        const val TAG = "SettingsLauncher"
    }
}
