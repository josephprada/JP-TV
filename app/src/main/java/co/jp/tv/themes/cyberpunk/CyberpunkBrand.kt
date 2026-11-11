package co.jp.tv.themes.cyberpunk

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em

/** Flickering pink neon brand, top-left (`cy-flicker` 8s). */
@Composable
internal fun CyberpunkBrand(brand: String, time: State<Float>, modifier: Modifier = Modifier) {
    CyberpunkGlowText(
        brand,
        TextStyle(
            color = CyberpunkPalette.Brand,
            fontSize = 40.spx,
            fontWeight = FontWeight.Bold,
            fontFamily = CyberpunkFonts.Display,
            letterSpacing = 0.06.em,
        ),
        glows = listOf(
            CyberpunkPalette.Pink to 6f,
            CyberpunkPalette.Pink to 20f,
            CyberpunkPalette.Pink.copy(alpha = 0.7f) to 48f,
        ),
        modifier = modifier.graphicsLayer {
            alpha = CyberpunkKeyframes.progress(time.value, 8f, 0f)?.let(CyberpunkKeyframes::flicker) ?: 1f
        },
    )
}
