package co.jp.tv.wallpaper.image

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.design.JpColors
import co.jp.tv.wallpaper.Wallpaper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Still-image wallpaper. Prefers `/sdcard/JPTV/fondo.jpg` (PLAN path); falls back to the
 * bundled asset so the TV still shows something when storage permission is missing.
 */
object StaticImageWallpaper : Wallpaper {
    override val id = ID
    override val displayName = "Imagen fija"

    const val ID = "image"
    const val EXTERNAL_FILE_NAME = "fondo.jpg"
    private const val ASSET_PATH = "wallpapers/fondo.jpg"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val context = LocalContext.current
        val bitmap by produceState<ImageBitmap?>(initialValue = null, context) {
            value = withContext(Dispatchers.IO) { load(context) }
        }
        Box(modifier.fillMaxSize().background(JpColors.Ink)) {
            bitmap?.let { image ->
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }

    fun externalFile(): File =
        File(Environment.getExternalStorageDirectory(), "JPTV/$EXTERNAL_FILE_NAME")

    private fun load(context: Context): ImageBitmap? {
        decodeFile(externalFile())?.let { return it }
        return decodeAsset(context)
    }

    private fun decodeFile(file: File): ImageBitmap? {
        if (!file.isFile) return null
        return runCatching {
            BitmapFactory.Options().run {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                BitmapFactory.decodeFile(file.absolutePath, this)?.asImageBitmap()
            }
        }.getOrNull()
    }

    private fun decodeAsset(context: Context): ImageBitmap? = runCatching {
        context.assets.open(ASSET_PATH).use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        }
    }.getOrNull()
}
