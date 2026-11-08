package co.jp.tv.themes.jptv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState

/** Default theme: left glass rail, clock card bottom-right. Sizes are the 1920x1080 design halved to dp. */
object JpTvTheme : LauncherTheme {
    override val id = "jptv"
    override val displayName = "JP-TV"
    override val supportsAccent = true

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        Box(Modifier.fillMaxSize().background(JpTvPalette.Ink)) {
            AppRail(state, actions, Modifier.align(Alignment.CenterStart))
            ClockCard(state, Modifier.align(Alignment.BottomEnd))
        }
    }
}
