package co.jp.tv.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.jp.tv.apps.domain.AppCatalog
import co.jp.tv.apps.domain.AppLayout
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Merges apps, clock and focus into the [HomeState] every theme renders. */
class HomeViewModel(
    private val catalog: AppCatalog,
    ticks: Flow<LocalDateTime>,
    ownPackage: String,
    initialNow: LocalDateTime = LocalDateTime.now(),
    private val config: LauncherConfig = LauncherConfig.Default,
) : ViewModel(), HomeActions {

    private val requestedFocus = MutableStateFlow(0)

    val state: StateFlow<HomeState> = combine(
        catalog.apps,
        ticks,
        requestedFocus,
    ) { apps, now, focus ->
        val arranged = AppLayout.arrange(apps, ownPackage)
        HomeState(
            brand = config.brand,
            apps = arranged,
            focusedIndex = focus.coerceIn(0, (arranged.size - 1).coerceAtLeast(0)),
            network = NetworkStatus.Unknown,
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

    // Wired in phase 3 (network panel, quick settings) and phase 6 (theme picker).
    override fun openNetwork() = Unit
    override fun openSettings() = Unit
    override fun openThemes() = Unit
}
