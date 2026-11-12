package co.jp.tv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.jp.tv.core.design.JpColors
import co.jp.tv.preferences.ui.WeatherPlaceCard
import co.jp.tv.weather.domain.WeatherPlace
import androidx.compose.ui.input.key.Key
import co.jp.tv.apps.domain.AppLayout
import co.jp.tv.apps.domain.AppOrdering
import co.jp.tv.apps.ui.EditAppsScreen
import co.jp.tv.network.ui.NetworkPanel
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.ui.CustomizeScreen
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.ui.QuickSettingsInfo
import co.jp.tv.settings.ui.QuickSettingsScreen
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.themes.ui.ThemesScreen
import co.jp.tv.wallpaper.WallpaperRegistry
import org.junit.Test

/** Full-screen panels: Themes, Customize (per wallpaper), Settings, Network and Edit. */
class PanelScreens : ScreenshotTest() {
    private val accent = Color(LauncherConfig.Default.accent)

    @Test fun themes() = shoot("themes") {
        ThemesScreen(activeThemeId = "jptv", accent = accent, onSelect = {}, onCustomize = {})
    }

    private fun customize(wallpaperId: String) = shoot("customize-$wallpaperId") {
        CustomizeScreen(initial = LauncherConfig.Default.copy(wallpaperId = wallpaperId), onApply = {})
    }

    @Test fun customizeEmbers() = customize("embers")
    @Test fun customizeGalaxy() = customize("galaxy")
    @Test fun customizeVideo() = customize("video")
    @Test fun customizeImage() = customize("image")

    private fun customizeFocus(name: String, keys: List<Key>) = shoot("customize-focus-$name", keys) {
        CustomizeScreen(initial = LauncherConfig.Default.copy(wallpaperId = "galaxy"), onApply = {})
    }

    /** Focus ring on a font row, the wallpaper chips, the brand chips and APLICAR (Right x6 then Right). */
    @Test fun customizeFocusFont() = customizeFocus("font", List(2) { Key.DirectionDown })
    @Test fun customizeFocusWallpaper() = customizeFocus("wallpaper", List(7) { Key.DirectionDown })
    @Test fun customizeFocusApply() = customizeFocus("apply", List(7) { Key.DirectionRight })
    @Test fun customizeFocusApplyFromFont() = customizeFocus("apply-from-font", listOf(Key.DirectionDown, Key.DirectionDown, Key.DirectionRight))

    /** Down presses through the option cards until the weather town chips hold focus (the card sits below the fold). */
    @Test fun customizeFocusWeather() = customizeFocus("weather", List(13) { Key.DirectionDown })

    /** The town picker with a search typed: the matches appear as chips under the box. */
    @Test fun customizeWeatherSearch() = shoot("customize-weather-search") {
        Column(Modifier.background(JpColors.Ink).width(400.dp).padding(24.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            WeatherPlaceCard(
                selected = WeatherPlace.QuickPicks[2],
                onSelect = {},
                search = { listOf(WeatherPlace("Girón", "Santander", 7.07, -73.17), WeatherPlace("Girón", "Cundinamarca", 5.0, -74.0)) },
                accent = accent,
                initialQuery = "gir",
            )
        }
    }

    @Test fun settings() = shoot("settings") {
        val info = QuickSettingsInfo(
            launcherName = "JP-TV",
            launcherVersion = "1.0",
            androidRelease = "9",
            deviceModel = "V_BOX14",
            apps = ScreenFixtures.apps.take(5),
            storage = DeviceInfo.Storage(totalBytes = 16L shl 30, freeBytes = 5L shl 30),
            display = 720 to 60,
            volume = 0.6f,
            bluetoothOn = false,
            now = ScreenFixtures.now,
            hourFormat = ClockFormat.HourFormat.H24,
            titleFontId = LauncherConfig.Default.titleFontId,
        )
        QuickSettingsScreen(
            info = info,
            network = ScreenFixtures.network,
            accent = accent,
            onShortcut = {},
            onAllSettings = {},
            onEditApps = {},
        )
    }

    @Test fun network() = shoot("network") {
        ThemeRegistry.default.Home(ScreenFixtures.homeState(), ScreenFixtures.actions)
        NetworkPanel(status = ScreenFixtures.network, accent = accent, onTest = {}, onWifiSettings = {})
    }

    @Test fun edit() = shoot("edit") {
        val order = AppOrdering(
            order = ScreenFixtures.apps.map { it.packageName },
            hidden = setOf("org.videolan.vlc", "com.crunchyroll.crunchyroid"),
        )
        EditAppsScreen(
            apps = AppLayout.resolve(ScreenFixtures.apps, order),
            freeGb = "3.5",
            config = LauncherConfig.Default.copy(wallpaperId = "embers", wallpaperDim = 40),
            onWallpaper = {},
            onDim = {},
            onPauseInApps = {},
            accent = accent,
            initialFocus = null,
            onMove = { _, _ -> },
            onSetHidden = { _, _ -> },
            onReset = {},
        )
    }

    @Test fun wallpapersRegistered() {
        check(WallpaperRegistry.all.map { it.id }.toSet() == setOf("embers", "galaxy", "video", "image"))
    }
}
