package co.jp.tv.preferences.domain

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
) {
    companion object {
        const val DEFAULT_BRAND = "JP-TV"
        const val DEFAULT_WALLPAPER_ID = "galaxy"
        const val DEFAULT_THEME_ID = "jptv"

        /** Ids of the available wallpapers; add new ones here and in WallpaperRegistry. */
        val WALLPAPER_IDS = listOf("embers", "galaxy", "video", "image")

        val BRAND_OPTIONS = listOf("JP-TV", "JP", "Casa")

        val Default = LauncherConfig()
    }
}
