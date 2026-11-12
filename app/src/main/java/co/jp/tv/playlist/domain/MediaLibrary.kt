package co.jp.tv.playlist.domain

import kotlinx.coroutines.flow.StateFlow

/** A file seen by the scanner, before it is classified. */
data class ScannedFile(val path: String, val source: MediaSource)

/** Turns what the scanner saw into the ordered list of [MediaItem]s. Pure. */
object MediaListing {
    private val sourceOrder = listOf(MediaSource.LEGACY, MediaSource.FONDOS, MediaSource.PROTECTOR, MediaSource.USB)

    fun build(files: List<ScannedFile>): List<MediaItem> =
        files
            .distinctBy { it.path }
            .mapNotNull { file ->
                val name = file.path.substringAfterLast('/')
                val kind = MediaFileKinds.kindOf(name) ?: return@mapNotNull null
                // Photos for the screensaver are pictures only.
                if (file.source == MediaSource.PROTECTOR && kind != MediaKind.IMAGE) return@mapNotNull null
                MediaItem(file.path, name, kind, file.source)
            }
            .sortedWith(compareBy<MediaItem> { sourceOrder.indexOf(it.source) }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
}

/** Everything the last scan found. [loaded] stays false until the first scan has finished. */
data class MediaLibraryState(val items: List<MediaItem>, val loaded: Boolean) {
    /** Candidates for the video wallpaper. */
    val videos: List<MediaItem> get() = items.filter { it.kind == MediaKind.VIDEO && it.source != MediaSource.PROTECTOR }

    /** Candidates for the still-image wallpaper. */
    val images: List<MediaItem> get() = items.filter { it.kind == MediaKind.IMAGE && it.source != MediaSource.PROTECTOR }

    /** Photos kept for the screensaver only. */
    val protector: List<MediaItem> get() = items.filter { it.source == MediaSource.PROTECTOR }

    fun of(kind: MediaKind): List<MediaItem> = if (kind == MediaKind.VIDEO) videos else images

    companion object {
        val Empty = MediaLibraryState(emptyList(), loaded = false)
    }
}

/** Port: finds the wallpaper media on the device. */
interface MediaCatalog {
    val library: StateFlow<MediaLibraryState>

    /** Looks at the storage again; call it off the main thread's critical path (it suspends). */
    suspend fun refresh()
}
