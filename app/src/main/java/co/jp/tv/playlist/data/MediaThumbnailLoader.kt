package co.jp.tv.playlist.data

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.ui.MediaThumbnails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Small previews for the Customize chips: a downsampled picture, or the first frame of a video read with
 * [MediaMetadataRetriever]. Off the main thread, one at a time (a video frame is briefly full size), and
 * the last few are kept so scrolling back is free.
 */
class MediaThumbnailLoader(
    private val widthPx: Int = 192,
    private val heightPx: Int = 108,
) : MediaThumbnails {
    private val cache = LruCache<String, ImageBitmap>(CACHE_ENTRIES)
    private val one = Semaphore(1)

    override suspend fun load(item: MediaItem): ImageBitmap? {
        cache.get(item.id)?.let { return it }
        return one.withPermit {
            cache.get(item.id) ?: withContext(Dispatchers.IO) {
                runCatching {
                    when (item.kind) {
                        MediaKind.IMAGE -> BoundedBitmaps.decode(item.id, widthPx, heightPx)
                        MediaKind.VIDEO -> firstFrame(item.id)
                    }
                }.getOrNull()
            }?.let { fit(it).asImageBitmap() }?.also { cache.put(item.id, it) }
        }
    }

    private fun firstFrame(path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            retriever.getFrameAtTime(FRAME_AT_US, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    /** Scales [source] down to fit the thumbnail box and frees the original when a copy was made. */
    private fun fit(source: Bitmap): Bitmap {
        val scale = minOf(widthPx.toFloat() / source.width, heightPx.toFloat() / source.height, 1f)
        if (scale >= 1f) return source
        val scaled = Bitmap.createScaledBitmap(source, (source.width * scale).toInt().coerceAtLeast(1), (source.height * scale).toInt().coerceAtLeast(1), true)
        if (scaled !== source) source.recycle()
        return scaled
    }

    private companion object {
        const val CACHE_ENTRIES = 32
        const val FRAME_AT_US = 1_000_000L
    }
}
