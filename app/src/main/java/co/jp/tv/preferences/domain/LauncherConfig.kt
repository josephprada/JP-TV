package co.jp.tv.preferences.domain

/** User-facing configuration. Persistence arrives in phase 5; defaults apply until then. */
data class LauncherConfig(
    val brand: String = "JP-TV",
    /** ARGB */
    val accent: Long = 0xFFE63329,
    val wallpaperId: String = DEFAULT_WALLPAPER_ID,
) {
    companion object {
        const val DEFAULT_WALLPAPER_ID = "galaxy"

        /** Ids of the available wallpapers; add new ones here and in WallpaperRegistry. */
        val WALLPAPER_IDS = listOf("galaxy", "embers")

        val Default = LauncherConfig()
    }
}
