package co.jp.tv.themes.cyberpunk

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import co.jp.tv.core.design.bakedLayer

/**
 * Fog over the towers and a dark veil under a gloomy sky, between the skyline and the neon signs so the
 * apps stay crisp. Baked once per phase and [sky]; nothing is drawn when the sky is neither foggy nor murky.
 */
@Composable
internal fun CyberpunkHaze(phase: CyberpunkPalette.Phase, sky: CyberpunkSky) {
    if (sky.fog <= 0f && sky.murk <= 0f) return
    val fogColor = if (phase === CyberpunkPalette.Night) CyberpunkPalette.FogNight else CyberpunkPalette.FogDay
    Spacer(
        Modifier
            .fillMaxSize()
            .bakedLayer {
                inDesignPx {
                    val full = Size(DESIGN_WIDTH, DESIGN_HEIGHT)
                    if (sky.murk > 0f) {
                        drawRect(
                            Brush.verticalGradient(
                                0f to CyberpunkPalette.Murk.copy(alpha = sky.murk),
                                0.6f to CyberpunkPalette.Murk.copy(alpha = 0f),
                                startY = 0f,
                                endY = DESIGN_HEIGHT,
                            ),
                            Offset.Zero,
                            full,
                        )
                    }
                    if (sky.fog > 0f) {
                        drawRect(
                            Brush.verticalGradient(
                                0f to fogColor.copy(alpha = 0f),
                                0.3f to fogColor.copy(alpha = sky.fog * 0.7f),
                                0.7f to fogColor.copy(alpha = sky.fog * 0.9f),
                                1f to fogColor.copy(alpha = sky.fog * 0.35f),
                                startY = 0f,
                                endY = DESIGN_HEIGHT,
                            ),
                            Offset.Zero,
                            full,
                        )
                    }
                }
            },
    )
}
