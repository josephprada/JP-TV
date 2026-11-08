package co.jp.tv.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.AppLayout
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn

/** Merges apps, network, clock and focus into the [HomeState] every theme renders. */
class HomeViewModel(
    private val catalog: AppCatalog,
    ticks: Flow<LocalDateTime>,
    ownPackage: String,
    private val monitor: NetworkMonitor,
    initialNow: LocalDateTime = LocalDateTime.now(),
    private val config: LauncherConfig = LauncherConfig.Default,
) : ViewModel(), HomeActions {

    private val requestedFocus = MutableStateFlow(0)
    private val active = MutableStateFlow(true)
    private val backStack = MutableStateFlow<List<Screen>>(emptyList())

    /** Open screens, bottom first; empty means the theme's home. */
    val screens: StateFlow<List<Screen>> = backStack

    /** The screen on top; [Screen.Home] when nothing is open. */
    val screen: StateFlow<Screen> = backStack
        .map { it.lastOrNull() ?: Screen.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Screen.Home)

    // Probes run only while active; going inactive cancels the monitor flow and keeps the last value.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val network: Flow<NetworkStatus> = active
        .flatMapLatest { if (it) monitor.status else emptyFlow() }
        .onStart { emit(NetworkStatus.Unknown) }

    // Arranged once per catalog change so clock ticks reuse the same list instance (keeps the UI skippable).
    private val arrangedApps: Flow<List<TvApp>> = catalog.apps.map { AppLayout.arrange(it, ownPackage) }

    val state: StateFlow<HomeState> = combine(
        arrangedApps,
        ticks,
        requestedFocus,
        network,
    ) { arranged, now, focus, net ->
        HomeState(
            brand = config.brand,
            apps = arranged,
            focusedIndex = focus.coerceIn(0, (arranged.size - 1).coerceAtLeast(0)),
            network = net,
            now = now,
            config = config,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState(
            brand = config.brand,
            apps = emptyList(),
            focusedIndex = 0,
            network = NetworkStatus.Unknown,
            now = initialNow,
            config = config,
        ),
    )

    override fun launch(app: TvApp) = catalog.launch(app)

    override fun focus(index: Int) {
        requestedFocus.value = index
    }

    /** False while the activity is stopped: stops the network probes. */
    fun setActive(isActive: Boolean) {
        active.value = isActive
    }

    fun testConnection() = monitor.probeNow()

    override fun openNetwork() = push(Screen.Network)
    override fun openSettings() = push(Screen.Settings)

    // Wired in phase 6 (theme picker).
    override fun openThemes() = Unit

    /** Closes the top screen. Returns false when already at home. */
    fun back(): Boolean {
        if (backStack.value.isEmpty()) return false
        backStack.update { it.dropLast(1) }
        return true
    }

    private fun push(target: Screen) {
        backStack.update { if (it.lastOrNull() == target) it else it + target }
    }
}
