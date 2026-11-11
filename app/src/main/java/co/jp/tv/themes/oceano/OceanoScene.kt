package co.jp.tv.themes.oceano

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Everything behind the jellies, back to front as in the artboard: sunlight and caustics, marine
 * snow, the far reef, the mid reef and sand with their fish, the turtle, the foreground reef and
 * its swaying corals, the close fish, bubbles and, at night, the darkness and glowing life.
 * [night] runs 0 (day) to 1 (night) and is only read at draw time.
 */
@Composable
internal fun OceanoScene(clock: OceanoClock, night: () -> Float, modifier: Modifier = Modifier) {
    val scenery = remember { OceanoReefScenery.build() }
    val shoals = remember { OceanoShoals.build(OceanoFishShapes.build()) }
    val causticsTop = remember(night) { { OceanoMotion.lerp(OceanoPalette.CausticsAlphaDay, OceanoPalette.CausticsAlphaNight, night()) } }
    val reefFish = remember(night) { { OceanoMotion.lerp(OceanoPalette.ReefFishAlphaDay, OceanoPalette.ReefFishAlphaNight, night()) } }
    val schools = remember(night) { { OceanoMotion.lerp(OceanoPalette.SchoolAlphaDay, OceanoPalette.SchoolAlphaNight, night()) } }
    Box(modifier.fillMaxSize()) {
        OceanoSunlight(clock, night)
        OceanoCaustics(
            clock,
            top = 0f,
            bottom = 330f,
            fadePeriodS = 9f,
            driftPeriodsS = 30f to 38f,
            cellWidth = 220f,
            fadeDown = true,
            alpha = causticsTop,
        )
        OceanoMarineSnow(clock)
        OceanoReefLayer(scenery, OceanoReefBand.Far)
        OceanoFishLayer(clock, shoals.far)
        OceanoReefLayer(scenery, OceanoReefBand.Mid)
        OceanoCaustics(
            clock,
            top = 880f,
            bottom = 1080f,
            fadePeriodS = 7f,
            driftPeriodsS = null,
            cellWidth = 110f,
            fadeDown = false,
            alpha = SandCausticsAlpha,
        )
        OceanoFishLayer(clock, shoals.mid, reefFish)
        OceanoFishLayer(clock, shoals.daySchools, schools)
        OceanoFishLayer(clock, shoals.crossing)
        OceanoTurtle(clock)
        OceanoReefLayer(scenery, OceanoReefBand.Fore)
        OceanoCoralGarden(clock)
        OceanoFishLayer(clock, shoals.close, reefFish)
        OceanoFishLayer(clock, shoals.closeSchool, schools)
        OceanoBubbles(clock)
        OceanoNightGlow(clock, scenery, night)
    }
}

/** The sand caustics keep `opacity: 0.55` day and night. */
private val SandCausticsAlpha: () -> Float = { 0.55f }
