package co.jp.tv.wallpaper

import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.wallpaper.embers.EmbersWallpaper
import co.jp.tv.wallpaper.galaxy.GalaxyWallpaper

/** Maps wallpaper ids from [LauncherConfig.WALLPAPER_IDS] to implementations. */
object WallpaperRegistry {
    val all: List<Wallpaper> = listOf(GalaxyWallpaper, EmbersWallpaper)

    val default: Wallpaper get() = all.first { it.id == LauncherConfig.DEFAULT_WALLPAPER_ID }

    fun byId(id: String): Wallpaper = all.firstOrNull { it.id == id } ?: default
}
