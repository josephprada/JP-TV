package co.jp.tv.wallpaper

import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.wallpaper.embers.EmbersWallpaper
import co.jp.tv.wallpaper.galaxy.GalaxyWallpaper
import co.jp.tv.wallpaper.image.StaticImageWallpaper
import co.jp.tv.wallpaper.video.VideoWallpaper

/**
 * Maps wallpaper ids from [LauncherConfig.WALLPAPER_IDS] to implementations.
 * Lazy per entry: the video wallpaper (and with it Media3) is only loaded when it is chosen.
 */
object WallpaperRegistry {
    private class Entry(val id: String, create: () -> Wallpaper) {
        val wallpaper: Wallpaper by lazy(create)
    }

    private val entries = listOf(
        Entry("embers") { EmbersWallpaper },
        Entry("galaxy") { GalaxyWallpaper },
        Entry("video") { VideoWallpaper },
        Entry("image") { StaticImageWallpaper },
    )

    val all: List<Wallpaper> by lazy { entries.map { it.wallpaper } }

    val default: Wallpaper get() = byId(LauncherConfig.DEFAULT_WALLPAPER_ID)

    fun byId(id: String): Wallpaper =
        (entries.firstOrNull { it.id == id } ?: entries.first { it.id == LauncherConfig.DEFAULT_WALLPAPER_ID }).wallpaper
}
