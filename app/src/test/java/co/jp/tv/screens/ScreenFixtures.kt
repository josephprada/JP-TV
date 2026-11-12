package co.jp.tv.screens

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.AppIcons
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.MediaLibraryState
import co.jp.tv.playlist.domain.MediaSource
import co.jp.tv.playlist.domain.PlaylistSelection
import co.jp.tv.playlist.domain.WallpaperMedia
import co.jp.tv.playlist.ui.ImageSource
import co.jp.tv.playlist.ui.MediaThumbnails
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.screensaver.domain.ScreensaverState
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

    // --- Wallpaper playlists and screensaver ---

    private fun file(path: String, kind: MediaKind, source: MediaSource = MediaSource.FONDOS) =
        MediaItem(path, path.substringAfterLast('/'), kind, source)

    private val videoFiles = listOf(
        file("/sdcard/JPTV/loop.mp4", MediaKind.VIDEO, MediaSource.LEGACY),
        file("/sdcard/JPTV/fondos/aurora.mp4", MediaKind.VIDEO),
        file("/sdcard/JPTV/fondos/ciudad-noche.webm", MediaKind.VIDEO),
        file("/sdcard/JPTV/fondos/lluvia.mkv", MediaKind.VIDEO),
        file("/storage/ABCD-1234/JPTV/fondos/playa.mp4", MediaKind.VIDEO, MediaSource.USB),
    )

    private val imageFiles = listOf(
        file("/sdcard/JPTV/fondo.jpg", MediaKind.IMAGE, MediaSource.LEGACY),
        file("/sdcard/JPTV/fondos/montana.jpg", MediaKind.IMAGE),
        file("/sdcard/JPTV/fondos/atardecer.png", MediaKind.IMAGE),
        file("/sdcard/JPTV/fondos/bosque.webp", MediaKind.IMAGE),
        file("/storage/ABCD-1234/JPTV/fondos/ciudad.jpg", MediaKind.IMAGE, MediaSource.USB),
    )

    val library = MediaLibraryState(videoFiles + imageFiles, loaded = true)

    /** A made-up landscape (sky, sun, two hills) tinted from the file name: stands in for the real thumbnail or photo. */
    private fun scene(item: MediaItem, width: Int, height: Int): ImageBitmap {
        val hue = (item.name.hashCode().and(0x7fffffff) % 360).toFloat()
        val sky = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.55f, 0.85f))
        val dusk = android.graphics.Color.HSVToColor(floatArrayOf((hue + 40f) % 360f, 0.7f, 0.35f))
        val hill = android.graphics.Color.HSVToColor(floatArrayOf((hue + 160f) % 360f, 0.5f, 0.22f))
        val bitmap = ImageBitmap(width, height)
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap).nativeCanvas
        val paint = Paint().apply { isAntiAlias = true }
        paint.shader = android.graphics.LinearGradient(0f, 0f, 0f, height.toFloat(), sky, dusk, android.graphics.Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = android.graphics.Color.argb(230, 255, 240, 200)
        canvas.drawCircle(width * 0.7f, height * 0.38f, height * 0.12f, paint)
        paint.color = hill
        canvas.drawCircle(width * 0.25f, height * 1.15f, height * 0.62f, paint)
        paint.color = android.graphics.Color.argb(255, 8, 8, 12)
        canvas.drawCircle(width * 0.8f, height * 1.3f, height * 0.7f, paint)
        return bitmap
    }

    val thumbnails = MediaThumbnails { item -> scene(item, 192, 108) }

    val imageSource = ImageSource { item, _, _ -> scene(item, 960, 540) }

    fun saverState(
        themeId: String = LauncherConfig.DEFAULT_THEME_ID,
        screensaver: ScreensaverSettings = ScreensaverSettings.Default,
        weather: Weather? = weather(WeatherCondition.Clear),
        photos: List<MediaItem> = imageFiles.filter { it.source != MediaSource.LEGACY },
        at: LocalDateTime = now,
    ): ScreensaverState {
        val config = LauncherConfig.Default.copy(themeId = themeId, screensaver = screensaver)
        return ScreensaverState(
            now = at,
            config = config,
            weather = weather,
            photos = photos,
            videos = PlaylistSelection.of(library.videos, config.videoPlaylist),
            wallpaperMedia = WallpaperMedia.of(library, config.videoPlaylist, config.imagePlaylist),
            mediaLoaded = true,
        )
    }
}
