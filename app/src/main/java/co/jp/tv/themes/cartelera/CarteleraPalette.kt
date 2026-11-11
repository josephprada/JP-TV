package co.jp.tv.themes.cartelera

import androidx.compose.ui.graphics.Color

/** A poster's sunburst colors and the color of its big initial. */
class PosterTint(val rayA: Color, val rayB: Color, val ink: Color)

object CarteleraPalette {
    val Background = Color(0xFF1C0A0C)
    val VelvetA = Color(0xFF240C0F)
    val VelvetB = Color(0xFF3A1418)
    val VignetteClear = Color(0x001C0A0C)
    val VignetteEdge = Color(0xCC0E0406)
    val Gold = Color(0xFFF6C453)
    val GoldSoft = Color(0x80F6C453)
    val MarqueeBg = Color(0xFF2A0F12)
    val Cream = Color(0xFFFFF4D6)
    val BulbOff = Color(0xFF5A3A10)
    val BulbOn = Color(0xFFFFE08A)
    val BulbHalo = Color(0x4DFFD66E)
    val TitleGlow = Color(0x73FFD66E)
    val ConeTop = Color(0x5CFFE6A0)
    val ConeBottom = Color(0x0DFFE6A0)
    val FloorGlow = Color(0x61FFD66E)
    val FloorGlowClear = Color(0x00FFD66E)
    val FrameDim = Color(0xFF7A5518)
    val FrameGlow = Color(0x24FFD66E)
    val FaceBottom = Color(0xFF12050A)
    val ArrowBg = Color(0xD92A0F12)
    val InitialShadow = Color(0x4D000000)

    /** Placeholder cover art, cycled by app position. */
    val PosterTints = listOf(
        PosterTint(Color(0xFF7A2FA0), Color(0xFF4D1A68), Cream),
        PosterTint(Color(0xFFC0272D), Color(0xFF85141A), Cream),
        PosterTint(Color(0xFFB3121A), Color(0xFF750B11), Cream),
        PosterTint(Color(0xFF1F9D4D), Color(0xFF126A31), Cream),
        PosterTint(Color(0xFF1D6FB8), Color(0xFF124A7E), Cream),
        PosterTint(Color(0xFF2A47B5), Color(0xFF1A2C80), Cream),
        PosterTint(Color(0xFFF0A24A), Color(0xFFD9741A), Color(0xFF2A1400)),
        PosterTint(Color(0xFFF2C744), Color(0xFFD19A12), Color(0xFF2A1F00)),
        PosterTint(Color(0xFF5A5F6B), Color(0xFF3A3E48), Cream),
        PosterTint(Color(0xFF16897F), Color(0xFF0E5F58), Cream),
    )
}
