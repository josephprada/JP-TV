package co.jp.tv.themes.jptv

import androidx.compose.ui.graphics.Color

/** Fixed colors of the JP-TV theme (phase 2 moves the shared ones into core/design). */
internal object JpTvPalette {
    val Ink = Color(0xFF0B0B0C)
    val Paper = Color(0xFFF2EFE9)
    val Accent = Color(0xFFE63329)
    val Muted = Paper.copy(alpha = 0.78f)
    val Faint = Paper.copy(alpha = 0.5f)
    val Line = Paper.copy(alpha = 0.12f)
    val Glass = Color(0xFF121214).copy(alpha = 0.42f)
    val FocusFill = Color(0xFF050506).copy(alpha = 0.62f)
    val TileFill = Paper.copy(alpha = 0.08f)
}
