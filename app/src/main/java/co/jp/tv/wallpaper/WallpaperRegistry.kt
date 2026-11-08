package co.jp.tv.wallpaper

import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.wallpaper.embers.EmbersWallpaper
import co.jp.tv.wallpaper.galaxy.GalaxyWallpaper
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper

/** Maps wallpaper ids from [LauncherConfig.WALLPAPER_IDS] to implementations. */
object WallpaperRegistry {
    val all: List<Wallpaper> = listOf(
        EmbersWallpaper,
        GalaxyWallpaper,
        VideoWallpaper,
        StaticImageWallpaper,
    )

    val default: Wallpaper get() = all.first { it.id == LauncherConfig.DEFAULT_WALLPAPER_ID }

    fun byId(id: String): Wallpaper = all.firstOrNull { it.id == id } ?: default
}
