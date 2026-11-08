package co.jp.tv.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.preferences.domain.LauncherConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Adapter: Jetpack DataStore (Preferences). Brand, accent and wallpaper are not persisted yet (phase 5). */
class DataStoreConfigStore(private val dataStore: DataStore<Preferences>) : ConfigStore {

    override val config: Flow<LauncherConfig?> = dataStore.data.map { it.toConfig() }

    override suspend fun update(transform: (LauncherConfig?) -> LauncherConfig) {
        dataStore.edit { prefs ->
            val next = transform(prefs.toConfig())
            prefs[SAVED] = true
            prefs[APP_ORDER] = next.appOrder.joinToString(SEPARATOR)
            prefs[HIDDEN_APPS] = next.hiddenApps
        }
    }

    private fun Preferences.toConfig(): LauncherConfig? =
        if (this[SAVED] != true) {
            null
        } else {
            LauncherConfig.Default.copy(
                appOrder = this[APP_ORDER].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
                hiddenApps = this[HIDDEN_APPS].orEmpty(),
            )
        }

    private companion object {
        const val SEPARATOR = "\n"
        val SAVED = booleanPreferencesKey("saved")
        val APP_ORDER = stringPreferencesKey("app_order")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
    }
}
