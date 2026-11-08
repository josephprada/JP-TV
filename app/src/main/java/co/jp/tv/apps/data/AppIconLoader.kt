package co.jp.tv.apps.data

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.AppIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads app icons lazily for the UI. The rail uses square tiles, so this loads the launcher
 * icon (activity icon, falling back to the application icon); banners are not needed there.
 */
class AppIconLoader(context: Context, private val sizePx: Int = 128) : AppIcons {
    private val pm: PackageManager = context.packageManager
    private val cache = LruCache<String, ImageBitmap>(64)

    override suspend fun load(app: TvApp): ImageBitmap? {
        cache.get(app.packageName)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                val intent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
                    ?: pm.getLaunchIntentForPackage(app.packageName)
                val drawable = intent?.let { pm.getActivityIcon(it) }
                    ?: pm.getApplicationIcon(app.packageName)
                drawable.toBitmap(sizePx, sizePx).asImageBitmap()
            }.getOrNull()?.also { cache.put(app.packageName, it) }
        }
    }
}
