package co.jp.tv.preferences.domain

/** User-facing configuration. Brand, accent and wallpaper are placeholders until phase 5 persists them. */
data class LauncherConfig(
    val brand: String = "JP-TV",
    /** ARGB */
    val accent: Long = 0xFFE63329,
    val wallpaperId: String = DEFAULT_WALLPAPER_ID,
    /** Package names of every known app, in the user's order (hidden ones keep their slot). */
    val appOrder: List<String> = emptyList(),
    /** Package names kept off the home rail. */
    val hiddenApps: Set<String> = emptySet(),
) {
    companion object {
        const val DEFAULT_WALLPAPER_ID = "galaxy"

        /** Ids of the available wallpapers; add new ones here and in WallpaperRegistry. */
        val WALLPAPER_IDS = listOf("galaxy", "embers")

        val Default = LauncherConfig()
    }
}
