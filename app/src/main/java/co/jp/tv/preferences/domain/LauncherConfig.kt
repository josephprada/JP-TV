package co.jp.tv.preferences.domain

import co.jp.tv.core.sound.SoundVolume
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.weather.domain.WeatherPlace

/** User-facing launcher configuration persisted in DataStore. */
data class LauncherConfig(
    val brand: String = DEFAULT_BRAND,
    /** ARGB */
    val accent: Long = AccentPresets.DEFAULT.argb,
    val titleFontId: String = TitleFonts.DEFAULT_ID,
    val wallpaperId: String = DEFAULT_WALLPAPER_ID,
    val themeId: String = DEFAULT_THEME_ID,
    /** Package names of every known app, in the user's order (hidden ones keep their slot). */
    val appOrder: List<String> = emptyList(),
    /** Package names kept off the home rail. */
    val hiddenApps: Set<String> = emptySet(),
    /** 24-hour (default) or 12-hour AM/PM clock in every theme. */
    val hourFormat: HourFormat = HourFormat.H24,
    /** 0..[MAX_DIM] percent of black laid over the wallpaper. */
    val wallpaperDim: Int = 0,
    /** Idle the wallpaper (and its video) while another app is in front. */
    val pauseWallpaperInApps: Boolean = true,
    /** Where the weather is read for. */
    val weatherPlace: WeatherPlace = WeatherPlace.Default,
    /** Short interface sounds in the theme's own voice. */
    val soundsEnabled: Boolean = true,
    val soundVolume: SoundVolume = SoundVolume.MEDIUM,
    /** Opt-in: take the screen from the stock launcher on devices whose firmware ignores the preferred home. */
    val homeOverrideEnabled: Boolean = false,
    /** Files, rotation and order of the "Mi video" wallpaper. */
    val videoPlaylist: PlaylistSettings = PlaylistSettings.Default,
    /** Files, rotation and order of the "Imagen fija" wallpaper. */
    val imagePlaylist: PlaylistSettings = PlaylistSettings.Default,
    val screensaver: ScreensaverSettings = ScreensaverSettings.Default,
) {
    fun playlist(kind: MediaKind): PlaylistSettings = if (kind == MediaKind.VIDEO) videoPlaylist else imagePlaylist

    fun withPlaylist(kind: MediaKind, settings: PlaylistSettings): LauncherConfig =
        if (kind == MediaKind.VIDEO) copy(videoPlaylist = settings) else copy(imagePlaylist = settings)

    companion object {
        const val DEFAULT_BRAND = "JP-TV"
        const val DEFAULT_WALLPAPER_ID = "galaxy"
        const val DEFAULT_THEME_ID = "jptv"

        /** Ids of the available wallpapers; add new ones here and in WallpaperRegistry. */
        val WALLPAPER_IDS = listOf("embers", "galaxy", "video", "image")

        const val MAX_BRAND = 14

        /** What the name field may save: trimmed, at most [MAX_BRAND] characters, never empty. */
        fun sanitizeBrand(raw: String): String = raw.trim().take(MAX_BRAND).trim().ifEmpty { DEFAULT_BRAND }

        const val MAX_DIM = 80
        private const val DIM_STEP = 10

        /** One D-pad step of the dim setting, snapped to multiples of [DIM_STEP] and clamped to 0..[MAX_DIM]. */
        fun stepDim(current: Int, direction: Int): Int {
            val next = if (direction > 0) (current / DIM_STEP + 1) * DIM_STEP else (current + DIM_STEP - 1) / DIM_STEP * DIM_STEP - DIM_STEP
            return next.coerceIn(0, MAX_DIM)
        }

        val Default = LauncherConfig()
    }
}
