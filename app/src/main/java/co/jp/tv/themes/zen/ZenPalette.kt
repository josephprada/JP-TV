package co.jp.tv.themes.zen

import androidx.compose.ui.graphics.Color

/** The soft halo behind the focused app icon, built from the user's accent. There is no tile: the icon is drawn alone. */
class ZenGlow(accent: Color) {
    val glow: Color = accent.copy(alpha = 0.4f)
    val glowClear: Color = accent.copy(alpha = 0f)
}

object ZenPalette {
    val Paper = Color(0xFFF5F5F7)
    val PaperSoft = Color(0xC7F5F5F7)
    val PaperMuted = Color(0xB3F5F5F7)
    val PaperFaint = Color(0x99F5F5F7)
    val FocusRing = Color(0xFFFFFFFF)

    /** Legibility scrim on the left, over the wallpaper. */
    val ScrimStrong = Color(0x9E050507)
    val ScrimMid = Color(0x47050507)
    val ScrimClear = Color(0x00050507)

    val RingSoft = Color(0x1AFFFFFF)
    val RingStrong = Color(0x2EFFFFFF)
}
