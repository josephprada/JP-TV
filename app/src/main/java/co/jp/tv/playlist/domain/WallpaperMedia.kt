package co.jp.tv.playlist.domain

/** What the playlist wallpapers play: the resolved files of both kinds. [loaded] is false until the first scan ends. */
data class WallpaperMedia(
    val video: PlaylistSelection = PlaylistSelection.Empty,
    val image: PlaylistSelection = PlaylistSelection.Empty,
    val loaded: Boolean = false,
) {
    companion object {
        val Empty = WallpaperMedia()

        fun of(library: MediaLibraryState, videoSettings: PlaylistSettings, imageSettings: PlaylistSettings) = WallpaperMedia(
            video = PlaylistSelection.of(library.videos, videoSettings),
            image = PlaylistSelection.of(library.images, imageSettings),
            loaded = library.loaded,
        )
    }
}
