package co.jp.tv.playlist.domain

enum class MediaKind { VIDEO, IMAGE }

/** Where a file was found. Decides the order of the list and what the screensaver may use. */
enum class MediaSource {
    /** The single files of before playlists: `/sdcard/JPTV/loop.mp4` and `/sdcard/JPTV/fondo.jpg`. */
    LEGACY,

    /** `/sdcard/JPTV/fondos/`. */
    FONDOS,

    /** `/sdcard/JPTV/protector/`: photos meant only for the screensaver. */
    PROTECTOR,

    /** `JPTV/fondos` (or `JPTV/protector`) on removable storage. */
    USB,
}

/** One playable file. [id] is the absolute path and is what the config stores. */
data class MediaItem(
    val id: String,
    val name: String,
    val kind: MediaKind,
    val source: MediaSource = MediaSource.FONDOS,
)
