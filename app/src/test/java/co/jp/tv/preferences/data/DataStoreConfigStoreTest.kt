package co.jp.tv.preferences.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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
