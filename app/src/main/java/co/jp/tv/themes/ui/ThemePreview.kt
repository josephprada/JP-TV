package co.jp.tv.themes.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.jp.tv.themes.ui.preview.CarteleraPreview
import co.jp.tv.themes.ui.preview.CyberpunkPreview
import co.jp.tv.themes.ui.preview.GothamPreview
import co.jp.tv.themes.ui.preview.HudPreview
import co.jp.tv.themes.ui.preview.JpTvPreview
import co.jp.tv.themes.ui.preview.LofiPreview
import co.jp.tv.themes.ui.preview.MultiversoPreview
import co.jp.tv.themes.ui.preview.OceanoPreview
import co.jp.tv.themes.ui.preview.PixelPreview
import co.jp.tv.themes.ui.preview.TerminalPreview
import co.jp.tv.themes.ui.preview.ViniloPreview
import co.jp.tv.themes.ui.preview.ZenPreview

/**
 * Thumbnail drawn for a theme card (230 design px tall, rounded 22). Keyed by theme id so a theme
 * registered later shows its art as soon as its id has an entry here; an unknown id gets a plain dark panel.
 */
@Composable
internal fun ThemePreview(themeId: String, modifier: Modifier = Modifier) {
    val frame = modifier
        .fillMaxWidth()
        .height(115.dp)
        .clip(RoundedCornerShape(11.dp))
    when (themeId) {
        "jptv" -> JpTvPreview(frame)
        "pixel" -> PixelPreview(frame)
        "terminal" -> TerminalPreview(frame)
        "cartelera" -> CarteleraPreview(frame)
        "zen" -> ZenPreview(frame)
        "hud" -> HudPreview(frame)
        "vinilo" -> ViniloPreview(frame)
        "lofi" -> LofiPreview(frame)
        "gotham" -> GothamPreview(frame)
        "multiverso" -> MultiversoPreview(frame)
        "cyberpunk" -> CyberpunkPreview(frame)
        "oceano" -> OceanoPreview(frame)
        else -> Canvas(frame) { drawRect(FALLBACK_PANEL) }
    }
}

private val FALLBACK_PANEL = Color(0xFF121214)
