package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts

/** Bottom-left readout of the focused app: "02 / 10" and its name in the app's neon tint. */
@Composable
internal fun CyberpunkSelection(name: String, tint: Color, index: Int, count: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dpx)) {
        CyberpunkGlowText(
            stringResource(R.string.theme_cyberpunk_position, twoDigits(index + 1), twoDigits(count)),
            TextStyle(
                color = tint,
                fontSize = 16.spx,
                fontFamily = JpFonts.ShareTechMono,
                letterSpacing = 0.24.em,
            ),
            glows = listOf(tint.copy(alpha = 0.6f) to 10f),
        )
        CyberpunkGlowText(
            name.uppercase(),
            TextStyle(
                color = CyberpunkPalette.White,
                fontSize = 72.spx,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CyberpunkFonts.Display,
                letterSpacing = 0.03.em,
                lineHeight = 1.em,
            ),
            glows = listOf(tint to 6f, tint.copy(alpha = 0.8f) to 24f, tint.copy(alpha = 0.5f) to 60f),
        )
    }
}

/** JS `padStart(2, '0')`. */
private fun twoDigits(value: Int): String = value.toString().padStart(2, '0')
