package co.jp.tv.wallpaper.galaxy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.wallpaper.Wallpaper
import co.jp.tv.wallpaper.cssGradientLine
import co.jp.tv.wallpaper.pingPong
import co.jp.tv.wallpaper.rememberFrameMillis

/** Default wallpaper: stars, meteors and aurora orbs. Compose Canvas port of JP-Wallet's galaxy. */
object GalaxyWallpaper : Wallpaper {
    override val id = "galaxy"
    override val displayName = "Galaxia"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val context = LocalContext.current
        val lowEnd = remember { DeviceProfile.isLowEnd(context) }
        val frame = rememberFrameMillis(paused, maxFps = if (lowEnd) 30 else 60)
        val scene = remember { GalaxyScene() }

        // Own layer: a redraw here must not re-record the UI above it.
        Canvas(modifier.fillMaxSize().graphicsLayer()) {
            val t = frame.longValue
            val s = scene.prepare(size.width, size.height, density, accent)
            s.engine.update(t)
            drawRect(s.background)
            drawOrbs(s, t)
            drawStars(s, t, accent)
            drawMeteors(s, accent)
        }
    }
}

private class OrbSpec(
    val x: Float, val y: Float, val diameter: Float, val alpha: Float,
    val driftX: Float, val driftY: Float, val scaleTo: Float, val periodMs: Float, val phase: Float,
)

// Design coordinates (1920x1080): position, size, alpha, drift offset, end scale, period.
private val Orbs = listOf(
    OrbSpec(1100f, -200f, 900f, 0.30f, 60f, -40f, 1.08f, 8000f, 0.0f),
    OrbSpec(1400f, 520f, 700f, 0.18f, -70f, 50f, 0.94f, 10000f, 0.25f),
    OrbSpec(300f, 600f, 800f, 0.14f, 40f, 60f, 1.0f, 7000f, 0.5f),
    OrbSpec(800f, 200f, 520f, 0.12f, 60f, -40f, 1.08f, 9000f, 0.75f),
    OrbSpec(1600f, 80f, 420f, 0.16f, -70f, 50f, 0.94f, 8500f, 0.1f),
)

/** Everything that depends on the canvas size or accent, built once and reused each frame. */
private class GalaxyScene {
    private var cached: Prepared? = null

    class Prepared(
        val width: Float,
        val height: Float,
        val accent: Color,
        val engine: GalaxyEngine,
        val background: Brush,
        val orbBrushes: Array<Brush>,
        val orbCenters: Array<Offset>,
        val orbScale: Float,
    )

    fun prepare(width: Float, height: Float, density: Float, accent: Color): Prepared {
        val c = cached
        if (c != null && c.width == width && c.height == height && c.accent == accent) return c
        val k = width / 1920f
        val (start, end) = cssGradientLine(155f, width, height)
        val centers = Array(Orbs.size) { Offset((Orbs[it].x + Orbs[it].diameter / 2f) * k, (Orbs[it].y + Orbs[it].diameter / 2f) * k) }
        val brushes = Array(Orbs.size) {
            val o = Orbs[it]
            // CSS fades to transparent at 65% of the farthest corner, about 0.92 of the radius.
            Brush.radialGradient(
                0f to accent.copy(alpha = o.alpha),
                1f to accent.copy(alpha = 0f),
                center = centers[it],
                radius = o.diameter / 2f * 0.92f * k,
            )
        }
        val bg = Brush.linearGradient(
            0f to Color(0xFF0A0A0C), 0.45f to Color(0xFF110D10), 1f to Color(0xFF140A0B),
            start = start, end = end,
        )
        return Prepared(width, height, accent, GalaxyEngine(width, height, density), bg, brushes, centers, k)
            .also { cached = it }
    }
}

private fun DrawScope.drawOrbs(s: GalaxyScene.Prepared, t: Long) {
    for (i in Orbs.indices) {
        val o = Orbs[i]
        val p = pingPong(t, o.periodMs, o.phase)
        val sc = 1f + (o.scaleTo - 1f) * p
        translate(o.driftX * s.orbScale * p, o.driftY * s.orbScale * p) {
            scale(sc, sc, pivot = s.orbCenters[i]) {
                drawCircle(s.orbBrushes[i], radius = o.diameter / 2f * s.orbScale, center = s.orbCenters[i])
            }
        }
    }
}

private fun DrawScope.drawStars(s: GalaxyScene.Prepared, t: Long, accent: Color) {
    val e = s.engine
    for (i in 0 until e.starCount) {
        val twinkle = e.twinkle(i, t)
        val center = Offset(e.starX[i], e.starY[i])
        val color = if (e.starIsAccent[i]) accent.copy(alpha = (twinkle * 0.85f).coerceIn(0f, 1f))
        else Color(0.941f, 0.973f, 1f, twinkle.coerceIn(0f, 1f))
        drawCircle(color, e.starRadius[i], center)
        if (e.starBrightness[i] > 0.8f && twinkle > 0.7f) {
            drawCircle(accent.copy(alpha = (twinkle * 0.15f).coerceIn(0f, 1f)), e.starRadius[i] * 3f, center)
        }
    }
}

// Tail gradient stops from head (0) to end (1): white -> pale pink @0.15 -> accent @0.45 -> clear.
private const val TAIL_SEGMENTS = 8

private fun DrawScope.drawMeteors(s: GalaxyScene.Prepared, accent: Color) {
    val e = s.engine
    val stroke = 1.5f * density
    for (i in 0 until GalaxyEngine.MAX_METEORS) {
        if (!e.meteorAlive[i]) continue
        val o = e.meteorOpacity[i]
        val hx = e.meteorX[i]
        val hy = e.meteorY[i]
        val tx = e.meteorVx[i] * e.meteorLength[i] / 12f
        val ty = e.meteorVy[i] * e.meteorLength[i] / 12f
        for (seg in 0 until TAIL_SEGMENTS) {
            val a = seg / TAIL_SEGMENTS.toFloat()
            val b = (seg + 1) / TAIL_SEGMENTS.toFloat()
            val color = tailColor((a + b) / 2f, o, accent)
            drawLine(color, Offset(hx - tx * a, hy - ty * a), Offset(hx - tx * b, hy - ty * b), stroke)
        }
        val head = Offset(hx, hy)
        drawCircle(accent.copy(alpha = 0.35f * o), 1.8f * density * 3f, head)
        drawCircle(Color.White.copy(alpha = o), 1.8f * density, head)
        drawLine(Color.White.copy(alpha = o), head, head, stroke, StrokeCap.Round)
    }
}

private fun tailColor(t: Float, o: Float, accent: Color): Color = when {
    t < 0.15f -> {
        val f = t / 0.15f
        Color(1f, 1f - 0.137f * f, 1f - 0.157f * f, o + (0.85f * o - o) * f)
    }
    t < 0.45f -> {
        val f = (t - 0.15f) / 0.30f
        Color(
            1f + (accent.red - 1f) * f,
            0.863f + (accent.green - 0.863f) * f,
            0.843f + (accent.blue - 0.843f) * f,
            0.85f * o + (0.45f * o - 0.85f * o) * f,
        )
    }
    else -> {
        val f = (t - 0.45f) / 0.55f
        Color(accent.red, accent.green, accent.blue, 0.45f * o * (1f - f))
    }
}
