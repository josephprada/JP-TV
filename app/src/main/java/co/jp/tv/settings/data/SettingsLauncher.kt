package co.jp.tv.settings.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import co.jp.tv.settings.domain.ShortcutTarget

/** Opens native Android settings screens, falling back to the main settings screen when a handler is missing. */
class SettingsLauncher(context: Context) {
    private val appContext = context.applicationContext

    fun open(target: ShortcutTarget.Native): Boolean = open(target.action, target.fallbackAction)

    fun open(action: String, fallbackAction: String): Boolean =
        start(action) || start(fallbackAction)

    private fun start(action: String): Boolean = try {
        appContext.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
