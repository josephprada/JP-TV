package co.jp.tv.themes.gotham

import androidx.compose.ui.graphics.Color
import co.jp.tv.themes.shared.DayPhase

/**
 * The colours of the Gotham scene for one [DayPhase]. [Night] is the artboard's palette; [Day] is the same
 * city under an overcast grey-blue sky: a muted skyline with dark windows, no aircraft lights and the
 * searchlight off ([beam] = 0). Geometry never changes between the two (see [GothamCity]).
 */
internal class GothamLook(
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val cloud: Color,
    val fog: Color,
    val rain: Color,
    val farTop: Color,
    val farFoot: Color,
    val midTop: Color,
    val midFoot: Color,
    val roofTop: Color,
    val nearTop: Color,
    val nearFoot: Color,
    val ledge: Color,
    val railing: Color,
    val roofHut: Color,
    val roofVent: Color,
    /** Window lights on: night only. */
    val litWindows: Boolean,
    /** Red aircraft lights blinking: night only. */
    val beacons: Boolean,
    /** Strength of the searchlight beam and its monogram, 0..1. */
    val beam: Float,
    /** Colour of lightning lighting the clouds. */
    val flash: Color,
) {
    companion object {
        val Night = GothamLook(
            skyTop = GothamPalette.SkyTop,
            skyMid = GothamPalette.SkyMid,
            skyBottom = GothamPalette.SkyBottom,
            cloud = GothamPalette.Cloud,
            fog = GothamPalette.Fog,
            rain = GothamPalette.Rain,
            farTop = GothamPalette.FarTop,
            farFoot = GothamPalette.FarFoot,
            midTop = GothamPalette.MidTop,
            midFoot = GothamPalette.MidFoot,
            roofTop = GothamPalette.RoofTop,
            nearTop = GothamPalette.NearTop,
            nearFoot = GothamPalette.NearFoot,
            ledge = GothamPalette.Ledge,
            railing = GothamPalette.Railing,
            roofHut = GothamPalette.RoofHut,
            roofVent = GothamPalette.RoofVent,
            litWindows = true,
            beacons = true,
            beam = 1f,
            flash = GothamPalette.Flash,
        )

        val Day = GothamLook(
            skyTop = GothamPalette.DaySkyTop,
            skyMid = GothamPalette.DaySkyMid,
            skyBottom = GothamPalette.DaySkyBottom,
            cloud = GothamPalette.DayCloud,
            fog = GothamPalette.DayFog,
            rain = GothamPalette.DayRain,
            farTop = GothamPalette.DayFarTop,
            farFoot = GothamPalette.DayFarFoot,
            midTop = GothamPalette.DayMidTop,
            midFoot = GothamPalette.DayMidFoot,
            roofTop = GothamPalette.DayRoofTop,
            nearTop = GothamPalette.DayNearTop,
            nearFoot = GothamPalette.DayNearFoot,
            ledge = GothamPalette.DayLedge,
            railing = GothamPalette.DayRailing,
            roofHut = GothamPalette.DayRoofHut,
            roofVent = GothamPalette.DayRoofVent,
            litWindows = false,
            beacons = false,
            beam = 0f,
            flash = GothamPalette.DayFlash,
        )

        fun of(phase: DayPhase): GothamLook = if (phase.isNight) Night else Day
    }
}
