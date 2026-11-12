package co.jp.tv.settings.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsShortcutsTest {
    @Test
    fun `list starts with the in-launcher network panel`() {
        val first = SettingsShortcuts.tiles.first()
        assertEquals(SettingsShortcut.Id.NETWORK, first.id)
        assertEquals(ShortcutTarget.NetworkPanel, first.target)
    }

    @Test
    fun `native tiles map to the documented intent actions`() {
        val actions = SettingsShortcuts.tiles.mapNotNull { (it.target as? ShortcutTarget.Native)?.action }
        assertEquals(
            listOf(
                "android.settings.BLUETOOTH_SETTINGS",
                "android.settings.DISPLAY_SETTINGS",
                "android.settings.SOUND_SETTINGS",
                "android.settings.APPLICATION_SETTINGS",
                "android.settings.INTERNAL_STORAGE_SETTINGS",
                "android.settings.DATE_SETTINGS",
            ),
            actions,
        )
    }

    @Test
    fun `every native target falls back to the main settings screen`() {
        SettingsShortcuts.tiles.mapNotNull { it.target as? ShortcutTarget.Native }.forEach {
            assertEquals(SettingsShortcuts.ALL_SETTINGS_ACTION, it.fallbackAction)
        }
        assertEquals("android.settings.SETTINGS", SettingsShortcuts.ALL_SETTINGS_ACTION)
    }

    @Test
    fun `wifi settings action is exposed for the network panel`() {
        assertEquals("android.settings.WIFI_SETTINGS", SettingsShortcuts.WIFI_SETTINGS_ACTION)
    }

    @Test
    fun `theme tile opens the themes screen`() {
        val theme = SettingsShortcuts.tiles.single { it.id == SettingsShortcut.Id.THEME }
        assertEquals(ShortcutTarget.Themes, theme.target)
        assertEquals(SettingsShortcut.Id.THEME, SettingsShortcuts.tiles.last().id)
    }

    @Test
    fun `byte counts format as gibibytes with one decimal`() {
        assertEquals("3.5", SettingsShortcuts.formatGb(3_758_096_384L))
        assertEquals("11.0", SettingsShortcuts.formatGb(11_811_160_064L))
        assertEquals("0.0", SettingsShortcuts.formatGb(0L))
    }

    @Test
    fun `the screensaver shortcut opens Android's own daydream picker with display as the fallback`() {
        assertEquals("android.settings.DREAM_SETTINGS", SettingsShortcuts.DREAM_SETTINGS_ACTION)
        assertEquals("android.settings.DISPLAY_SETTINGS", SettingsShortcuts.DISPLAY_SETTINGS_ACTION)
    }
}
