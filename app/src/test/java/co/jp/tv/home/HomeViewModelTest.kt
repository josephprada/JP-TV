package co.jp.tv.home

import app.cash.turbine.test
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.network.domain.NetworkStatus
import java.time.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val t0 = LocalDateTime.of(2025, 11, 8, 21, 40, 0)

    private class FakeCatalog(initial: List<TvApp>) : AppCatalog {
        val source = MutableStateFlow(initial)
        val launched = mutableListOf<TvApp>()
        override val apps: Flow<List<TvApp>> = source
        override fun launch(app: TvApp) { launched += app }
    }

    private class FakeMonitor : NetworkMonitor {
        val source = MutableStateFlow(NetworkStatus.Unknown)
        var collectors = 0
        var probes = 0
        override val status: Flow<NetworkStatus> = source
            .onStart { collectors++ }
            .onCompletion { collectors-- }
        override fun probeNow() { probes++ }
    }

    private val own = "co.jp.tv"
    private val apps = listOf(
        TvApp("a.yt", "YouTube", true),
        TvApp(own, "JP-TV", true),
        TvApp("a.cam", "Camara", false),
        TvApp("a.nf", "Netflix", true),
    )

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach fun tearDown() { Dispatchers.resetMain() }

    private fun vm(
        catalog: FakeCatalog,
        ticks: Flow<LocalDateTime> = flowOf(t0),
        monitor: FakeMonitor = FakeMonitor(),
    ) = HomeViewModel(catalog, ticks, ownPackage = own, monitor = monitor, initialNow = t0)

    @Test
    fun `state exposes arranged apps without our own package`() = runTest {
        vm(FakeCatalog(apps)).state.test {
            val s = expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() else it }
            assertEquals(listOf("Netflix", "YouTube", "Camara"), s.apps.map { it.label })
            assertEquals(0, s.focusedIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `focus is clamped to the list bounds`() = runTest {
        val vm = vm(FakeCatalog(apps))
        vm.state.test {
            awaitItem()
            vm.focus(99)
            assertEquals(2, expectMostRecentItem().focusedIndex)
            vm.focus(-5)
            assertEquals(0, expectMostRecentItem().focusedIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `focus is re-clamped when the list shrinks`() = runTest {
        val catalog = FakeCatalog(apps)
        val vm = vm(catalog)
        vm.state.test {
            awaitItem()
            vm.focus(2)
            expectMostRecentItem()
            catalog.source.value = listOf(TvApp("a.nf", "Netflix", true))
            assertEquals(0, expectMostRecentItem().focusedIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clock ticks update now`() = runTest {
        val ticks = MutableSharedFlow<LocalDateTime>(replay = 1).apply { tryEmit(t0) }
        vm(FakeCatalog(apps), ticks).state.test {
            awaitItem()
            ticks.emit(t0.plusMinutes(1))
            assertEquals(t0.plusMinutes(1), expectMostRecentItem().now)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `launch delegates to the catalog`() = runTest {
        val catalog = FakeCatalog(apps)
        val target = apps.first()
        vm(catalog).launch(target)
        assertEquals(listOf(target), catalog.launched)
    }

    private val wifi = NetworkStatus(
        state = NetworkStatus.State.CONNECTED,
        transport = NetworkStatus.Transport.WIFI,
        ip = "192.168.1.4",
        hasInternet = true,
    )

    @Test
    fun `state carries the live network status`() = runTest {
        val monitor = FakeMonitor()
        vm(FakeCatalog(apps), monitor = monitor).state.test {
            awaitItem()
            monitor.source.value = wifi
            assertEquals(wifi, expectMostRecentItem().network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network probing stops while inactive and resumes when active again`() = runTest {
        val monitor = FakeMonitor()
        val vm = vm(FakeCatalog(apps), monitor = monitor)
        vm.state.test {
            awaitItem()
            assertEquals(1, monitor.collectors)
            vm.setActive(false)
            assertEquals(0, monitor.collectors)
            vm.setActive(true)
            assertEquals(1, monitor.collectors)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `last known network status is kept while inactive`() = runTest {
        val monitor = FakeMonitor()
        val vm = vm(FakeCatalog(apps), monitor = monitor)
        vm.state.test {
            awaitItem()
            monitor.source.value = wifi
            expectMostRecentItem()
            vm.setActive(false)
            assertEquals(wifi, vm.state.value.network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test connection asks the monitor to probe now`() {
        val monitor = FakeMonitor()
        vm(FakeCatalog(apps), monitor = monitor).testConnection()
        assertEquals(1, monitor.probes)
    }

    @Test
    fun `screens stack and back unwinds them`() {
        val vm = vm(FakeCatalog(apps))
        assertEquals(Screen.Home, vm.screen.value)
        vm.openSettings()
        assertEquals(Screen.Settings, vm.screen.value)
        vm.openNetwork()
        assertEquals(Screen.Network, vm.screen.value)
        vm.back()
        assertEquals(Screen.Settings, vm.screen.value)
        vm.back()
        assertEquals(Screen.Home, vm.screen.value)
        vm.back()
        assertEquals(Screen.Home, vm.screen.value)
    }

    @Test
    fun `opening the same screen twice does not stack it`() {
        val vm = vm(FakeCatalog(apps))
        vm.openNetwork()
        vm.openNetwork()
        vm.back()
        assertEquals(Screen.Home, vm.screen.value)
    }
}
