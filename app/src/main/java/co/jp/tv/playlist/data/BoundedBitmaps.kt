package co.jp.tv.playlist.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/** Decodes pictures no bigger than the screen needs, so a 20 MP photo never costs 80 MB on a 2 GB box. */
object BoundedBitmaps {
    /** The picture at [path] sampled down by a power of two while it stays at least [maxWidth] x [maxHeight]; null when unreadable. */
    fun decode(path: String, maxWidth: Int, maxHeight: Int): Bitmap? =
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                null
            } else {
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxWidth, maxHeight)
                }
                BitmapFactory.decodeFile(path, options)
            }
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }

    /** Largest power of two that keeps the image at least [maxWidth] x [maxHeight]. */
    fun sampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= maxWidth && height / (sample * 2) >= maxHeight) sample *= 2
        return sample
    }
}
