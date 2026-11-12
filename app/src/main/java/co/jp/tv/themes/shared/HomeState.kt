package co.jp.tv.themes.shared

import co.jp.tv.apps.domain.TvApp
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.weather.domain.Weather
import java.time.LocalDateTime

data class HomeState(
    val brand: String,
    /** Visible apps, already ordered. */
    val apps: List<TvApp>,
    val focusedIndex: Int,
    val network: NetworkStatus,
    val now: LocalDateTime,
    val config: LauncherConfig,
    /** Current weather at the chosen place; null until the first reading and when there is none (offline, first run). */
    val weather: Weather? = null,
)
