package co.jp.tv.themes.cyberpunk

import androidx.compose.ui.graphics.Color

/** Fixed palette of the Cyberpunk home (design: ThemeCyberpunk.dc.html). The only place with literal colors. */
object CyberpunkPalette {
    /** Values that change between day and night (design `P`, root, sun, smog, street, vignette, top shade). */
    class Phase(
        val sky: List<Pair<Float, Color>>,
        val far1: Color,
        val far2: Color,
        val mid1: Color,
        val mid2: Color,
        val dotAlpha: Float,
        val windowAlpha: Float,
        val streakAlpha: Float,
        val rainAlpha: Float,
        val bloom: Float,
        val sun: List<Pair<Float, Color>>,
        val sunAlpha: Float,
        val smogA: Color,
        val smogB: Color,
        val street: List<Pair<Float, Color>>,
        val vignetteAlpha: Float,
        val topShadeAlpha: Float,
    )

    val Night = Phase(
        sky = listOf(0f to Color(0xFF07031A), 0.45f to Color(0xFF150834), 0.72f to Color(0xFF2A0C3E), 1f to Color(0xFF120624)),
        far1 = Color(0xFF1D1040),
        far2 = Color(0xFF100726),
        mid1 = Color(0xFF130A2A),
        mid2 = Color(0xFF0A0518),
        dotAlpha = 0.7f,
        windowAlpha = 0.75f,
        streakAlpha = 0.35f,
        rainAlpha = 0.32f,
        bloom = 1f,
        sun = listOf(0f to Color(0xFFE6DEFF), 0.45f to Color(0x80E6DEFF), 0.7f to Color(0x00E6DEFF)),
        sunAlpha = 0.6f,
        smogA = Color(0x52FF2A6D),
        smogB = Color(0x3819F0FF),
        street = listOf(0f to Color(0xFF170B2B), 0.42f to Color(0xFF08040F), 1f to Color(0xFF040209)),
        vignetteAlpha = 0.82f,
        topShadeAlpha = 0f,
    )

    val Day = Phase(
        sky = listOf(0f to Color(0xFF4D6A96), 0.4f to Color(0xFF8F9CBD), 0.7f to Color(0xFFDCB9A6), 1f to Color(0xFF8D7F95)),
        far1 = Color(0xFF7C89AE),
        far2 = Color(0xFF606B90),
        mid1 = Color(0xFF4C5476),
        mid2 = Color(0xFF333A58),
        dotAlpha = 0.25f,
        windowAlpha = 0.3f,
        streakAlpha = 0.16f,
        rainAlpha = 0.2f,
        bloom = 0.7f,
        sun = listOf(0f to Color(0xFFFFF8E2), 0.28f to Color(0xB3FFE2AA), 0.68f to Color(0x00FFD696)),
        sunAlpha = 1f,
        smogA = Color(0x66FFAA78),
        smogB = Color(0x61FFE0B0),
        street = listOf(0f to Color(0xFF4C4F6B), 0.42f to Color(0xFF2A2B3C), 1f to Color(0xFF17171F)),
        vignetteAlpha = 0.5f,
        topShadeAlpha = 1f,
    )

    /** Per-app neon tints of the design's sample list, in order; real apps take `index % size`. */
    val AppTints = listOf(
        Color(0xFFB06CFF),
        Color(0xFFFF2A6D),
        Color(0xFFFF3B5C),
        Color(0xFF9DFF4F),
        Color(0xFF19F0FF),
        Color(0xFF4D8DFF),
        Color(0xFFFFA62E),
        Color(0xFFFFE14D),
        Color(0xFF7FFCFF),
        Color(0xFFFF5FD2),
    )

    val Cyan = Color(0xFF19F0FF)
    val Pink = Color(0xFFFF2A6D)
    val Violet = Color(0xFFB06CFF)
    val Yellow = Color(0xFFFFE14D)
    val Amber = Color(0xFFFFB45E)
    val PinkSoft = Color(0xFFFF6B9A)
    val Lilac = Color(0xFFC9A2FF)

    /** Design `neon` list: cyan, pink, violet, yellow. */
    val Neon = listOf(Cyan, Pink, Violet, Yellow)
    val WindowColors = listOf(Cyan, PinkSoft, Amber, Lilac)
    val StreakColors = listOf(Pink, Cyan, Violet, Amber)

    val Ink = Color(0xFF05020C)
    val FarEdge = Color(0x2EB06CFF)
    val MidEdge = Color(0x2419F0FF)
    val MidStripe = Color(0x06FFFFFF)
    val Beacon = Color(0xFFFF3B30)
    val Steam = Color(0xFFDCD2FF)
    val Splash = Color(0x8CB4DCFF)
    val Rain = Color(0xFFAADCFF)
    val Scanline = Color(0x24000000)

    // Weather: haze over the towers by phase, the storm's lightning wash and the veil under a gloomy sky.
    val FogNight = Color(0xFF7A55B0)
    val FogDay = Color(0xFFCBD0DE)
    val Flash = Color(0xFFD8E4FF)
    val Murk = Color(0xFF05020C)
    val TopShade = Color(0x80080618)

    val SignBodyTop = Color(0xFF170B2A)
    val SignBodyBottom = Color(0xFF0B0517)
    val SignScrew = Color(0xFF3A3148)
    val PoleEdge = Color(0xFF07040D)
    val PoleCenter = Color(0xFF2A2238)
    val PoleBase = Color(0xFF0B0614)

    val Ice = Color(0xFFE8FDFF)
    val White = Color(0xFFFFFFFF)
    val Brand = Color(0xFFFFE0EC)
    val PillFill = Color(0x8C070314)
    val Black = Color(0xFF000000)
}
