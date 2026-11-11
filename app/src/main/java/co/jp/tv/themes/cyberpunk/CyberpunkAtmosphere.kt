package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.scale

/** Vignette ellipse of the artboard: 75% x 70% of the frame. */
private const val VIGNETTE_RX = DESIGN_WIDTH * 0.75f
private const val VIGNETTE_RY = DESIGN_HEIGHT * 0.7f

/** Scanlines, vignette and the daytime top shade, over the whole scene. */
@Composable
internal fun CyberpunkAtmosphere(phase: CyberpunkPalette.Phase) {
    val vignette = remember(phase) {
        Brush.radialGradient(
            0.55f to CyberpunkPalette.Ink.copy(alpha = 0f),
            1f to CyberpunkPalette.Ink.copy(alpha = phase.vignetteAlpha),
            center = Offset(DESIGN_WIDTH / 2f, DESIGN_HEIGHT / 2f),
            radius = VIGNETTE_RX,
        )
    }
    val topShade = remember {
        Brush.verticalGradient(
            listOf(CyberpunkPalette.TopShade, CyberpunkPalette.TopShade.copy(alpha = 0f)),
            startY = 0f,
            endY = 240f,
        )
    }
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                inDesignPx {
                    // repeating-linear-gradient(0deg): 1 px line every 4 px, from the bottom.
                    var y = DESIGN_HEIGHT - 1f
                    while (y >= 0f) {
                        drawRect(CyberpunkPalette.Scanline, Offset(0f, y), Size(DESIGN_WIDTH, 1f))
                        y -= 4f
                    }
                    val squash = VIGNETTE_RY / VIGNETTE_RX
                    val tall = DESIGN_HEIGHT / squash
                    scale(scaleX = 1f, scaleY = squash, pivot = Offset(DESIGN_WIDTH / 2f, DESIGN_HEIGHT / 2f)) {
                        drawRect(vignette, Offset(0f, DESIGN_HEIGHT / 2f - tall / 2f), Size(DESIGN_WIDTH, tall))
                    }
                    if (phase.topShadeAlpha > 0f) {
                        drawRect(topShade, Offset.Zero, Size(DESIGN_WIDTH, 240f), alpha = phase.topShadeAlpha)
                    }
                }
            },
    )
}
