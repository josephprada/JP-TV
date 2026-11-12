package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * One moon jelly standing for one app: glow, tentacles and oral arms, the pulsing bell with its
 * four gonads and the app's initial, and the label below. [focus] runs 0 -> 1 as the jelly takes
 * focus (the artboard's 0.8 s transitions); it scales the jelly 0.8 -> 1.3, brightens it and moves
 * the label from 20 px at y 318 to 62 px at y 470. [bob] and the pulse loop on the shared clock.
 */
@Composable
internal fun OceanoJelly(
    index: Int,
    label: String,
    tint: Color,
    art: OceanoJellyArt,
    clock: OceanoClock,
    focus: () -> Float,
    night: () -> Float,
    focusedNow: Boolean,
    modifier: Modifier = Modifier,
) {
    val brushes = remember(tint) { art.brushesFor(tint) }
    val bobPeriod = 3.4f + (index % 4) * 0.5f
    val bobDelay = index * 0.7f
    val pulsePeriod = if (focusedNow) 2.6f else 3.4f + (index % 3) * 0.5f
    val pulseDelay = index * 0.6f
    val density = LocalDensity.current
    val designToPx = with(density) { OceanoDesign.dp(1f).toPx() }
    Box(
        modifier
            .size(OceanoDesign.dp(OceanoJellyArt.BOX_WIDTH), OceanoDesign.dp(OceanoJellyArt.BOX_HEIGHT))
            .graphicsLayer {
                translationY = OceanoMotion.lerp(-14f, 14f, OceanoMotion.alternate(clock.seconds, bobPeriod, bobDelay)) * designToPx
            },
    ) {
        // Its own layer: the per-frame redraw re-records only the drawing, not the text around it.
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val on = focus()
            val lite = clock.lite
            val t = clock.seconds
            val k = size.width / OceanoJellyArt.BOX_WIDTH
            scale(k, k, Offset.Zero) {
                val glowAlpha = (OceanoMotion.lerp(0.22f, 0.6f, on) + OceanoMotion.lerp(OceanoPalette.JellyGlowBoostDay, OceanoPalette.JellyGlowBoostNight, night()))
                drawCircle(brushes.glow, radius = 260f, center = OceanoJellyArt.GlowCenter, alpha = glowAlpha.coerceIn(0f, 1f))
                val inner = OceanoMotion.lerp(0.8f, 1.3f, on)
                scale(inner, inner, OceanoJellyArt.GlowCenter) {
                    drawTentacles(art, tint, on, t, lite)
                    val p = OceanoMotion.pulse(t, pulsePeriod, pulseDelay)
                    scale(OceanoMotion.lerp(1f, 1.07f, p), OceanoMotion.lerp(1f, 0.9f, p), OceanoJellyArt.BellCenter) {
                        drawBell(art, brushes, tint, on, lite)
                    }
                }
            }
        }
        // The initial rides the bell: same focus scale and pulse.
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val inner = OceanoMotion.lerp(0.8f, 1.3f, focus())
                    scaleX = inner
                    scaleY = inner
                    transformOrigin = TransformOrigin(0.5f, 55f / OceanoJellyArt.BOX_HEIGHT)
                },
        ) {
            Box(
                Modifier
                    .offset(OceanoDesign.dp(OceanoJellyArt.BELL_LEFT), OceanoDesign.dp(0f))
                    .size(OceanoDesign.dp(OceanoJellyArt.BELL_WIDTH), OceanoDesign.dp(OceanoJellyArt.BELL_HEIGHT))
                    .graphicsLayer {
                        val p = OceanoMotion.pulse(clock.seconds, pulsePeriod, pulseDelay)
                        scaleX = OceanoMotion.lerp(1f, 1.07f, p)
                        scaleY = OceanoMotion.lerp(1f, 0.9f, p)
                    },
            ) {
                BasicText(
                    label.take(1).uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = OceanoDesign.dp(OceanoJellyArt.BELL_HEIGHT * 0.22f)),
                    style = TextStyle(
                        color = OceanoPalette.White,
                        fontSize = 17.sp,
                        lineHeight = 17.sp,
                        fontFamily = OceanoType.Geist,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        shadow = remember(designToPx) { Shadow(OceanoPalette.InitialShadow, Offset(0f, designToPx), blurRadius = 10f * designToPx) },
                    ),
                )
            }
        }
        OceanoJellyLabel(label, focus, designToPx, clock.lite)
    }
}

@Composable
private fun OceanoJellyLabel(label: String, focus: () -> Float, designToPx: Float, lite: Boolean) {
    val on = focus()
    val shadow = remember(on > 0.5f, designToPx, lite) {
        if (lite && on <= 0.5f) {
            // A blurred text shadow is re-rasterised every frame; unfocused labels go without on low-end boxes.
            null
        } else if (on > 0.5f) {
            Shadow(OceanoPalette.LabelShadow, Offset(0f, 2f * designToPx), blurRadius = 22f * designToPx)
        } else {
            Shadow(OceanoPalette.LabelShadowSoft, Offset(0f, designToPx), blurRadius = 10f * designToPx)
        }
    }
    BasicText(
        label,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally, unbounded = true)
            .offset { IntOffset(0, (OceanoMotion.lerp(318f, 470f, focus()) * designToPx).roundToInt()) },
        style = TextStyle(
            color = OceanoPalette.White,
            fontSize = (OceanoMotion.lerp(20f, 62f, on) / 2f).sp,
            lineHeight = (OceanoMotion.lerp(20f, 62f, on) / 2f * 1.05f).sp,
            fontFamily = OceanoType.Geist,
            fontWeight = if (on > 0.5f) FontWeight.SemiBold else FontWeight.Medium,
            letterSpacing = OceanoMotion.lerp(0.01f, -0.03f, on).em,
            shadow = shadow,
        ),
        maxLines = 1,
        softWrap = false,
    )
}

