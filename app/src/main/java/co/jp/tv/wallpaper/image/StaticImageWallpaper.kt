package co.jp.tv.wallpaper.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.wallpaper.Wallpaper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Still-image wallpaper from `/sdcard/JPTV/fondo.jpg`. Without a readable file it shows a
 * neutral gradient with a hint, like the video wallpaper's placeholder.
 */
object StaticImageWallpaper : Wallpaper {
    override val id = ID
    override val displayName = "Imagen fija"

    const val ID = "image"
    const val EXTERNAL_FILE_NAME = "fondo.jpg"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val bitmap by produceState<ImageBitmap?>(initialValue = null) {
            value = withContext(Dispatchers.IO) { decodeFile(externalFile()) }
        }
        Box(
            modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(JpColors.Ink, JpColors.GlassFake, JpColors.Ink))),
            contentAlignment = Alignment.Center,
        ) {
            val image = bitmap
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                BasicText(
                    text = stringResource(R.string.wallpaper_image_hint),
                    style = TextStyle(
                        color = JpColors.Faint,
                        fontSize = 13.sp,
                        fontFamily = JpFonts.Mono,
                        letterSpacing = 0.18.em,
                    ),
                )
            }
        }
    }

    fun externalFile(): File =
        File(Environment.getExternalStorageDirectory(), "JPTV/$EXTERNAL_FILE_NAME")

    private const val MAX_WIDTH = 1920
    private const val MAX_HEIGHT = 1080

    private fun decodeFile(file: File): ImageBitmap? {
        if (!file.isFile) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
            }
            BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    /** Largest power of two that keeps the image at least [MAX_WIDTH] x [MAX_HEIGHT]. */
    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= MAX_WIDTH && height / (sample * 2) >= MAX_HEIGHT) sample *= 2
        return sample
    }
}
