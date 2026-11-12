package co.jp.tv.themes.gotham

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import co.jp.tv.core.design.bakedLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** One drifting `ng-fog` haze: an [EllipseGlow] sliding -220..220 px over [durationMs]. */
private class Haze(val glow: EllipseGlow, val durationMs: Float, val reverse: Boolean = false)

private fun cloud(x: Float, y: Float, w: Float, h: Float, a: Float, seconds: Float) = Haze(
    EllipseGlow(
        x, y, w, h,
        0f to GothamPalette.Cloud.copy(alpha = a),
        0.7f to GothamPalette.Cloud.copy(alpha = 0f),
    ),
    seconds * 1000f,
)

private fun fog(x: Float, y: Float, w: Float, h: Float, a: Float, seconds: Float, reverse: Boolean) = Haze(
    EllipseGlow(
        x, y, w, h,
        0f to GothamPalette.Fog.copy(alpha = a),
        0.7f to GothamPalette.Fog.copy(alpha = 0f),
    ),
    seconds * 1000f,
    reverse,
)

/** Cloud banks of the artboard: x, y, w, h, opacity, seconds. */
private val Clouds = listOf(
    cloud(-260f, -90f, 1000f, 240f, 0.55f, 70f),
    cloud(700f, -120f, 1100f, 260f, 0.45f, 90f),
    cloud(1300f, 40f, 900f, 200f, 0.4f, 80f),
    cloud(100f, 150f, 800f, 170f, 0.3f, 100f),
    cloud(900f, 200f, 900f, 180f, 0.28f, 110f),
)

private val FarFog = fog(-200f, 640f, 1500f, 280f, 0.16f, 38f, reverse = false)
private val NearFog = fog(700f, 800f, 1500f, 260f, 0.14f, 44f, reverse = true)

/**
 * The night city behind the case files, back to front: cloud banks, far skyline, low fog,
 * mid skyline with the searchlight rooftop, the searchlight rig, near skyline, front fog.
 * Static layers sit on their own layers and never redraw; only the moving ones read [time].
 */
@Composable
internal fun GothamScene(city: GothamCity, time: () -> Long, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace { for (i in Clouds.indices) drawHaze(Clouds[i], t) }
        }
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.far) } })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace { drawHaze(FarFog, t) }
        }
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.mid) } })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace { drawBeacons(city.midBeacons, t) }
        }
        GothamSearchlight(city.motes, time)
        // Static skyline: baked once into the lower half of the screen, where the buildings stand.
        Spacer(Modifier.fillMaxSize().bakedLayer(topFraction = SkylineTop) { inDesignSpace { drawCityLayer(city.near) } })
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val t = time()
            inDesignSpace {
                drawBeacons(city.nearBeacons, t)
                drawHaze(NearFog, t)
            }
        }
    }
}

/** Fraction of the screen height above which no building reaches (towers top out at y = 540). */
private const val SkylineTop = 0.5f

/** Maps the 1920x1080 artboard onto the screen (one dp per two design px). */
internal inline fun DrawScope.inDesignSpace(block: DrawScope.() -> Unit) {
    scale(0.5.dp.toPx(), pivot = Offset.Zero, block)
}

private fun DrawScope.drawHaze(haze: Haze, timeMs: Long) {
    val p = GothamMotion.alternate(timeMs, haze.durationMs, haze.reverse)
    val dx = GothamMotion.FogFromPx + (GothamMotion.FogToPx - GothamMotion.FogFromPx) * p
    translate(left = dx) { haze.glow.draw(this) }
}
