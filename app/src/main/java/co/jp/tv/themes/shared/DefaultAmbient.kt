package co.jp.tv.themes.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.wallpaper.WallpaperRegistry

/** What a theme without its own scene shows as the screensaver's ambient: the wallpaper the user picked. */
@Composable
fun DefaultAmbient(state: HomeState) {
    WallpaperRegistry.byId(state.config.wallpaperId)
        .Render(Color(state.config.accent), LocalPaused.current, Modifier.fillMaxSize())
}
