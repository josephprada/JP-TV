package co.jp.tv.home

import app.cash.turbine.test
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.preferences.domain.LauncherConfig
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
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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

    private class FakeConfigStore(initial: LauncherConfig? = null) : ConfigStore {
        val source = MutableStateFlow(initial)
        override val config: Flow<LauncherConfig?> = source
        override suspend fun update(transform: (LauncherConfig?) -> LauncherConfig) {
            source.value = transform(source.value)
        }
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
        store: FakeConfigStore = FakeConfigStore(),
    ) = HomeViewModel(catalog, ticks, ownPackage = own, monitor = monitor, initialNow = t0, configStore = store)

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

    private val manyApps = listOf(
        TvApp("a.nf", "Netflix", true),
        TvApp("a.yt", "YouTube", true),
        TvApp("a.sp", "Spotify", true),
        TvApp("a.cam", "Camara", false),
    )

    private fun visible(vm: HomeViewModel) = vm.state.value.apps.map { it.packageName }

    @Test
    fun `saved order and hidden apps shape the rail and the count`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.sp", "a.cam", "a.nf", "a.yt"), hiddenApps = setOf("a.cam")))
        val vm = vm(FakeCatalog(manyApps), store = store)
        vm.state.test {
            expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() }
            assertEquals(listOf("a.sp", "a.nf", "a.yt"), visible(vm))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `first run persists favorites first and hides system tools`() = runTest {
        val apps = listOf(
            TvApp("com.android.tv.settings", "Settings", true),
            TvApp("a.nf2", "Aaa", true),
            TvApp("com.spotify.tv.android", "Spotify", true),
            TvApp("com.xuper.netxxus", "Xuper Hydra", true),
        )
        val store = FakeConfigStore()
        val vm = vm(FakeCatalog(apps), store = store)
        vm.state.test {
            expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() }
            advanceUntilIdle()
            assertEquals(listOf("com.xuper.netxxus", "com.spotify.tv.android", "a.nf2"), visible(vm))
            assertEquals(setOf("com.android.tv.settings"), store.source.value?.hiddenApps)
            assertEquals("com.xuper.netxxus", store.source.value?.appOrder?.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `new apps are appended after the saved order`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.yt", "a.nf")))
        val vm = vm(FakeCatalog(manyApps), store = store)
        vm.state.test {
            expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() }
            assertEquals(listOf("a.yt", "a.nf", "a.sp", "a.cam"), visible(vm))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `moving an app persists the new order`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.nf", "a.yt", "a.sp", "a.cam")))
        val vm = vm(FakeCatalog(manyApps), store = store)
        vm.state.test {
            expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() }
            vm.moveApp("a.sp", up = true)
            assertEquals(listOf("a.nf", "a.sp", "a.yt", "a.cam"), store.source.value?.appOrder)
            vm.moveApp("a.nf", up = false)
            assertEquals(listOf("a.sp", "a.nf", "a.yt", "a.cam"), visible(vm))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hiding and showing an app persists and updates the edit lists`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.nf", "a.yt", "a.sp", "a.cam")))
        val vm = vm(FakeCatalog(manyApps), store = store)
        vm.editState.test {
            expectMostRecentItem().let { if (it.visible.isEmpty()) awaitItem() }
            vm.setAppHidden("a.yt", hidden = true)
            expectMostRecentItem().let {
                assertEquals(listOf("a.nf", "a.sp", "a.cam"), it.visible.map { a -> a.packageName })
                assertEquals(listOf("a.yt"), it.hidden.map { a -> a.packageName })
            }
            assertEquals(setOf("a.yt"), store.source.value?.hiddenApps)
            vm.setAppHidden("a.yt", hidden = false)
            assertEquals(listOf("a.nf", "a.yt", "a.sp", "a.cam"), expectMostRecentItem().visible.map { it.packageName })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset restores the first run arrangement`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.cam", "a.sp", "a.yt", "a.nf"), hiddenApps = setOf("a.nf")))
        val vm = vm(FakeCatalog(manyApps), store = store)
        vm.state.test {
            expectMostRecentItem().let { if (it.apps.isEmpty()) awaitItem() }
            vm.resetApps()
            assertEquals(emptySet<String>(), store.source.value?.hiddenApps)
            assertEquals(listOf("a.nf", "a.sp", "a.yt", "a.cam"), visible(vm))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset with an empty catalog does not persist an empty order`() = runTest {
        val saved = LauncherConfig(appOrder = listOf("a.nf", "a.yt"), hiddenApps = setOf("a.yt"))
        val store = FakeConfigStore(saved)
        val vm = vm(FakeCatalog(emptyList()), store = store)
        vm.resetApps()
        advanceUntilIdle()
        assertEquals(saved, store.source.value)
    }

    @Test
    fun `first run seed waits for the catalog to settle`() = runTest {
        val catalog = FakeCatalog(listOf(TvApp("a.nf", "Netflix", true)))
        val store = FakeConfigStore()
        vm(catalog, store = store)
        advanceTimeBy(200)
        catalog.source.value = manyApps
        advanceUntilIdle()
        assertEquals(setOf("a.nf", "a.yt", "a.sp", "a.cam"), store.source.value?.appOrder?.toSet())
    }

    @Test
    fun `applying customize persists the clock format with the other appearance fields`() = runTest {
        val store = FakeConfigStore(LauncherConfig(appOrder = listOf("a.yt")))
        val vm = vm(FakeCatalog(apps), store = store)
        vm.applyCustomize(LauncherConfig(brand = "Casa", hourFormat = HourFormat.H12))
        advanceUntilIdle()
        assertEquals(HourFormat.H12, store.source.value?.hourFormat)
        assertEquals("Casa", store.source.value?.brand)
    }

    @Test
    fun `appearance changes before the first run seed do not suppress it`() = runTest {
        val store = FakeConfigStore()
        val catalog = FakeCatalog(emptyList())
        val vm = vm(catalog, store = store)
        vm.setTheme("pixel")
        catalog.source.value = manyApps
        advanceUntilIdle()
        assertEquals("pixel", store.source.value?.themeId)
        assertEquals(manyApps.map { it.packageName }.toSet(), store.source.value?.appOrder?.toSet())
    }

    @Test
    fun `opening the editor remembers which app to focus and back closes it`() {
        val vm = vm(FakeCatalog(manyApps))
        vm.openEditApps(manyApps[1])
        assertEquals(Screen.EditApps, vm.screen.value)
        assertEquals("a.yt", vm.editFocusPackage)
        vm.back()
        assertEquals(Screen.Home, vm.screen.value)
        vm.openEditApps(null)
        assertEquals(null, vm.editFocusPackage)
    }
}
