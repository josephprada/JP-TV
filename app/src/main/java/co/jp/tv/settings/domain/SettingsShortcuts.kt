package co.jp.tv.settings.domain

import java.util.Locale

/**
 * The quick settings tiles. Actions are the string values of the `android.provider.Settings.ACTION_*`
 * constants so this file stays free of Android imports.
 */
object SettingsShortcuts {
    const val ALL_SETTINGS_ACTION = "android.settings.SETTINGS"
    const val WIFI_SETTINGS_ACTION = "android.settings.WIFI_SETTINGS"
    const val LOCATION_SETTINGS_ACTION = "android.settings.LOCATION_SOURCE_SETTINGS"

    /** Android's own screensaver (Daydream) picker; the user chooses JP-TV there. */
    const val DREAM_SETTINGS_ACTION = "android.settings.DREAM_SETTINGS"
    const val DISPLAY_SETTINGS_ACTION = "android.settings.DISPLAY_SETTINGS"

    private fun native(id: SettingsShortcut.Id, action: String) =
        SettingsShortcut(id, ShortcutTarget.Native(action, ALL_SETTINGS_ACTION))

    val tiles: List<SettingsShortcut> = listOf(
        SettingsShortcut(SettingsShortcut.Id.NETWORK, ShortcutTarget.NetworkPanel),
        native(SettingsShortcut.Id.BLUETOOTH, "android.settings.BLUETOOTH_SETTINGS"),
        native(SettingsShortcut.Id.DISPLAY, "android.settings.DISPLAY_SETTINGS"),
        native(SettingsShortcut.Id.SOUND, "android.settings.SOUND_SETTINGS"),
        native(SettingsShortcut.Id.APPS, "android.settings.APPLICATION_SETTINGS"),
        native(SettingsShortcut.Id.STORAGE, "android.settings.INTERNAL_STORAGE_SETTINGS"),
        native(SettingsShortcut.Id.DATE_TIME, "android.settings.DATE_SETTINGS"),
        SettingsShortcut(SettingsShortcut.Id.THEME, ShortcutTarget.Themes),
    )

    /** Bytes as GiB with one decimal, e.g. "3.5". */
    fun formatGb(bytes: Long): String = String.format(Locale.ROOT, "%.1f", bytes / (1024.0 * 1024.0 * 1024.0))
}