/** Nine thin tentacles (7 segments) and three striped oral arms (5 segments), each curling. */
private fun DrawScope.drawTentacles(art: OceanoJellyArt, tint: Color, on: Float, t: Float, lite: Boolean) {
    val thinAlpha = OceanoMotion.lerp(0.7f, 0.95f, on)
    // Low-end boxes: unfocused jellies keep every other thin tentacle and lose the faint striped arms.
    val thinStep = if (lite && on < 0.5f) 2 else 1
    for (i in art.thinX.indices step thinStep) {
        val swing = if (art.thinKind[i] == 1) 7f else -9f
        chain(
            x = art.thinX[i],
            length = art.thinLength[i],
            widths = art.thinWidths,
            swingDeg = swing,
            periodS = art.thinPeriod[i],
            t = t,
            color = tint,
            alpha = thinAlpha,
            striped = false,
            art = art,
        )
    }
    val armAlpha = OceanoMotion.lerp(0.38f, 0.6f, on)
    for (i in art.armX.indices) {
        if (lite && on < 0.5f) break
        chain(
            x = art.armX[i],
            length = 46f,
            widths = art.armWidths,
            swingDeg = 5f,
            periodS = art.armPeriod[i],
            t = t,
            color = OceanoPalette.White,
            alpha = armAlpha,
            striped = true,
            art = art,
        )
    }
}

/**
 * A chain of segments hanging from (x, 100), each starting 3 px before the previous one ends,
 * each rotating ±[swingDeg] on top of its parent (`jf-seg*`, alternate), 0.86 the opacity of its
 * parent. A small lag per segment makes the curl travel down the tentacle.
 */
private fun DrawScope.chain(
    x: Float,
    length: Float,
    widths: FloatArray,
    swingDeg: Float,
    periodS: Float,
    t: Float,
    color: Color,
    alpha: Float,
    striped: Boolean,
    art: OceanoJellyArt,
) {
    var px = x
    var py = OceanoJellyArt.TENTACLE_TOP
    var angle = 0f
    var segAlpha = alpha
    for (s in widths.indices) {
        val swing = OceanoMotion.lerp(-swingDeg, swingDeg, OceanoMotion.alternate(t, periodS, s * 0.12f))
        angle += Math.toRadians(swing.toDouble()).toFloat()
        val ex = px - sin(angle) * length
        val ey = py + cos(angle) * length
        drawLine(
            color,
            start = Offset(px, py),
            end = Offset(ex, ey),
            strokeWidth = widths[s],
            cap = if (striped) StrokeCap.Butt else StrokeCap.Round,
            pathEffect = if (striped) art.armStripes else null,
            alpha = segAlpha,
        )
        val back = 3f / length
        px = ex + (px - ex) * back
        py = ey + (py - ey) * back
        segAlpha *= 0.86f
    }
}

/** Bell halo, body, border, gonads, rim and nothing else: the initial is a text overlay. */
private fun DrawScope.drawBell(art: OceanoJellyArt, brushes: OceanoJellyArt.TintBrushes, tint: Color, on: Float, lite: Boolean) {
    val haloAlpha = OceanoMotion.lerp(0.5f, 0.95f, on)
    for (i in art.haloIdle.indices) {
        if (lite && i > 0 && on < 0.5f) break
        val weight = when (i) {
            0 -> 0.3f
            1 -> 0.14f
            else -> 0.06f
        }
        if (on < 1f) drawPath(art.bell, tint, alpha = haloAlpha * weight * (1f - on), style = art.haloIdle[i])
        if (on > 0f) drawPath(art.bell, tint, alpha = haloAlpha * weight * on, style = art.haloFocused[i])
    }
    drawPath(art.bell, brushes.bellIdle, alpha = 1f - on)
    drawPath(art.bell, brushes.bellFocused, alpha = on)
    drawPath(art.bell, OceanoPalette.White, alpha = OceanoMotion.lerp(0.7f, 1f, on), style = art.borderIdle)
    val gonadAlpha = OceanoMotion.lerp(0.75f, 1f, on)
    gonad(art, tint, gonadAlpha, 0.35f, 0.46f, -20f)
    gonad(art, tint, gonadAlpha, 0.65f, 0.46f, 20f)
    gonad(art, tint, gonadAlpha, 0.38f, 0.70f, 200f)
    gonad(art, tint, gonadAlpha, 0.62f, 0.70f, 160f)
    drawArc(
        OceanoPalette.White,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(OceanoJellyArt.BELL_LEFT + 4f, OceanoJellyArt.BELL_HEIGHT - 13f),
        size = Size(OceanoJellyArt.BELL_WIDTH - 8f, 20f),
        alpha = 0.55f,
        style = art.rimStroke,
    )
}

/** A 30x24 horseshoe outline (border without its bottom edge), rotated in place. */
private fun DrawScope.gonad(art: OceanoJellyArt, tint: Color, alpha: Float, fx: Float, fy: Float, rotation: Float) {
    val cx = OceanoJellyArt.BELL_LEFT + OceanoJellyArt.BELL_WIDTH * fx
    val cy = OceanoJellyArt.BELL_HEIGHT * fy
    rotate(rotation, Offset(cx, cy)) {
        drawArc(
            tint,
            startAngle = 150f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(cx - 15f, cy - 12f),
            size = Size(30f, 24f),
            alpha = alpha,
            style = art.gonadStroke,
        )
    }
}
