package co.jp.tv.screens

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.AppIcons
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCondition
import java.time.LocalDateTime

/** Fake data for the off-device screenshots. Deterministic: no clock, no PackageManager. */
object ScreenFixtures {
    /** Saturday 8 Nov, matching the artboards. */
    val now: LocalDateTime = LocalDateTime.of(2025, 11, 8, 21, 40)

    private val catalog = listOf(
        Triple("com.xuper.netxxus", "Xuper Hydra", 0xFF6C3BD1),
        Triple("com.google.android.youtube.tv", "YouTube", 0xFFD32F2F),
        Triple("com.netflix.mediaclient", "Netflix", 0xFFB71C1C),
        Triple("com.spotify.tv.android", "Spotify", 0xFF1DB954),
        Triple("com.disney.disneyplus", "Disney+", 0xFF1E3A8A),
        Triple("com.amazon.amazonvideo.livingroom", "Prime Video", 0xFF0EA5E9),
        Triple("tv.twitch.android.app", "Twitch", 0xFF9146FF),
        Triple("org.xbmc.kodi", "Kodi", 0xFF0E7490),
        Triple("com.plexapp.android", "Plex", 0xFFE5A00D),
        Triple("com.wbd.stream", "Max", 0xFF3B2A9B),
        Triple("org.videolan.vlc", "VLC", 0xFFF57C00),
        Triple("com.crunchyroll.crunchyroid", "Crunchyroll", 0xFFEA580C),
    )

    val apps: List<TvApp> = catalog.map { (pkg, label, _) -> TvApp(pkg, label, isTvApp = true) }

    val network = NetworkStatus(
        state = NetworkStatus.State.CONNECTED,
        transport = NetworkStatus.Transport.WIFI,
        ssid = "Casa-5G",
        ip = "192.168.1.4",
        gateway = "192.168.1.254",
        dns = listOf("94.140.14.14", "8.8.8.8"),
        signalLevel = 3,
        signalPercent = 62,
        band = "5 GHZ",
        routerLatencyMs = 12,
        internetLatencyMs = 38,
        hasInternet = true,
        mac = "60:35:73:AB:CD:1F",
    )

    fun config(
        wallpaperId: String = LauncherConfig.DEFAULT_WALLPAPER_ID,
        themeId: String = LauncherConfig.DEFAULT_THEME_ID,
    ) = LauncherConfig(wallpaperId = wallpaperId, themeId = themeId)

    /** Same Saturday at 10:30, like the design's Día moment: the day side of the themes that follow [co.jp.tv.themes.shared.DayPhase]. */
    val dayNow: LocalDateTime = now.withHour(10).withMinute(30)

    fun homeState(
        themeId: String = LauncherConfig.DEFAULT_THEME_ID,
        focusedIndex: Int = 1,
        at: LocalDateTime = now,
        weather: Weather? = null,
    ) = HomeState(
        brand = LauncherConfig.DEFAULT_BRAND,
        apps = apps,
        focusedIndex = focusedIndex,
        network = network,
        now = at,
        config = config(themeId = themeId),
        weather = weather,
    )

    /** A reading for Bucaramanga; [night] flips the icon's sun to a moon. */
    fun weather(condition: WeatherCondition, night: Boolean = false, temperatureC: Int = 24, stale: Boolean = false) = Weather(
        condition = condition,
        temperatureC = temperatureC,
        isDay = !night,
        updatedAt = now,
        placeName = "Bucaramanga",
        stale = stale,
    )

    val actions = object : HomeActions {
        override fun launch(app: TvApp) = Unit
        override fun focus(index: Int) = Unit
        override fun openNetwork() = Unit
        override fun openSettings() = Unit
        override fun openThemes() = Unit
        override fun openCustomize() = Unit
        override fun openEditApps(focus: TvApp?) = Unit
    }

    /** Colored banner placeholder with the app's first letter, instead of real PackageManager art. */
    val icons = AppIcons { app ->
        val color = catalog.firstOrNull { it.first == app.packageName }?.third ?: 0xFF444444
        val bitmap = ImageBitmap(320, 180)
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
        canvas.nativeCanvas.drawColor(Color(color).toArgb())
        val paint = Paint().apply {
            this.color = android.graphics.Color.WHITE
            textSize = 96f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.nativeCanvas.drawText(app.label.take(1), 160f, 125f, paint)
        bitmap
    }
}
