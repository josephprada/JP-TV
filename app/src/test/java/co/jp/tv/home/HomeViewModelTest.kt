package co.jp.tv.home

import app.cash.turbine.test
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.TvApp
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

    private val own = "co.jp.tv"
    private val apps = listOf(
        TvApp("a.yt", "YouTube", true),
        TvApp(own, "JP-TV", true),
        TvApp("a.cam", "Camara", false),
        TvApp("a.nf", "Netflix", true),
    )

    @BeforeEach fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @AfterEach fun tearDown() { Dispatchers.resetMain() }

    private fun vm(catalog: FakeCatalog, ticks: Flow<LocalDateTime> = flowOf(t0)) =
        HomeViewModel(catalog, ticks, ownPackage = own, initialNow = t0)

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
}
