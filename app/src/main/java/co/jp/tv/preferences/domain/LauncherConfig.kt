package co.jp.tv.preferences.domain

import co.jp.tv.core.util.ClockFormat.HourFormat

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
) {
    companion object {
        const val DEFAULT_BRAND = "JP-TV"
        const val DEFAULT_WALLPAPER_ID = "galaxy"
        const val DEFAULT_THEME_ID = "jptv"

        /** Ids of the available wallpapers; add new ones here and in WallpaperRegistry. */
        val WALLPAPER_IDS = listOf("embers", "galaxy", "video", "image")

        const val MAX_DIM = 80
        private const val DIM_STEP = 10

        /** One D-pad step of the dim setting, snapped to multiples of [DIM_STEP] and clamped to 0..[MAX_DIM]. */
        fun stepDim(current: Int, direction: Int): Int {
            val next = if (direction > 0) (current / DIM_STEP + 1) * DIM_STEP else (current + DIM_STEP - 1) / DIM_STEP * DIM_STEP - DIM_STEP
            return next.coerceIn(0, MAX_DIM)
        }

        val BRAND_OPTIONS = listOf("JP-TV", "JP", "Casa")

        val Default = LauncherConfig()
    }
}
