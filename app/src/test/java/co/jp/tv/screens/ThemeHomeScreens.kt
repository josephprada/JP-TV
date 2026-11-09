package co.jp.tv.screens

import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.wallpaper.WallpaperRegistry
import org.junit.Test

/** One Home render per theme, over the default wallpaper, like MainActivity composes it. */
class ThemeHomeScreens : ScreenshotTest() {
    private fun home(themeId: String) = shoot("home-$themeId") {
        val state = ScreenFixtures.homeState(themeId)
        WallpaperRegistry.byId(state.config.wallpaperId)
            .Render(androidx.compose.ui.graphics.Color(state.config.accent), paused = true, modifier = androidx.compose.ui.Modifier)
        ThemeRegistry.byId(themeId).Home(state, ScreenFixtures.actions)
    }

    @Test fun jptv() = home("jptv")
    @Test fun pixel() = home("pixel")
    @Test fun terminal() = home("terminal")
    @Test fun cartelera() = home("cartelera")
    @Test fun zen() = home("zen")
    @Test fun hud() = home("hud")
    @Test fun vinilo() = home("vinilo")
    @Test fun lofi() = home("lofi")

    @Test fun registryMatchesTests() {
        // Keeps this file honest when a theme is added to the registry.
        val covered = setOf("jptv", "pixel", "terminal", "cartelera", "zen", "hud", "vinilo", "lofi")
        check(ThemeRegistry.all.map { it.id }.toSet() == covered) { "Add the new theme to ThemeHomeScreens" }
        check(LauncherConfig.WALLPAPER_IDS.isNotEmpty())
    }
}
