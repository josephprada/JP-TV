package co.jp.tv.themes.jptv

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState

/** Default theme: left glass rail, top-right pills, clock card bottom-right. dp = design px / 2. */
object JpTvTheme : LauncherTheme {
    override val id = "jptv"
    override val displayName = "JP-TV"
    override val supportsAccent = true

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val accent = Color(state.config.accent)
        val titleFontId = state.config.titleFontId
        Box(Modifier.fillMaxSize()) {
            AppRail(
                brand = state.brand,
                apps = state.apps,
                focusedIndex = state.focusedIndex,
                accent = accent,
                titleFontId = titleFontId,
                actions = actions,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            TopPills(network = state.network, accent = accent, actions = actions, modifier = Modifier.align(Alignment.TopEnd))
            ClockCard(
                now = state.now,
                accent = accent,
                titleFontId = titleFontId,
                modifier = Modifier.align(Alignment.BottomEnd),
                hourFormat = state.config.hourFormat,
                weather = state.weather,
            )
        }
    }
}
