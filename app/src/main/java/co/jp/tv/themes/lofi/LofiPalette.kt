package co.jp.tv.themes.lofi

import androidx.compose.ui.graphics.Color

/** Fixed palette of design/project/ThemeLofi.dc.html. Day and night values are named apart. */
object LofiPalette {
    // Room background (root gradient, stops 0 / 60 / 100 %).
    val NightRoomTop = Color(0xFF2A2350)
    val NightRoomMid = Color(0xFF1E1B3A)
    val NightRoomBottom = Color(0xFF17142C)
    val DayRoomTop = Color(0xFF6A69A6)
    val DayRoomMid = Color(0xFF55548C)
    val DayRoomBottom = Color(0xFF45446F)

    // Window sky (stops 0 / 55 / 100 %).
    val NightSkyTop = Color(0xFF3B2D5C)
    val NightSkyMid = Color(0xFF7A4A72)
    val NightSkyBottom = Color(0xFFF2A65A)
    val DaySkyTop = Color(0xFF5FB0EA)
    val DaySkyMid = Color(0xFFA8D8F5)
    val DaySkyBottom = Color(0xFFFDE6B8)

    // Moon (night) and sun (day).
    val NightOrb = Color(0xFFF6E7D0)
    val NightOrbGlow = Color(0xFFF6E7D0).copy(alpha = 0.6f)
    val DayOrb = Color(0xFFFFF3C4)
    val DayOrbGlow = Color(0xFFFFECAA).copy(alpha = 0.9f)

    /** Clouds; each cloud applies its own opacity on top. */
    val Cloud = Color(0xFFFFFFFF)

    // Skyline, alternating A / B buildings.
    val NightBuildingA = Color(0xFF231C3D)
    val NightBuildingB = Color(0xFF2A2148)
    val DayBuildingA = Color(0xFF7C8CBC)
    val DayBuildingB = Color(0xFF93A2CC)

    val RainDrop = Color(0xFFDCD7FF).copy(alpha = 0.45f)

    /** Rain against the bright day sky needs a darker streak than the night's pale one. */
    val RainDropDay = Color(0xFF55608F).copy(alpha = 0.55f)

    // Weather: the grey veil over the sky, dark clouds in the night sky, haze over the skyline, lightning.
    val DayVeil = Color(0xFF6C7488)
    val NightVeil = Color(0xFF14102A)
    val NightCloud = Color(0xFF4B3F6B)
    val DayFog = Color(0xFFD9DEE8)
    val NightFog = Color(0xFF6C5A8C)
    val Flash = Color(0xFFEAF0FF)
    val CityLightAmber = Color(0xFFF2A65A)
    val CityLightPaper = Color(0xFFF6E7D0)
    val WindowFrame = Color(0xFF3A2D4A)

    // Desk (stops 0 / 100 %).
    val NightDeskTop = Color(0xFF4A3047)
    val NightDeskBottom = Color(0xFF331F33)
    val DayDeskTop = Color(0xFF7A5266)
    val DayDeskBottom = Color(0xFF593A4C)

    val SunbeamTop = Color(0xFFFFECBE).copy(alpha = 0.32f)
    val SunbeamBottom = Color(0xFFFFECBE).copy(alpha = 0f)
    val LampGlowCore = Color(0xFFF2A65A).copy(alpha = 0.35f)
    val LampGlowEdge = Color(0xFFF2A65A).copy(alpha = 0f)
    val LampShade = Color(0xFFE98CA8)
    val LampStem = Color(0xFFC9708C)
    val Cat = Color(0xFF1B1530)
    val Steam = Color(0xFFF6E7D0).copy(alpha = 0.5f)

    /** Text, mug and notebook paper. */
    val Paper = Color(0xFFF6E7D0)
    val PlayMark = Color(0xFFE98CA8)

    // Notebook.
    val NotebookLine = Color(0xFFD8C7AD)
    val NotebookMargin = Color(0xFFE98CA8)
    val NotebookTitle = Color(0xFF7A4A72)
    val Ink = Color(0xFF2A2350)
    val InkSoft = Color(0xFF4A3D6E)
    val Highlight = Color(0xFFF2A65A).copy(alpha = 0.45f)

    // "ajustes" pill; the focused look is not in the artboard and keeps the previous treatment.
    val Accent = Color(0xFFF2A65A)
    val PillIdle = Color(0xFFF6E7D0).copy(alpha = 0.1f)
    val PillFocused = Color(0xFFF6E7D0).copy(alpha = 0.25f)

    val Shadow = Color(0xFF000000).copy(alpha = 0.5f)
    val Grain = Color(0xFFFFFFFF).copy(alpha = 0.06f)
    val VignetteClear = Color(0xFF000000).copy(alpha = 0f)
    val VignetteEdge = Color(0xFF000000).copy(alpha = 0.45f)
}
