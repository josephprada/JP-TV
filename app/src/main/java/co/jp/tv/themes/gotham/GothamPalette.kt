package co.jp.tv.themes.gotham

import androidx.compose.ui.graphics.Color

/** Fixed palette of the "Gotham" home (design/project/ThemeGotham.dc.html). */
object GothamPalette {
    // Night sky, top to bottom.
    val SkyTop = Color(0xFF05070B)
    val SkyMid = Color(0xFF0F1620)
    val SkyBottom = Color(0xFF1A2330)

    // Atmosphere.
    val Cloud = Color(0xFF4A5C74)
    val Fog = Color(0xFF96AFC8)
    val Rain = Color(0xFFBED7FF)

    // Skyline layers: c1 is the top of a building, c2 its foot.
    val FarTop = Color(0xFF16212E)
    val FarFoot = Color(0xFF0F1823)
    val MidTop = Color(0xFF0D141C)
    val MidFoot = Color(0xFF080C12)
    val RoofTop = Color(0xFF0E151D)
    val NearTop = Color(0xFF06090D)
    val NearFoot = Color(0xFF030508)
    val Edge = Color(0xFFAAC3DC)
    val Grid = Color(0xFFFFFFFF)

    // Searchlight rooftop.
    val Ledge = Color(0xFF0A0F15)
    val Railing = Color(0xFF27303B)
    val RoofHut = Color(0xFF121820)
    val RoofVent = Color(0xFF151B23)

    // Lit windows and aircraft beacons.
    val WarmWindows = listOf(Color(0xFFFFD66B), Color(0xFFFFCF5A), Color(0xFFFFE19A))
    val CoolWindows = listOf(Color(0xFFAEE0FF), Color(0xFF8FD0FF))
    val WindowGlow = Color(0xFFFFD66B)
    val Beacon = Color(0xFFFF3B30)

    // Searchlight beam, motes and projected monogram.
    val BeamOuter = Color(0xFFFFF6D2)
    val BeamInner = Color(0xFFFFFAE1)
    val Mote = Color(0xFFFFF8DC)
    val MonogramCore = Color(0xFFFFF8DE)
    val FlareWarm = Color(0xFFFFF0B4)
    val MonogramInk = Color(0xFF0A0E14)

    // Searchlight housing.
    val BaseTop = Color(0xFF2A323D)
    val BaseFoot = Color(0xFF12171D)
    val Bracket = Color(0xFF1B222B)
    val DrumLeft = Color(0xFF12161C)
    val DrumShine = Color(0xFF3A4452)
    val DrumRight = Color(0xFF10141A)
    val Rim = Color(0xFF20262E)
    val RimEdge = Color(0xFF4A5666)
    val LensCore = Color(0xFFFFFBE8)
    val LensMid = Color(0xFFFFE9A0)
    val LensEdge = Color(0xFFB78A2A)

    // Day: the same city under an overcast grey-blue sky. The top of the sky is kept dark enough for the light
    // brand and clock text; the skyline is muted, far to near from hazy to dark.
    val DaySkyTop = Color(0xFF566A80)
    val DaySkyMid = Color(0xFF7B8EA3)
    val DaySkyBottom = Color(0xFFA3B2C1)
    val DayCloud = Color(0xFFDCE4EC)
    val DayFog = Color(0xFFE2EAF2)
    val DayRain = Color(0xFF5A6F8C)
    val DayFarTop = Color(0xFF7E90A4)
    val DayFarFoot = Color(0xFF93A3B4)
    val DayMidTop = Color(0xFF52647A)
    val DayMidFoot = Color(0xFF65788D)
    val DayRoofTop = Color(0xFF566A80)
    val DayNearTop = Color(0xFF2F3D4E)
    val DayNearFoot = Color(0xFF3B4A5C)
    val DayLedge = Color(0xFF3A4859)
    val DayRailing = Color(0xFF4B5B6E)
    val DayRoofHut = Color(0xFF445468)
    val DayRoofVent = Color(0xFF4D5D70)

    // Lightning lighting the clouds, by night and by day.
    val Flash = Color(0xFFBFD4FF)
    val DayFlash = Color(0xFFF2F6FF)

    // Case-file cards.
    val PinLight = Color(0xFFFF7B7B)
    val PinRed = Color(0xFFC0272D)
    val PinDark = Color(0xFF6E0F14)
    val PaperLight = Color(0xFFE0D3AE)
    val PaperDark = Color(0xFFCDBD90)
    val PaperInk = Color(0xFF2B2314)
    val PhotoTop = Color(0xFF4B525D)
    val PhotoFoot = Color(0xFF262B33)
    val Silhouette = Color(0xFF11151B)
    val Redaction = Color(0xFF1A140A)
    val Stamp = Color(0xFFC0272D)
    val Shadow = Color(0xFF000000)

    // Chrome: brand, top bar, arrows, counter.
    val Text = Color(0xFFE8EDF2)
    val ArrowFill = Color(0xFF080B10)
    val FocusOutline = Color(0xFFFFFFFF)

    /**
     * Per-app card scatter from the artboard, derived from the app index:
     * rotation = ((i * 37) % 7 - 3) * 1.1 degrees, drop = ((i * 53) % 5 - 2) * 8 design px.
     */
    const val TiltFactor = 37
    const val TiltModulo = 7
    const val TiltCenter = 3
    const val TiltDegrees = 1.1f
    const val DropFactor = 53
    const val DropModulo = 5
    const val DropCenter = 2
    const val DropPx = 8f

    fun tiltDegrees(index: Int): Float = ((index * TiltFactor) % TiltModulo - TiltCenter) * TiltDegrees

    fun dropPx(index: Int): Float = ((index * DropFactor) % DropModulo - DropCenter) * DropPx
}
