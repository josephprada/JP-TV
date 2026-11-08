package co.jp.tv.wallpaper.embers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.wallpaper.Wallpaper
import co.jp.tv.wallpaper.pingPong
import co.jp.tv.wallpaper.rememberFrameMillis

/** "Brasas": three large orbs drifting slowly under a vignette. */
object EmbersWallpaper : Wallpaper {
    override val id = "embers"
    override val displayName = "Brasas"

    @Composable
    override fun Render(accent: Color, paused: Boolean, modifier: Modifier) {
        val context = LocalContext.current
        val lowEnd = remember { DeviceProfile.isLowEnd(context) }
        // Motion is very slow, so even 30 fps is plenty; 20 on weak boxes.
        val frame = rememberFrameMillis(paused, maxFps = if (lowEnd) 20 else 30)
        val scene = remember { EmbersScene() }

        Canvas(modifier.fillMaxSize().graphicsLayer()) {
            val t = frame.longValue
            val s = scene.prepare(size.width, size.height, accent)
            drawRect(JpColors.Ink)
            for (i in s.orbs.indices) drawOrb(s, i, t)
            drawVignette(s)
        }
    }
}

private class Orb(
    val x: Float, val y: Float, val diameter: Float, val driftX: Float, val driftY: Float,
    val scaleFrom: Float, val scaleTo: Float, val halfPeriodMs: Float, val stopAt: Float,
)

// Design coordinates (1920x1080). Period is for one direction; the animation alternates.
private val OrbSpecs = listOf(
    Orb(620f, -360f, 1100f, 260f, 140f, 1f, 1.25f, 24000f, 0.65f),
    Orb(1100f, 260f, 1300f, -320f, -120f, 1.1f, 0.9f, 24000f, 0.65f),
    Orb(1250f, 120f, 700f, -160f, 220f, 1f, 1f, 12000f, 0.60f),
)

/** CSS gradient reaches transparent at [Orb.stopAt] of the farthest corner (0.7071 of the diameter). */
private fun orbRadius(o: Orb, k: Float) = o.diameter * k * o.stopAt * 0.7071f

private class EmbersScene {
    private var cached: Prepared? = null

    class Prepared(
        val width: Float, val height: Float, val accent: Color,
        val orbs: Array<Brush>, val centers: Array<Offset>, val k: Float,
        val vignette: Brush, val vignetteCenter: Offset, val vignetteScaleY: Float,
    )

    fun prepare(width: Float, height: Float, accent: Color): Prepared {
        val c = cached
        if (c != null && c.width == width && c.height == height && c.accent == accent) return c
        val k = width / 1920f
        val centers = Array(OrbSpecs.size) {
            Offset((OrbSpecs[it].x + OrbSpecs[it].diameter / 2f) * k, (OrbSpecs[it].y + OrbSpecs[it].diameter / 2f) * k)
        }
        val colors = listOf(accent.copy(alpha = 0.55f), Color(0xE63C0A0E), accent.copy(alpha = 0.35f))
        val brushes = Array(OrbSpecs.size) {
            val o = OrbSpecs[it]
            Brush.radialGradient(
                0f to colors[it],
                1f to colors[it].copy(alpha = 0f),
                center = centers[it],
                radius = orbRadius(o, k),
            )
        }
        // CSS: radial-gradient(120% 90% at 70% 40%, transparent 0, ink@0.75 100%)
        val rx = 1.2f * width
        val ry = 0.9f * height
        val vc = Offset(0.7f * width, 0.4f * height)
        val vignette = Brush.radialGradient(
            0f to Color(0x000B0B0C), 1f to Color(0xBF0B0B0C), center = vc, radius = rx,
        )
        return Prepared(width, height, accent, brushes, centers, k, vignette, vc, ry / rx).also { cached = it }
    }
}

private fun DrawScope.drawOrb(s: EmbersScene.Prepared, i: Int, t: Long) {
    val o = OrbSpecs[i]
    val p = pingPong(t, o.halfPeriodMs * 2f)
    val sc = o.scaleFrom + (o.scaleTo - o.scaleFrom) * p
    translate(o.driftX * s.k * p, o.driftY * s.k * p) {
        scale(sc, sc, pivot = s.centers[i]) {
            drawCircle(s.orbs[i], radius = orbRadius(o, s.k), center = s.centers[i])
        }
    }
}

private fun DrawScope.drawVignette(s: EmbersScene.Prepared) {
    scale(1f, s.vignetteScaleY, pivot = s.vignetteCenter) {
        drawRect(
            s.vignette,
            topLeft = Offset(s.vignetteCenter.x - 4f * s.width, s.vignetteCenter.y - 4f * s.width),
            size = androidx.compose.ui.geometry.Size(8f * s.width, 8f * s.width),
        )
    }
}
