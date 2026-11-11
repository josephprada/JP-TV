package co.jp.tv.themes.zen

import androidx.compose.ui.graphics.Color

/** An app icon tile: gradient base color, its glow when focused, and the initials color. */
class ZenTint(val color: Color, val ink: Color) {
    /** Bottom end of the 160 degree gradient ("color cc"). */
    val fade: Color = color.copy(alpha = 0.8f)
    val glow: Color = color.copy(alpha = 0.4f)
    val glowClear: Color = color.copy(alpha = 0f)
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

    /** Placeholder icon colors, cycled by app position. */
    val Tints = listOf(
        ZenTint(Color(0xFF7B5CFF), Color(0xFFFFFFFF)),
        ZenTint(Color(0xFFFF1F3D), Color(0xFFFFFFFF)),
        ZenTint(Color(0xFFD81F26), Color(0xFFFFFFFF)),
        ZenTint(Color(0xFF1ED760), Color(0xFF06210F)),
        ZenTint(Color(0xFF2BB5EE), Color(0xFF041A26)),
        ZenTint(Color(0xFF1F46D6), Color(0xFFFFFFFF)),
        ZenTint(Color(0xFFF47521), Color(0xFF1F0D02)),
        ZenTint(Color(0xFFE5A00D), Color(0xFF231800)),
        ZenTint(Color(0xFF636366), Color(0xFFFFFFFF)),
        ZenTint(Color(0xFF48484A), Color(0xFFFFFFFF)),
    )
}
