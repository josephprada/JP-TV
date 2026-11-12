package co.jp.tv.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.AppLayout
import co.jp.tv.apps.domain.AppOrdering
import co.jp.tv.apps.domain.LaunchOrigin
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.weather.domain.PlaceSearch
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherPlace
import co.jp.tv.weather.domain.WeatherRepository
import co.jp.tv.weather.domain.WeatherRules
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Merges apps, network, clock and focus into the [HomeState] every theme renders. */
@OptIn(FlowPreview::class)
class HomeViewModel(
    private val catalog: AppCatalog,
    ticks: Flow<LocalDateTime>,
    ownPackage: String,
    private val monitor: NetworkMonitor,
    initialNow: LocalDateTime = LocalDateTime.now(),
    private val configStore: ConfigStore,
    private val weatherRepository: WeatherRepository = WeatherRepository.None,
    private val placeSearch: PlaceSearch = PlaceSearch.None,
) : ViewModel(), HomeActions {

    /** Visible and hidden apps resolved against the saved arrangement, plus the saved config. */
    private data class Layout(val apps: AppLayout.ResolvedApps, val config: LauncherConfig)

    /** False until the saved config is read, so a cold start never draws the default theme and then swaps it. */
    val ready: StateFlow<Boolean> = configStore.config
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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

    // Latest installed packages in default order; lets edits run without re-querying the catalog.
    @Volatile
    private var installedPackages: List<String> = emptyList()

    // Default arrangement of the catalog; clock ticks reuse the same list instance (keeps the UI skippable).
    private val installed: Flow<List<TvApp>> = catalog.apps
        .map { AppLayout.arrange(it, ownPackage) }
        .onEach { list -> installedPackages = list.map { it.packageName } }

    private val layout: Flow<Layout> = combine(installed, configStore.config) { apps, saved ->
        val packages = apps.map { it.packageName }
        Layout(AppLayout.resolve(apps, orderingOf(saved, packages)), saved ?: LauncherConfig.Default)
    }

    // Last reading for the chosen place; kept while inactive, cleared when the place changes.
    private val weather = MutableStateFlow<Weather?>(null)

    private val appEdits = Channel<(AppOrdering) -> AppOrdering>(Channel.UNLIMITED)

    init {
        // First run: persist the default arrangement once the catalog has apps and has stopped changing.
        viewModelScope.launch {
            val apps = installed.debounce(SEED_SETTLE_MS).first { it.isNotEmpty() }
            val packages = apps.map { it.packageName }
            configStore.update { saved ->
                if (saved == null || saved.appOrder.isEmpty()) {
                    (saved ?: LauncherConfig.Default).withOrdering(AppLayout.firstRun(packages))
                } else {
                    saved
                }
            }
        }
        // Weather follows the chosen place and the clock (at most one ask a minute; the repository decides
        // whether that means the network). Nothing runs while the activity is stopped.
        viewModelScope.launch {
            val places = configStore.config.map { (it ?: LauncherConfig.Default).weatherPlace }.distinctUntilChanged()
            combine(active, places) { isActive, place -> isActive to place }
                .distinctUntilChanged()
                .collectLatest { (isActive, place) ->
                    if (!isActive) return@collectLatest
                    weather.value = weatherRepository.cached(place)
                    ticks
                        .distinctUntilChanged { a, b -> a.truncatedTo(ChronoUnit.MINUTES) == b.truncatedTo(ChronoUnit.MINUTES) }
                        .collect { now -> weather.value = weatherRepository.current(place, now) }
                }
        }
        // Edits are applied one at a time so rapid key repeats never interleave.
        viewModelScope.launch {
            for (edit in appEdits) {
                val packages = installedPackages.ifEmpty { installed.first().map { it.packageName } }
                if (packages.isEmpty()) continue // nothing installed yet: never persist an empty arrangement
                configStore.update { saved ->
                    (saved ?: LauncherConfig.Default).withOrdering(edit(orderingOf(saved, packages)))
                }
            }
        }
    }

    /** Apps in the rail (visible) and off it (hidden), for the editor. */
    val editState: StateFlow<AppLayout.ResolvedApps> = layout
        .map { it.apps }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLayout.ResolvedApps(emptyList(), emptyList()))

    val state: StateFlow<HomeState> = combine(
        layout,
        ticks,
        requestedFocus,
        network,
        weather,
    ) { layout, now, focus, net, reading ->
        HomeState(
            brand = layout.config.brand,
            apps = layout.apps.visible,
            focusedIndex = focus.coerceIn(0, (layout.apps.visible.size - 1).coerceAtLeast(0)),
            network = net,
            now = now,
            config = layout.config,
            weather = WeatherRules.withStaleness(reading, now),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeState(
            brand = LauncherConfig.Default.brand,
            apps = emptyList(),
            focusedIndex = 0,
            network = NetworkStatus.Unknown,
            now = initialNow,
            config = LauncherConfig.Default,
        ),
    )

    private fun orderingOf(saved: LauncherConfig?, packages: List<String>): AppOrdering =
        saved?.takeIf { it.appOrder.isNotEmpty() }
            ?.let { AppOrdering(it.appOrder, it.hiddenApps).reconcile(packages) }
            ?: AppLayout.firstRun(packages)

    private fun LauncherConfig.withOrdering(ordering: AppOrdering) =
        copy(appOrder = ordering.order, hiddenApps = ordering.hidden)

    override fun launch(app: TvApp) = catalog.launch(app)

    /** Launch with the bounds of the focused tile so the window animation can grow out of it. */
    fun launch(app: TvApp, origin: LaunchOrigin?) = catalog.launch(app, origin)

    override fun focus(index: Int) {
        requestedFocus.value = index
    }

    /** False while the activity is stopped: stops the network probes. */
    fun setActive(isActive: Boolean) {
        active.value = isActive
    }

    fun testConnection() = monitor.probeNow()

    /** The location permission or switch changed: read the Wi-Fi name again. */
    fun refreshNetworkAccess() = monitor.refreshAccess()

    override fun openNetwork() = push(Screen.Network)
    override fun openSettings() = push(Screen.Settings)

    /** Package the editor should focus first, or null for the top of the rail. */
    var editFocusPackage: String? = null
        private set

    override fun openEditApps(focus: TvApp?) {
        editFocusPackage = focus?.packageName
        push(Screen.EditApps)
    }

    fun moveApp(packageName: String, up: Boolean) {
        appEdits.trySend { if (up) it.moveUp(packageName) else it.moveDown(packageName) }
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        appEdits.trySend { if (hidden) it.hide(packageName) else it.show(packageName) }
    }

    /** Back to the first-run arrangement: favorites first, system tools hidden. */
    fun resetApps() {
        appEdits.trySend { AppLayout.firstRun(installedPackages) }
    }

    override fun openThemes() = push(Screen.Themes)

    override fun openCustomize() = push(Screen.Customize)

    /** Persists appearance fields from the Customize / Themes screens. */
    fun applyAppearance(transform: (LauncherConfig) -> LauncherConfig) {
        viewModelScope.launch {
            configStore.update { saved -> transform(saved ?: LauncherConfig.Default) }
        }
    }

    fun applyCustomize(config: LauncherConfig) {
        applyAppearance {
            it.copy(
                brand = config.brand,
                accent = config.accent,
                titleFontId = config.titleFontId,
                wallpaperId = config.wallpaperId,
                hourFormat = config.hourFormat,
                weatherPlace = config.weatherPlace,
            )
        }
        back()
    }

    /** Town search for the weather picker; empty when nothing matches or the network is down. */
    suspend fun searchPlaces(query: String): List<WeatherPlace> = placeSearch.search(query)

    fun setWallpaper(id: String) = applyAppearance { it.copy(wallpaperId = id) }

    fun setWallpaperDim(percent: Int) = applyAppearance { it.copy(wallpaperDim = percent.coerceIn(0, LauncherConfig.MAX_DIM)) }

    fun setPauseWallpaperInApps(pause: Boolean) = applyAppearance { it.copy(pauseWallpaperInApps = pause) }

    fun setTheme(themeId: String) {
        applyAppearance { it.copy(themeId = themeId) }
    }

    /** Closes the top screen. Returns false when already at home. */
    fun back(): Boolean {
        if (backStack.value.isEmpty()) return false
        backStack.update { it.dropLast(1) }
        return true
    }

    /** Home key: drops every open screen at once. Returns false when already at home. */
    fun closeAll(): Boolean {
        if (backStack.value.isEmpty()) return false
        backStack.value = emptyList()
        return true
    }

    private fun push(target: Screen) {
        backStack.update { if (it.lastOrNull() == target) it else it + target }
    }

    private companion object {
        /** Quiet time on the catalog before the first-run arrangement is persisted. */
        const val SEED_SETTLE_MS = 1_000L
    }
}
