package co.jp.tv.themes.gotham

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import co.jp.tv.core.design.bakedLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import co.jp.tv.themes.shared.Lightning
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** One drifting `ng-fog` haze: an [EllipseGlow] sliding -220..220 px over [durationMs]. */
private class Haze(val glow: EllipseGlow, val durationMs: Float, val reverse: Boolean = false)

/** The drifting cloud banks and low fog of the artboard, tinted for one [GothamLook]; built once per look. */
private class Atmosphere(private val look: GothamLook) {
    private fun cloud(x: Float, y: Float, w: Float, h: Float, a: Float, seconds: Float) = Haze(
        EllipseGlow(
            x, y, w, h,
            0f to look.cloud.copy(alpha = a),
            0.7f to look.cloud.copy(alpha = 0f),
        ),
        seconds * 1000f,
    )

    private fun fog(x: Float, y: Float, w: Float, h: Float, a: Float, seconds: Float, reverse: Boolean) = Haze(
        EllipseGlow(
            x, y, w, h,
            0f to look.fog.copy(alpha = a),
            0.7f to look.fog.copy(alpha = 0f),
        ),
        seconds * 1000f,
        reverse,
    )

    /** Cloud banks of the artboard: x, y, w, h, opacity, seconds. */
    val clouds = listOf(
        cloud(-260f, -90f, 1000f, 240f, 0.55f, 70f),
        cloud(700f, -120f, 1100f, 260f, 0.45f, 90f),
        cloud(1300f, 40f, 900f, 200f, 0.4f, 80f),
        cloud(100f, 150f, 800f, 170f, 0.3f, 100f),
        cloud(900f, 200f, 900f, 180f, 0.28f, 110f),
    )

    val farFog = fog(-200f, 640f, 1500f, 280f, 0.16f, 38f, reverse = false)
    val nearFog = fog(700f, 800f, 1500f, 260f, 0.14f, 44f, reverse = true)
}

/** Gotham's lightning cycle, 15 s: out of step with the other themes' storms. */
private const val LightningPeriodMs = 15_000
private const val LightningAlpha = 0.4f

/**
 * The city behind the case files, back to front: cloud banks (and lightning behind the skyline), far skyline,
 * low fog, mid skyline with the searchlight rooftop, the searchlight rig, the weather's fog veil, near
 * skyline, front fog. Static layers sit on their own layers and never redraw; only the moving ones read [time].
 * [look] is the phase (night or day) the [city] was built for; [sky] is what the weather does to it.
 */
@Composable
internal fun GothamScene(
    city: GothamCity,
    look: GothamLook,
    sky: GothamSky,
    time: () -> Long,
    modifier: Modifier = Modifier,
) {
    val atmosphere = remember(look) { Atmosphere(look) }
    val clouds = sky.clouds.coerceIn(0f, 1f)
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            if (clouds > 0f) inDesignSpace { for (i in atmosphere.clouds.indices) drawHaze(atmosphere.clouds[i], t, clouds) }
            if (sky.lightning) {
                val f = Lightning.flash(t, LightningPeriodMs)
                if (f > 0f) drawRect(look.flash, alpha = LightningAlpha * f)
            }
        }
        // Gloom: a darker top of the sky under heavy weather. Baked once per look and amount.
        if (sky.murk > 0f) {
            Spacer(
                Modifier.fillMaxSize().bakedLayer(bottomFraction = MurkBottom) {
                    drawRect(
                        Brush.verticalGradient(
                            0f to look.skyTop.copy(alpha = sky.murk),
                            1f to look.skyTop.copy(alpha = 0f),
                            startY = 0f,
                            endY = size.height,
                        ),
                    )
                },
            )
        }
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.far) } })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace { drawHaze(atmosphere.farFog, t) }
        }
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.mid) } })
        if (look.beacons) {
            Canvas(Modifier.fillMaxSize().graphicsLayer()) {
                val t = time()
                inDesignSpace { drawBeacons(city.midBeacons, t) }
            }
        }
        GothamSearchlight(city.motes, time, beam = look.beam)
        // Fog veil: foggy weather thickens the air in front of the far and mid skyline.
        if (sky.fog > 0f) {
            Spacer(
                Modifier.fillMaxSize().bakedLayer(topFraction = FogTop) {
                    // The layer draws in screen space and only the part below FogTop is kept.
                    val screenH = size.height / (1f - FogTop)
                    val top = screenH * FogTop
                    drawRect(
                        Brush.verticalGradient(
                            0f to look.fog.copy(alpha = 0f),
                            1f to look.fog.copy(alpha = sky.fog * FogGain),
                            startY = top,
                            endY = screenH,
                        ),
                        topLeft = Offset(0f, top),
                        size = Size(size.width, screenH - top),
                    )
                },
            )
        }
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.near) } })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace {
                if (look.beacons) drawBeacons(city.nearBeacons, t)
                drawHaze(atmosphere.nearFog, t)
            }
        }
    }
}

/** The murk fades out by 70 % of the screen height; the fog veil starts at 40 % and thickens to the bottom. */
private const val MurkBottom = 0.7f
private const val FogTop = 0.4f
private const val FogGain = 0.6f

/** Fraction of the screen height above which no building reaches (towers top out at y = 540). */
private const val SkylineTop = 0.5f

/** Maps the 1920x1080 artboard onto the screen (one dp per two design px). */
internal inline fun DrawScope.inDesignSpace(block: DrawScope.() -> Unit) {
    scale(0.5.dp.toPx(), pivot = Offset.Zero, block)
}

private fun DrawScope.drawHaze(haze: Haze, timeMs: Long, alpha: Float = 1f) {
    val p = GothamMotion.alternate(timeMs, haze.durationMs, haze.reverse)
    val dx = GothamMotion.FogFromPx + (GothamMotion.FogToPx - GothamMotion.FogFromPx) * p
    translate(left = dx) { haze.glow.draw(this, alpha) }
}
