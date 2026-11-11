package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Text with the artboard's stacked `text-shadow` glows. Each glow is (color, blur in design px);
 * Compose draws one shadow per text, so the text is drawn once per glow, on top of each other.
 */
@Composable
internal fun CyberpunkGlowText(
    text: String,
    style: TextStyle,
    glows: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
) {
    val unit = with(LocalDensity.current) { 0.5.dp.toPx() }
    Box(modifier) {
        if (glows.isEmpty()) {
            BasicText(text, style = style, maxLines = 1, softWrap = false)
        } else {
            glows.forEach { (color, blur) ->
                BasicText(
                    text,
                    style = style.copy(shadow = Shadow(color = color, offset = Offset.Zero, blurRadius = blur * unit)),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
