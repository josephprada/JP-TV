package co.jp.tv.core.design

import android.content.Context
import androidx.compose.ui.text.font.createFontFamilyResolver

/**
 * Decodes the fonts the home draws on its first frame (body, mono, the chosen title font) off the main thread.
 * Compose resolves them through process-wide caches, so the first text measure on the main thread then hits the cache
 * instead of parsing the TTFs (variable-weight Geist alone is three instances).
 */
object FontPreloader {
    suspend fun preload(context: Context, titleFontId: String) {
        val resolver = createFontFamilyResolver(context.applicationContext)
        listOf(JpFonts.Body, JpFonts.Mono, JpFonts.title(titleFontId)).forEach { family ->
            runCatching { resolver.preload(family) }
        }
    }
}
