package co.jp.tv.themes.vinilo

import androidx.compose.ui.graphics.Color

object ViniloPalette {
    val Background = Color(0xFF24160F)
    val BackgroundGlow = Color(0xFF3A2418)
    val BackgroundEdge = Color(0xFF170E09)
    val Orange = Color(0xFFE8743B)
    val Gold = Color(0xFFE0B23A)
    val Teal = Color(0xFF2F7F7A)
    val Cream = Color(0xFFF3E3C3)
    val CreamDim = Color(0xB3F3E3C3)
    val Deck = Color(0xFF3A2418)
    val DeckTop = Color(0xFF5A3A26)
    val Vinyl = Color(0xFF1A1A1A)
    val Groove = Color(0xFF111111)
    val GrooveLine = Color(0xFF1D1D1D)
    val Sheen = Color(0x14FFFFFF)
    val SheenClear = Color(0x00FFFFFF)
    val ArmMetal = Color(0xFFC9B48A)
    val ArmRod = Color(0xFFD9C9A3)
    val ShelfTop = Color(0xFF8A5A36)
    val ShelfMid = Color(0xFF6B4226)
    val ShelfBottom = Color(0xFF4A2C19)

    /** Sleeve and record-label colors, cycled by app position. */
    val LabelColors = listOf(
        Orange,
        Color(0xFFC8423A),
        Teal,
        Gold,
        Color(0xFF6F8FBF),
    )

    /** Equalizer bar colors, left to right. */
    val EqColors = listOf(Orange, Gold, Teal, Orange)
}
