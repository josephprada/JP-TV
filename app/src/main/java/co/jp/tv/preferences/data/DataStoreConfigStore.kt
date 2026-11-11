package co.jp.tv.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.domain.ConfigStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Adapter: Jetpack DataStore (Preferences) for the full [LauncherConfig]. */
class DataStoreConfigStore(private val dataStore: DataStore<Preferences>) : ConfigStore {

    override val config: Flow<LauncherConfig?> = dataStore.data.map { it.toConfig() }

    override suspend fun update(transform: (LauncherConfig?) -> LauncherConfig) {
        dataStore.edit { prefs ->
            val next = transform(prefs.toConfig())
            prefs[SAVED] = true
            prefs[BRAND] = next.brand
            prefs[ACCENT] = next.accent
            prefs[TITLE_FONT] = next.titleFontId
            prefs[WALLPAPER] = next.wallpaperId
            prefs[THEME] = next.themeId
            prefs[APP_ORDER] = next.appOrder.joinToString(SEPARATOR)
            prefs[HIDDEN_APPS] = next.hiddenApps
            prefs[HOUR_FORMAT] = next.hourFormat.name
        }
    }

    private fun Preferences.toConfig(): LauncherConfig? =
        if (this[SAVED] != true) {
            null
        } else {
            LauncherConfig(
                brand = this[BRAND] ?: LauncherConfig.DEFAULT_BRAND,
                accent = this[ACCENT] ?: LauncherConfig.Default.accent,
                titleFontId = this[TITLE_FONT] ?: LauncherConfig.Default.titleFontId,
                wallpaperId = this[WALLPAPER] ?: LauncherConfig.DEFAULT_WALLPAPER_ID,
                themeId = this[THEME] ?: LauncherConfig.DEFAULT_THEME_ID,
                appOrder = this[APP_ORDER].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
                hiddenApps = this[HIDDEN_APPS].orEmpty(),
                hourFormat = HourFormat.entries.firstOrNull { it.name == this[HOUR_FORMAT] } ?: HourFormat.H24,
            )
        }

    private companion object {
        const val SEPARATOR = "\n"
        val SAVED = booleanPreferencesKey("saved")
        val BRAND = stringPreferencesKey("brand")
        val ACCENT = longPreferencesKey("accent")
        val TITLE_FONT = stringPreferencesKey("title_font")
        val WALLPAPER = stringPreferencesKey("wallpaper")
        val THEME = stringPreferencesKey("theme")
        val APP_ORDER = stringPreferencesKey("app_order")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")

        /** Absent on installs from before the setting existed; reads as 24-hour. */
        val HOUR_FORMAT = stringPreferencesKey("hour_format")
    }
}
