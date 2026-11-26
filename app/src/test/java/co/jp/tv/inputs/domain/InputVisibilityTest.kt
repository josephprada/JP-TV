package co.jp.tv.inputs.domain

import app.cash.turbine.test
import co.jp.tv.inputs.domain.InputSource.Kind
import co.jp.tv.inputs.domain.InputSource.Signal
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.preferences.domain.LauncherConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InputVisibilityTest {
    private class FakeStore(initial: LauncherConfig? = null) : ConfigStore {
        val state = MutableStateFlow(initial)
        override val config: Flow<LauncherConfig?> = state
        override suspend fun update(transform: (LauncherConfig?) -> LauncherConfig) {
            state.value = transform(state.value)
        }
    }

    @Test
    fun `nothing saved means nothing hidden`() = runTest {
        InputVisibility(FakeStore()).hidden.test {
            assertEquals(emptySet<String>(), awaitItem())
        }
    }

    @Test
    fun `hiding and showing update the saved config and keep the rest of it`() = runTest {
        val store = FakeStore(LauncherConfig.Default.copy(brand = "Casa"))
        val visibility = InputVisibility(store)
        visibility.hidden.test {
            assertEquals(emptySet<String>(), awaitItem())
            visibility.hide("hdmi2")
            assertEquals(setOf("hdmi2"), awaitItem())
            visibility.hide("av1")
            assertEquals(setOf("hdmi2", "av1"), awaitItem())
            visibility.show("hdmi2")
            assertEquals(setOf("av1"), awaitItem())
        }
        assertEquals("Casa", store.state.value?.brand)
    }

    @Test
    fun `hiding on a first run creates the default config`() = runTest {
        val store = FakeStore()
        InputVisibility(store).hide("hdmi2")
        assertEquals(LauncherConfig.Default.copy(hiddenInputs = setOf("hdmi2")), store.state.value)
    }

    @Test
    fun `a signal unhides, no signal changes nothing`() = runTest {
        val store = FakeStore(LauncherConfig.Default.copy(hiddenInputs = setOf("hdmi1", "hdmi2")))
        val visibility = InputVisibility(store)
        visibility.revealConnected(listOf(InputSource("hdmi1", Kind.HDMI, null, Signal.STANDBY)))
        assertEquals(setOf("hdmi1", "hdmi2"), store.state.value?.hiddenInputs)
        visibility.revealConnected(listOf(InputSource("hdmi2", Kind.HDMI, null, Signal.CONNECTED)))
        assertEquals(setOf("hdmi1"), store.state.value?.hiddenInputs)
        visibility.revealOnState("hdmi1", 2)
        assertEquals(emptySet<String>(), store.state.value?.hiddenInputs)
    }

    @Test
    fun `revealing with nothing hidden does not write the config`() = runTest {
        val store = FakeStore()
        InputVisibility(store).revealConnected(listOf(InputSource("a", Kind.HDMI, null, Signal.CONNECTED)))
        assertEquals(null, store.state.value)
    }
}
