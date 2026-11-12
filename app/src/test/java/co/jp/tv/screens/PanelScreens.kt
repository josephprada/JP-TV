package co.jp.tv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
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
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.playlist.ui.LocalImageSource
import co.jp.tv.playlist.ui.LocalMediaThumbnails
import co.jp.tv.preferences.ui.CustomizeScreen
import co.jp.tv.preferences.ui.OptionsCard
import co.jp.tv.preferences.ui.PlaylistSection
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.homeoverride.domain.HomeOverrideState
import co.jp.tv.homeoverride.ui.HomeOverrideRow
import co.jp.tv.homeoverride.ui.HomeOverrideUi
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

    /** The wallpaper card with a playlist: toggle chips with previews, rotation and order chips, the hint. */
    private fun customizePlaylist(name: String, wallpaperId: String, keys: List<Key> = emptyList()) = shoot(name, keys) {
        val files = if (wallpaperId == "video") ScreenFixtures.library.videos else ScreenFixtures.library.images
        val config = LauncherConfig.Default.copy(wallpaperId = wallpaperId).withPlaylist(
            if (wallpaperId == "video") MediaKind.VIDEO else MediaKind.IMAGE,
            PlaylistSettings(selected = listOf(files[0].id, files[1].id, files[3].id), interval = RotationInterval.MIN_30, order = PlaylistOrder.SHUFFLE),
        )
        CompositionLocalProvider(LocalMediaThumbnails provides ScreenFixtures.thumbnails, LocalImageSource provides ScreenFixtures.imageSource) {
            CustomizeScreen(initial = config, onApply = {}, media = ScreenFixtures.library)
        }
    }

    @Test fun customizeVideoList() = customizePlaylist("customize-video-list", "video", List(9) { Key.DirectionDown })
    /** Further down the same list: the rotation chips and the order chips are reachable with the D-pad too. */
    @Test fun customizeVideoListOrder() = customizePlaylist("customize-video-list-order", "video", List(16) { Key.DirectionDown })
    @Test fun customizeImageList() = customizePlaylist("customize-image-list", "image", List(9) { Key.DirectionDown })

    /** The card on its own, so the whole list is visible whatever the scroll position. */
    @Test fun playlistCard() = shoot("customize-playlist-card") {
        CompositionLocalProvider(LocalMediaThumbnails provides ScreenFixtures.thumbnails) {
            Column(Modifier.background(JpColors.Ink).width(400.dp).padding(24.dp)) {
                OptionsCard("LIVE WALLPAPER", delayMs = 0) {
                    PlaylistSection(
                        kind = MediaKind.VIDEO,
                        found = ScreenFixtures.library.videos,
                        loaded = true,
                        settings = PlaylistSettings(selected = listOf("/sdcard/JPTV/loop.mp4", "/sdcard/JPTV/fondos/aurora.mp4", "/storage/ABCD-1234/JPTV/fondos/playa.mp4")),
                        accent = accent,
                        onChange = {},
                    )
                }
            }
        }
    }

    private fun overrideRow(name: String, state: HomeOverrideState) = shoot(name) {
        Column(Modifier.background(JpColors.Ink).width(620.dp).padding(24.dp)) {
            HomeOverrideRow(HomeOverrideUi(state, {}, {}, {}, {}), accent)
        }
    }

    @Test fun homeOverrideDisabled() = overrideRow("home-override-disabled", HomeOverrideState.of(false, false, false, false))
    @Test fun homeOverrideMissingBoth() = overrideRow("home-override-missing-both", HomeOverrideState.of(true, false, false, false))
    @Test fun homeOverrideMissingOverlay() = overrideRow("home-override-missing-overlay", HomeOverrideState.of(true, false, true, false))
    @Test fun homeOverrideActiveUsage() = overrideRow("home-override-active-usage", HomeOverrideState.of(true, false, true, true))
    @Test fun homeOverrideActiveAccessibility() = overrideRow("home-override-active-accessibility", HomeOverrideState.of(true, true, false, false))

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
            homeOverride = HomeOverrideUi(HomeOverrideState.of(true, false, false, false), {}, {}, {}, {}),
            screensaver = ScreensaverSettings.Default,
            onOpenScreensaver = {},
        )
    }

    @Test fun network() = shoot("network") {
        ThemeRegistry.default.Home(ScreenFixtures.homeState(), ScreenFixtures.actions)
        NetworkPanel(status = ScreenFixtures.network, accent = accent, onTest = {}, onWifiSettings = {})
    }

    @Test fun networkNeedsLocation() = shoot("network-location") {
        ThemeRegistry.default.Home(ScreenFixtures.homeState(), ScreenFixtures.actions)
        NetworkPanel(
            status = ScreenFixtures.network.copy(ssid = null, ssidAccess = co.jp.tv.network.domain.SsidAccess.NeedsLocation),
            accent = accent, onTest = {}, onWifiSettings = {},
        )
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

    /** The edit screen's wallpaper column mirrors the playlist when "Mi video" is the wallpaper. */
    @Test fun editVideoPlaylist() = shoot("edit-video") {
        val order = AppOrdering(order = ScreenFixtures.apps.map { it.packageName }, hidden = emptySet())
        EditAppsScreen(
            apps = AppLayout.resolve(ScreenFixtures.apps, order),
            freeGb = "3.5",
            config = LauncherConfig.Default.copy(wallpaperId = "video"),
            onWallpaper = {},
            onDim = {},
            onPauseInApps = {},
            accent = accent,
            initialFocus = null,
            onMove = { _, _ -> },
            onSetHidden = { _, _ -> },
            onReset = {},
            media = ScreenFixtures.library,
        )
    }

    @Test fun wallpapersRegistered() {
        check(WallpaperRegistry.all.map { it.id }.toSet() == setOf("embers", "galaxy", "video", "image"))
    }
}
