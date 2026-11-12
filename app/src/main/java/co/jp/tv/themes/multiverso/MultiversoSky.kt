package co.jp.tv.themes.multiverso

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.draw.drawWithCache
import co.jp.tv.core.design.bakeToBitmap
import co.jp.tv.core.design.bakedLayer
import co.jp.tv.core.util.rememberLowEnd
import kotlin.math.ceil
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Page background, the four drifting nebulae, deep stars, distant timelines and near stars, in the
 * artboard's stacking order. [frame] drives the loops (frozen while paused); [parallax] is the
 * eased focus index that shifts the star and timeline layers (-5, -8 and -14 px per app).
 */
@Composable
internal fun MultiversoSky(frame: LongState, parallax: State<Float>, modifier: Modifier = Modifier) {
    val sky = remember { MultiversoGeometry.sky() }
    val res = remember { SkyResources() }
    val lite = rememberLowEnd()
    // Low-end: the page background and the four nebulae are baked once into one bitmap (the nebulae drift
    // 80 px over half a minute, which is not worth four full-screen gradients per frame).
    if (lite) {
        Spacer(
            modifier.fillMaxSize().bakedLayer {
                scale(density / 2f, pivot = Offset.Zero) {
                    drawEllipseGradient(res.background, Offset(960f, 604.8f), 1728f, 864f, fill = true)
                    drawNebulae(res, 0.5f, null)
                }
            },
        )
    } else {
        Canvas(modifier.fillMaxSize().graphicsLayer()) {
            val t = frame.longValue
            scale(density / 2f, pivot = Offset.Zero) {
                drawEllipseGradient(res.background, Offset(960f, 604.8f), 1728f, 864f, fill = true)
                drawNebulae(res, null, t)
            }
        }
    }
    // Far timelines only shift with the focus: bake them and move the bitmap.
    val linesLayer = Modifier.drawWithCache {
        val k = density / 2f
        val baked = bakeToBitmap(ceil(2100f * k).toInt(), ceil(1080f * k).toInt()) {
            scale(k, pivot = Offset.Zero) {
                translate(left = 90f) {
                    for (line in sky.farLines) res.farGlow.draw(this, line, MultiversoPalette.FarLineGlow, 0.35f * FarLinesOpacity)
                    for (line in sky.farLines) res.farCore.draw(this, line, MultiversoPalette.FarLineCore, 0.22f * FarLinesOpacity)
                }
            }
        }
        onDrawBehind { drawImage(baked, topLeft = Offset((-90f - parallax.value * 8f) * k, 0f)) }
    }
    Spacer(modifier.fillMaxSize().graphicsLayer().then(linesLayer))
    // Own layer: a tick here must not re-record the UI above it.
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        val t = frame.longValue
        val focus = parallax.value
        scale(density / 2f, pivot = Offset.Zero) {
            val farX = -40f - focus * 5f
            for (star in sky.far) {
                drawCircle(
                    if (star.pink) MultiversoPalette.StarPink else MultiversoPalette.StarBlue,
                    radius = star.size / 2f,
                    center = Offset(farX + star.x + star.size / 2f, star.y + star.size / 2f),
                    alpha = twinkleAlpha(t, star.durationMs, star.delayMs),
                )
            }
            val nearX = -40f - focus * 14f
            for (star in sky.near) {
                val alpha = twinkleAlpha(t, star.durationMs, star.delayMs)
                val cx = nearX + star.x + star.size / 2f
                val cy = star.y + star.size / 2f
                if (lite) {
                    // A flat halo instead of a gradient per star.
                    drawCircle(MultiversoPalette.StarGlow, radius = StarGlowRadius * 0.6f, center = Offset(cx, cy), alpha = alpha * 0.35f)
                } else {
                    translate(cx, cy) {
                        drawCircle(res.starGlow, radius = StarGlowRadius, center = Offset.Zero, alpha = alpha)
                    }
                }
                drawCircle(
                    if (star.pink) MultiversoPalette.StarPink else MultiversoPalette.StarBlue,
                    radius = star.size / 2f,
                    center = Offset(cx, cy),
                    alpha = alpha,
                )
            }
        }
    }
}

/** The four drifting nebulae at clock time [t], or frozen at drift progress [fixed] when baking. */
private fun DrawScope.drawNebulae(res: SkyResources, fixed: Float?, t: Long?) {
    for (k in Nebulae.indices) {
        val n = Nebulae[k]
        val p = fixed ?: alternateProgress(t ?: 0L, n.durationMs, n.reverse)
        val s = 1f + 0.12f * p
        drawEllipseGradient(
            res.nebulae[k],
            Offset(n.left + n.width / 2f + 80f * p, n.top + n.height / 2f - 40f * p),
            n.width / 2f * Sqrt2 * s,
            n.height / 2f * Sqrt2 * s,
        )
    }
}

private class Nebula(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val durationMs: Long,
    val reverse: Boolean,
)

private val Nebulae = listOf(
    Nebula(-260f, -200f, 1100f, 800f, MultiversoPalette.NebulaViolet, 28_000L, reverse = false),
    Nebula(1100f, -260f, 1100f, 760f, MultiversoPalette.NebulaSky, 34_000L, reverse = true),
    Nebula(1200f, 640f, 1000f, 700f, MultiversoPalette.NebulaPink, 30_000L, reverse = false),
    Nebula(-120f, 680f, 1000f, 640f, MultiversoPalette.NebulaBlue, 38_000L, reverse = true),
)

/** CSS `radial-gradient(ellipse, ...)` reaches the box corner: half-size times sqrt(2). */
private const val Sqrt2 = 1.4142135f
private const val FarLinesOpacity = 0.8f

/** 3 px star with `box-shadow: 0 0 6px`: glow reaches 1.5 + 6 px. */
private const val StarGlowRadius = 7.5f

private class SkyResources {
    val background = unitRadial(
        0f to MultiversoPalette.BackgroundCenter,
        0.45f to MultiversoPalette.BackgroundMid,
        1f to MultiversoPalette.BackgroundEdge,
    )
    val nebulae = Nebulae.map { unitRadial(0f to it.color, 0.68f to it.color.copy(alpha = 0f)) }
    val starGlow = Brush.radialGradient(
        0.2f to MultiversoPalette.StarGlow,
        1f to MultiversoPalette.StarGlow.copy(alpha = 0f),
        center = Offset.Zero,
        radius = StarGlowRadius,
    )
    val farGlow = SoftStroke(width = 10f, sigma = 8f)
    val farCore = SoftStroke(width = 1.2f, sigma = 1.2f)
}
