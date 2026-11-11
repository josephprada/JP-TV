package co.jp.tv.themes.pixel

import androidx.compose.ui.graphics.Color

object PixelPalette {
    // Backdrop
    val BackgroundTop = Color(0xFF1A1650)
    val BackgroundMid = Color(0xFF2B1F78)
    val BackgroundBottom = Color(0xFF0C0D2A)
    val GlowCore = Color(0x59786EFF)
    val GlowClear = Color(0x00786EFF)

    // Grid floor
    val FloorLine = Color(0x998C82FF)
    val FloorFadeTop = Color(0xFF1F1A63)
    val FloorFadeTopClear = Color(0x001F1A63)
    val FloorFadeBottomClear = Color(0x000C0D2A)
    val FloorFadeBottom = Color(0xBF0C0D2A)

    // Brand letters, shapes and arrows share the console's four button colors
    val Red = Color(0xFFFF4B4B)
    val RedDark = Color(0xFFA31D1D)
    val Green = Color(0xFF3FD35F)
    val GreenDark = Color(0xFF13803A)
    val Blue = Color(0xFF4D85FF)
    val BlueDark = Color(0xFF1B2F9A)
    val Gold = Color(0xFFFDC220)
    val GoldDark = Color(0xFFB8870A)
    val White = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1A1650)

    // Text
    val ShadowBrand = Color(0x66000000)
    val ShadowClock = Color(0x59000000)
    val ShadowName = Color(0x73000000)
    val SubtleWhite = Color(0xD9FFFFFF)

    // Top-right controls
    val Chip = Color(0x8C0C0D2A)
    val ChipBorder = Color(0x33FFFFFF)

    // Cartridge
    val BodyTop = Color(0xFFA3A6AD)
    val BodyBottom = Color(0xFF82858C)
    val Pin = Color(0xFF4B4D53)
    val StripeDark = Color(0x4D000000)
    val StripeLight = Color(0x33FFFFFF)
    val SheenStart = Color(0x4DFFFFFF)
    val SheenEnd = Color(0x00FFFFFF)
    val LabelRing = Color(0xE6FFFFFF)
    val LabelShade = Color(0x1F000000)
    val DimOverlay = Color(0x33000000)
    val DropShadowFocused = Color(0x8C000000)
    val DropShadowIdle = Color(0x73000000)

    /** Label colors, cycled by position: top, bottom, text. */
    internal val Tints = listOf(
        PixelTint(Color(0xFF8B5CF6), Color(0xFF5B21B6), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFFDC2626), Color(0xFF991B1B), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFFC81E1E), Color(0xFF7F1D1D), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFF4ADE80), Color(0xFF16A34A), Color(0xFF052E16)),
        PixelTint(Color(0xFF3B82F6), Color(0xFF1E3A8A), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFF2A5BD7), Color(0xFF1B2A6B), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFFFDBA74), Color(0xFFF97316), Color(0xFF2B1000)),
        PixelTint(Color(0xFFFDE047), Color(0xFFEAB308), Color(0xFF2A1F00)),
        PixelTint(Color(0xFF4B5563), Color(0xFF1F2937), Color(0xFFFFFFFF)),
        PixelTint(Color(0xFF0D9488), Color(0xFF115E59), Color(0xFFFFFFFF)),
    )
}
