package co.jp.tv.themes.multiverso

import androidx.compose.ui.graphics.Color

/** Fixed palette of design/project/ThemeMultiverso.dc.html. The only file with literal colors. */
object MultiversoPalette {
    // Page background: radial-gradient(90% 80% at 50% 56%, #101a5c 0%, #070c38 45%, #030418 100%).
    val BackgroundCenter = Color(0xFF101A5C)
    val BackgroundMid = Color(0xFF070C38)
    val BackgroundEdge = Color(0xFF030418)

    val White = Color(0xFFFFFFFF)
    val Text = Color(0xFFEAF2FF)

    /** rgba(234,242,255,0.85): network link and icons. */
    val TextSoft = Color(0xD9EAF2FF)

    /** rgba(234,242,255,0.72): labels of the apps that are not selected. */
    val TextLabel = Color(0xB8EAF2FF)

    /** rgba(234,242,255,0.7): the "02 / 10" position. */
    val TextPosition = Color(0xB3EAF2FF)

    /** rgba(140,180,255,0.7): brand text-shadow. */
    val BrandGlow = Color(0xB38CB4FF)

    // Nebulae (center colors; each fades to transparent at 68%).
    val NebulaViolet = Color(0x527C5CFF)
    val NebulaSky = Color(0x3850BEFF)
    val NebulaPink = Color(0x29FF6FD8)
    val NebulaBlue = Color(0x383C5AFF)

    // Stars.
    val StarBlue = Color(0xFFD8E8FF)
    val StarPink = Color(0xFFFFB8EE)

    /** rgba(200,220,255,0.8): near-star box-shadow. */
    val StarGlow = Color(0xCCC8DCFF)

    // Distant timelines (opacity applied when drawing: 0.35 and 0.22).
    val FarLineGlow = Color(0xFF8A7CFF)
    val FarLineCore = Color(0xFFBCD4FF)

    /** rgba(96,84,255,0.26): haze around the main timeline. */
    val Haze = Color(0x426054FF)

    // Main timeline.
    val TrunkViolet = Color(0xFF5B48FF)
    val TrunkCyan = Color(0xFF6FE3FF)
    val TrunkCore = Color(0xFFBFE9FF)
    val Fringe = Color(0xFFFF6FD8)

    // mvFadeUp / mvFadeDown gradient stops.
    val FadeMid = Color(0xFFD6E9FF)
    val FadeEnd = Color(0xFF9D7BFF)

    val TwigOn = Color(0xFFE3F0FF)
    val TwigOff = Color(0xFFB8C8FF)

    /** rgb(111,227,255): soft horizontal streak behind the selected node. */
    val StreakCyan = Color(0xFF6FE3FF)

    /** rgba(255,255,255,0.6): second ripple ring. */
    val RippleWhite = Color(0x99FFFFFF)

    // Orb.
    val OrbDeepOn = Color(0xF512165C)
    val OrbDeepOff = Color(0xEB0A0E3E)
    val OrbBorderOn = Color(0xD9FFFFFF)
    val InitialShadow = Color(0xCC0A0E3E)
    val LabelShadow = Color(0xE6030418)

    /** rgba(2,3,16,0.85): vignette edge. */
    val Vignette = Color(0xD9020310)

    // Scrubber.
    val ScrubLine = Color(0x61C8DCFF)
    val TickIdle = Color(0x80DCE8FF)

    /**
     * Per-app tints of the artboard's sample list, in its order. App `i` uses `AppTints[i % size]`,
     * so the colors stay deterministic for any number of apps.
     */
    val AppTints = listOf(
        Color(0xFF9D7BFF),
        Color(0xFFFF7AA8),
        Color(0xFFFF6F91),
        Color(0xFF6FF0B4),
        Color(0xFF6FC8FF),
        Color(0xFF7A9BFF),
        Color(0xFFFFB36F),
        Color(0xFFFFE07A),
        Color(0xFFB8C4FF),
        Color(0xFFC58BFF),
    )

    fun tint(index: Int): Color = AppTints[Math.floorMod(index, AppTints.size)]
}
