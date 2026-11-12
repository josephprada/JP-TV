package co.jp.tv.core.design

import androidx.compose.ui.graphics.Color

/** JP design tokens: colors. */
object JpColors {
    val Ink = Color(0xFF0B0B0C)
    val Paper = Color(0xFFF2EFE9)
    /** Raised ink floors from ds/jp/tokens.json (ink < ink-band < ink-raised). */
    val InkBand = Color(0xFF121214)
    val InkRaised = Color(0xFF1A1A1D)
    val Accent = Color(0xFFE63329)
    /** Small red text on paper floors; not used on the dark TV UI, kept so the token set is complete. */
    val AccentDim = Color(0xFFA8231C)
    val AccentLift = Color(0xFFF53D31)
    val Muted = Paper.copy(alpha = 0.78f)
    val Faint = Paper.copy(alpha = 0.5f)
    /** Glass borders: the TV artboards write 0.12 literally (tokens.json says 0.14 for web hairlines). */
    val Line = Paper.copy(alpha = 0.12f)
    val LineStrong = Paper.copy(alpha = 0.4f)

    /** Opacity of a disabled control (tokens.json disabled-opacity). */
    const val DisabledAlpha = 0.5f

    /** Glass fill without real blur (API < 31). Slightly denser than the design's 0.42 to compensate. */
    val GlassFake = Color(0xFF121214).copy(alpha = 0.5f)
    val Glass = Color(0xFF121214).copy(alpha = 0.42f)
    val GlassHighlight = Color.White.copy(alpha = 0.08f)
    val FocusFill = Color(0xFF050506).copy(alpha = 0.62f)
    val TileFill = Paper.copy(alpha = 0.08f)
}
