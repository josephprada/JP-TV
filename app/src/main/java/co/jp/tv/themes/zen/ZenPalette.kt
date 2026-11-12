package co.jp.tv.themes.zen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** An app icon tile built from the user's accent: gradient base color, its glow when focused, and the initials color. */
class ZenTint(val color: Color, val ink: Color) {
    /** Bottom end of the 160 degree gradient ("color cc"). */
    val fade: Color = color.copy(alpha = 0.8f)
    val glow: Color = color.copy(alpha = 0.4f)
    val glowClear: Color = color.copy(alpha = 0f)

    companion object {
        /** Tile colors for [accent]; the initials flip to dark ink on light accents. */
        fun of(accent: Color): ZenTint =
            ZenTint(accent, if (accent.luminance() > 0.5f) Color(0xFF111114) else Color.White)
    }
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
