package co.jp.tv.preferences.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.preferences.domain.LauncherConfig
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreConfigStoreTest {
    @TempDir lateinit var dir: File

    private fun store(name: String = "prefs") = DataStoreConfigStore(
        PreferenceDataStoreFactory.create(produceFile = { File(dir, "$name.preferences_pb") }),
    )

    @Test
    fun `nothing saved reads as null`() = runTest {
        assertNull(store().config.first())
    }

    @Test
    fun `saved order and hidden apps round trip`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(appOrder = listOf("b", "a", "c"), hiddenApps = setOf("c")) }
        val read = store.config.first()
        assertEquals(listOf("b", "a", "c"), read?.appOrder)
        assertEquals(setOf("c"), read?.hiddenApps)
    }

    @Test
    fun `appearance fields round trip`() = runTest {
        val store = store()
        val written = LauncherConfig.Default.copy(
            brand = "Casa",
            accent = 0xFF3D8BFD,
            titleFontId = "bebas_neue",
            wallpaperId = "embers",
            themeId = "jptv",
            appOrder = listOf("a"),
        )
        store.update { written }
        assertEquals(written, store.config.first())
    }

    @Test
    fun `12 hour clock format round trips`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(hourFormat = HourFormat.H12) }
        assertEquals(HourFormat.H12, store.config.first()?.hourFormat)
        store.update { it!!.copy(hourFormat = HourFormat.H24) }
        assertEquals(HourFormat.H24, store.config.first()?.hourFormat)
    }

    @Test
    fun `a saved config without a clock format reads as 24 hour`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("brand")] = "Casa"
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals("Casa", read?.brand)
        assertEquals(HourFormat.H24, read?.hourFormat)
    }

    @Test
    fun `an empty saved order is still a saved config`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default }
        assertEquals(LauncherConfig.Default, store.config.first())
    }

    @Test
    fun `update receives the previous value`() = runTest {
        val store = store()
        store.update { LauncherConfig(appOrder = listOf("a")) }
        store.update { it!!.copy(hiddenApps = setOf("a")) }
        assertEquals(LauncherConfig(appOrder = listOf("a"), hiddenApps = setOf("a")), store.config.first())
    }
}
