package co.jp.tv.preferences.domain

/** User-facing configuration. Persistence arrives in phase 5; defaults apply until then. */
data class LauncherConfig(
    val brand: String = "JP-TV",
    /** ARGB */
    val accent: Long = 0xFFE63329,
    val wallpaperId: String = "galaxy",
) {
    companion object {
        val Default = LauncherConfig()
    }
}
