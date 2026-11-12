package co.jp.tv.screensaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverState
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.themes.shared.HomeState

/**
 * One saver mode, full screen. The same composable is hosted by the in-app overlay and by the system
 * DreamService, so a mode looks identical in both. [mode] is a concrete mode: [ScreensaverMode.RANDOM] is
 * resolved by [ScreensaverHost]; a mode with nothing to show (no photos, no videos) falls back to the clock.
 */
@Composable
fun ScreensaverContent(mode: ScreensaverMode, state: ScreensaverState, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.Black)) {
        when {
            mode == ScreensaverMode.PHOTOS && state.photos.isNotEmpty() -> PhotosSaver(state)
            mode == ScreensaverMode.LIVE && state.videos.items.isNotEmpty() -> LiveSaver(state)
            mode == ScreensaverMode.AMBIENT -> AmbientSaver(state)
            else -> ClockSaver(state)
        }
    }
}

/** What a theme's [co.jp.tv.themes.LauncherTheme.Ambient] needs: the clock, the weather and the config, no apps. */
internal fun ScreensaverState.toHomeState() = HomeState(
    brand = config.brand,
    apps = emptyList(),
    focusedIndex = 0,
    network = NetworkStatus.Unknown,
    now = now,
    config = config,
    weather = weather,
)

@Composable
private fun AmbientSaver(state: ScreensaverState) {
    val home = remember(state) { state.toHomeState() }
    Box(Modifier.fillMaxSize()) {
        ThemeRegistry.byId(state.config.themeId).Ambient(home)
        CornerClock(state)
    }
}
