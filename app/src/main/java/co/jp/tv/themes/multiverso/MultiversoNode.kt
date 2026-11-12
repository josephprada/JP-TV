package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * One app as a nexus point on the timeline: its branch (up, down and twigs), bloom, streaks,
 * ripples, spinning ring, orb with the initial, and the label. The box is the artboard's 700x1080
 * branch SVG (350x540 dp) with the node center at (350, 600) design px.
 *
 * [selected] drives the 0.7 s focus transition; [neighbor] is `|i - focus| == 1` (dimmer branch).
 */
@Composable
internal fun MultiversoNode(
    index: Int,
    label: String,
    selected: Boolean,
    neighbor: Boolean,
    frame: LongState,
    modifier: Modifier = Modifier,
) {
    val tint = MultiversoPalette.tint(index)
    val branch = remember(index) { MultiversoGeometry.branch(index) }
    val res = remember(tint) { NodeResources(tint) }
    val focusSpec = tween<Float>(MultiversoMotion.FocusMs, easing = MultiversoMotion.Swift)
    val on = animateFloatAsState(if (selected) 1f else 0f, focusSpec, label = "multiversoNode")
    val coreAlpha = animateFloatAsState(
        when {
            selected -> 1f
            neighbor -> 0.32f
            else -> 0.2f
        },
        focusSpec,
        label = "multiversoCore",
    )
    val density = LocalDensity.current.density
    // Text sizes are drawn at the selected size and scaled down, so the transition never relayouts.
    val designPx = density / 2f
    val initial = remember(label) { firstGlyph(label) }

    Box(modifier.size(350.dp, 540.dp)) {
        // Two layers: the branch and the orb only change on focus, so a clock tick re-records just the
        // light motion (flow, ripples, spinning ring) instead of every path and gradient of the node.
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val p = on.value
            scale(density / 2f, pivot = Offset.Zero) {
                drawBranch(branch, res, p, coreAlpha.value, selected)
                drawNexus(res, tint, p, selected)
            }
        }
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val p = on.value
            val t = frame.longValue
            scale(density / 2f, pivot = Offset.Zero) {
                drawMotion(branch, tint, p, selected, t)
            }
        }
        Box(
            Modifier
                .offset(142.dp, 267.dp)
                .size(66.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                initial,
                modifier = Modifier.graphicsLayer {
                    val s = lerp(21f, 56f, on.value) / 56f
                    scaleX = s
                    scaleY = s
                },
                style = TextStyle(
                    color = if (selected) MultiversoPalette.White else tint,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = MultiversoFonts.Geist,
                    shadow = if (selected) {
                        Shadow(MultiversoPalette.InitialShadow, Offset(0f, 2f * designPx), 12f * designPx)
                    } else {
                        null
                    },
                ),
                maxLines = 1,
                softWrap = false,
            )
        }
        val labelShadow = if (selected) {
            Shadow(tint.copy(alpha = 0.67f), Offset.Zero, 40f * designPx)
        } else {
            // The idle label is the selected size scaled by 17/64, so its shadow is scaled back up.
            Shadow(MultiversoPalette.LabelShadow, Offset(0f, 1f * designPx * LabelScaleUp), 8f * designPx * LabelScaleUp)
        }
        BasicText(
            label,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .wrapContentWidth(unbounded = true)
                .graphicsLayer {
                    val p = on.value
                    val s = lerp(17f, 64f, p) / 64f
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    scaleX = s
                    scaleY = s
                    translationY = lerp(644f, 706f, p) * designPx
                    alpha = lerp(0.72f, 1f, p)
                },
            style = TextStyle(
                color = if (selected) MultiversoPalette.White else MultiversoPalette.Text,
                fontSize = 32.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontFamily = MultiversoFonts.Geist,
                letterSpacing = if (selected) (-0.035).em else 0.01.em,
                lineHeight = 1.05.em,
                shadow = labelShadow,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** Branch layers in the artboard's order: halos, fading cores, twigs, pink fringe, flowing pulse. */
private fun DrawScope.drawBranch(
    branch: MultiversoGeometry.Branch,
    res: NodeResources,
    p: Float,
    coreAlpha: Float,
    selected: Boolean,
) {
    val tint = res.tint
    NodeShared.halo26.draw(this, branch.up, tint, 0.55f * p)
    NodeShared.halo20.draw(this, branch.down, tint, 0.55f * p)
    val core = NodeShared.coreStroke(lerp(1.1f, 2.8f, p))
    drawPath(branch.up, NodeShared.fadeUp, alpha = coreAlpha, style = core)
    drawPath(branch.down, NodeShared.fadeDown, alpha = coreAlpha, style = core)
    NodeShared.twig.draw(
        this,
        branch.twigs,
        if (selected) MultiversoPalette.TwigOn else MultiversoPalette.TwigOff,
        lerp(0.18f, 0.95f, p),
    )
    translate(2f, 2f) {
        NodeShared.twigFringe.draw(this, branch.twigs, MultiversoPalette.Fringe, 0.55f * p)
    }
}

/** The moving light: the pulse along the branch, two ripples and the spinning dashed ring. */
private fun DrawScope.drawMotion(branch: MultiversoGeometry.Branch, tint: Color, p: Float, selected: Boolean, t: Long) {
    if (p > 0.004f) {
        NodeShared.flow.at(t, MultiversoMotion.BranchFlowMs).draw(this, branch.up, MultiversoPalette.White, p)
    }
    val c = MultiversoGeometry.NodeCenter
    if (selected) {
        drawRipple(tint, loopProgress(t, MultiversoMotion.RippleMs))
        drawRipple(MultiversoPalette.RippleWhite, loopProgress(t, MultiversoMotion.RippleMs, MultiversoMotion.RippleDelayMs))
    }
    val spinMs = if (selected) MultiversoMotion.RingSpinOnMs else MultiversoMotion.RingSpinOffMs
    rotate(360f * loopProgress(t, spinMs), pivot = c) {
        drawCircle(
            MultiversoPalette.White,
            radius = lerp(74f, 196f, p) / 2f,
            center = c,
            alpha = if (selected) 0.5f else 0.22f,
            style = NodeShared.ring,
        )
    }
}

/** Button layers: bloom, soft streak, streak, two ripples, dashed ring, orb. */
private fun DrawScope.drawNexus(res: NodeResources, tint: Color, p: Float, selected: Boolean) {
    val c = MultiversoGeometry.NodeCenter
    drawEllipseGradient(res.bloom, c, BloomRadius, BloomRadius, alpha = p)

    if (p > 0.004f) {
        for (k in StreakSoftHeights.indices) {
            val h = StreakSoftHeights[k]
            drawOval(
                NodeShared.streakSoft,
                topLeft = Offset(c.x - 750f, c.y - h / 2f),
                size = Size(1500f, h),
                alpha = p * StreakSoftAlphas[k],
            )
        }
        drawRect(res.streak, topLeft = Offset(c.x - 650f, c.y - 1f), size = Size(1300f, 2f), alpha = 0.9f * p)
    }

    val r = lerp(52f, 132f, p) / 2f
    // box-shadow: 0 0 18px (idle) / 0 0 80px (selected), cross-faded.
    val glowReach = lerp(18f, 80f, p)
    drawEllipseGradient(res.glowOff, c, r + glowReach, r + glowReach, alpha = 1f - p)
    drawEllipseGradient(res.glowOn, c, r + glowReach, r + glowReach, alpha = p)
    // Gradient backgrounds do not transition in CSS: the fill switches with the selection.
    drawEllipseGradient(if (selected) res.orbOn else NodeShared.orbOff, c, r, r)
    drawEllipseGradient(if (selected) NodeShared.insetOn else NodeShared.insetOff, c, r, r)
    drawCircle(
        if (selected) MultiversoPalette.OrbBorderOn else tint.copy(alpha = 0.7f),
        radius = r - 0.75f,
        center = c,
        style = NodeShared.orbBorder,
    )
}

/** `mv-ripple`: scale 0.8 -> 2.4 and opacity 0.7 -> 0 with ease-out; [progress] < 0 is the delay. */
private fun DrawScope.drawRipple(color: Color, progress: Float) {
    if (progress < 0f) return
    val e = MultiversoMotion.CssEaseOut.transform(progress)
    val s = 0.8f + 1.6f * e
    drawCircle(
        color,
        radius = 70f * s - s / 2f,
        center = MultiversoGeometry.NodeCenter,
        alpha = 0.7f * (1f - e),
        style = NodeShared.rippleStroke(s),
    )
}

private fun firstGlyph(label: String): String =
    if (label.isEmpty()) "" else label.substring(0, label.offsetByCodePoints(0, 1))

/** `radial-gradient(circle, ...)` on the 560 px bloom reaches the corner: 280 * sqrt(2). */
private const val BloomRadius = 396f
private const val LabelScaleUp = 64f / 17f
private val StreakSoftHeights = floatArrayOf(38f, 22f, 10f)
private val StreakSoftAlphas = floatArrayOf(0.3f, 0.45f, 0.45f)

/** Brushes that depend on the app tint; built once per node. */
private class NodeResources(val tint: Color) {
    val bloom = unitRadial(
        0f to tint.copy(alpha = 0.35f),
        0.35f to tint.copy(alpha = 0.12f),
        0.68f to tint.copy(alpha = 0f),
    )
    val streak = Brush.horizontalGradient(
        0f to MultiversoPalette.White.copy(alpha = 0f),
        0.35f to tint,
        0.5f to MultiversoPalette.White,
        0.65f to tint,
        1f to MultiversoPalette.White.copy(alpha = 0f),
        startX = -300f,
        endX = 1000f,
    )

    /** Glow edge at the orb radius: 26 / (26 + 18) idle, 66 / (66 + 80) selected. */
    val glowOff = unitRadial(
        0f to tint.copy(alpha = 0.33f),
        0.59f to tint.copy(alpha = 0.33f),
        1f to tint.copy(alpha = 0f),
    )
    val glowOn = unitRadial(
        0f to tint.copy(alpha = 0.65f),
        0.45f to tint.copy(alpha = 0.65f),
        1f to tint.copy(alpha = 0f),
    )
    val orbOn = orbBrush(
        0f to MultiversoPalette.White.copy(alpha = 0.9f),
        0.22f to tint,
        0.72f to MultiversoPalette.OrbDeepOn,
    )
}

/** `radial-gradient(circle at 34% 28%, ...)` in a unit orb: farthest corner is 0.9767 of the box. */
private fun orbBrush(vararg stops: Pair<Float, Color>): Brush =
    Brush.radialGradient(*stops, center = Offset(-0.32f, -0.44f), radius = 1.9534f)

/** Tint-independent drawing objects shared by every node. */
private object NodeShared {
    val halo26 = SoftStroke(width = 26f, sigma = 8f)
    val halo20 = SoftStroke(width = 20f, sigma = 8f)
    val twig = SoftStroke(width = 1.3f, sigma = 1.2f)
    val twigFringe = SoftStroke(width = 1f, sigma = 0f)

    /** `stroke-dasharray: 44 416`, width 3.4, blurred 1.2; 115 phase slots of 4 px. */
    val flow = FlowDash(dash = 44f, gap = 416f, width = 3.4f, sigma = 1.2f, steps = 115)

    private val coreStrokes = Array(18) { Stroke(1.1f + it * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round) }
    fun coreStroke(width: Float): Stroke = coreStrokes[((width - 1.1f) / 0.1f).roundToInt().coerceIn(0, coreStrokes.size - 1)]

    private val rippleStrokes = Array(17) { Stroke(0.8f + it * 0.1f) }
    fun rippleStroke(scale: Float): Stroke = rippleStrokes[((scale - 0.8f) / 0.1f).roundToInt().coerceIn(0, rippleStrokes.size - 1)]

    val ring = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)))
    val orbBorder = Stroke(1.5f)

    val fadeUp = Brush.linearGradient(
        0f to MultiversoPalette.White,
        0.55f to MultiversoPalette.FadeMid.copy(alpha = 0.85f),
        1f to MultiversoPalette.FadeEnd.copy(alpha = 0f),
        start = Offset(0f, 600f),
        end = Offset(0f, -40f),
    )
    val fadeDown = Brush.linearGradient(
        0f to MultiversoPalette.White,
        0.5f to MultiversoPalette.FadeMid.copy(alpha = 0.7f),
        1f to MultiversoPalette.FadeEnd.copy(alpha = 0f),
        start = Offset(0f, 600f),
        end = Offset(0f, 1120f),
    )
    val streakSoft = Brush.horizontalGradient(
        0f to MultiversoPalette.StreakCyan.copy(alpha = 0f),
        0.4f to MultiversoPalette.StreakCyan.copy(alpha = 0.45f),
        0.5f to MultiversoPalette.White.copy(alpha = 0.8f),
        0.6f to MultiversoPalette.StreakCyan.copy(alpha = 0.45f),
        1f to MultiversoPalette.StreakCyan.copy(alpha = 0f),
        startX = -400f,
        endX = 1100f,
    )
    val orbOff = orbBrush(
        0f to MultiversoPalette.White.copy(alpha = 0.12f),
        0.6f to MultiversoPalette.OrbDeepOff,
        1f to MultiversoPalette.OrbDeepOff,
    )

    /** Inset glow `0 0 30px` on a 66 px radius (selected) and `0 0 10px` on 26 px (idle). */
    val insetOn = unitRadial(
        0f to MultiversoPalette.White.copy(alpha = 0f),
        0.545f to MultiversoPalette.White.copy(alpha = 0f),
        1f to MultiversoPalette.White.copy(alpha = 0.35f),
    )
    val insetOff = unitRadial(
        0f to MultiversoPalette.White.copy(alpha = 0f),
        0.615f to MultiversoPalette.White.copy(alpha = 0f),
        1f to MultiversoPalette.White.copy(alpha = 0.12f),
    )
}
