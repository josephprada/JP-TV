package co.jp.tv.themes.gotham

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import co.jp.tv.core.design.bakedLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts

/**
 * The rooftop searchlight rig: a 560x780 design-px box at (245, -8) that sways about its
 * bottom centre, carrying the beam, the dust motes, the "JP" monogram projected on the clouds,
 * the lens flare and the lamp housing.
 */
@Composable
internal fun GothamSearchlight(motes: List<BeamMote>, time: () -> Long, modifier: Modifier = Modifier, beam: Float = 1f) {
    val paint = remember { SearchlightPaint() }
    Box(
        modifier
            .offset(x = 122.5.dp, y = (-4).dp)
            .size(width = 280.dp, height = 390.dp)
            .graphicsLayer {
                val sway = GothamMotion.alternate(time(), GothamMotion.SwayMs)
                rotationZ = GothamMotion.SwayFromDeg + (GothamMotion.SwayToDeg - GothamMotion.SwayFromDeg) * sway
                transformOrigin = TransformOrigin(0.5f, 1f)
            },
    ) {
        // The housing never changes: baked once, rotated with the rig. The beam, glows and lens are baked
        // apart so that daylight can switch them off ([beam] = 0) without redrawing anything.
        Spacer(Modifier.fillMaxSize().bakedLayer { scale(0.5.dp.toPx(), pivot = Offset.Zero) { paint.drawHousing(this) } })
        if (beam > 0f) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = beam }
                    .bakedLayer { scale(0.5.dp.toPx(), pivot = Offset.Zero) { paint.drawBeam(this) } },
            )
            Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = beam }) {
                val t = time()
                scale(0.5.dp.toPx(), pivot = Offset.Zero) { drawMotes(motes, t) }
            }
        }
        if (beam > 0f) Box(
            Modifier
                .offset(x = 77.5.dp, y = 13.dp)
                .size(width = 125.dp, height = 85.dp)
                .graphicsLayer { alpha = beam },
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                "JP",
                style = TextStyle(
                    color = GothamPalette.MonogramInk,
                    fontSize = 56.sp,
                    lineHeight = 56.sp,
                    fontFamily = JpFonts.BebasNeue,
                    letterSpacing = 0.04.em,
                ),
            )
        }
    }
}

/** `ng-mote`: drifts (8, -14) and brightens 0.2 -> 0.9 -> 0.2, ease-in-out per half. */
private fun DrawScope.drawMotes(motes: List<BeamMote>, timeMs: Long) {
    for (i in motes.indices) {
        val m = motes[i]
        val p = GothamMotion.loop(timeMs, m.durationMs, m.delayMs)
        var drift = 0f
        var opacity = 1f
        if (p >= 0f) {
            if (p < 0.5f) {
                drift = GothamMotion.EaseInOut.transform(p * 2f)
                opacity = 0.2f + 0.7f * drift
            } else {
                val e = GothamMotion.EaseInOut.transform((p - 0.5f) * 2f)
                drift = 1f - e
                opacity = 0.9f - 0.7f * e
            }
        }
        val r = m.size / 2f
        drawCircle(
            GothamPalette.Mote,
            radius = r,
            center = Offset(m.left + 8f * drift + r, m.top - 14f * drift + r),
            alpha = 0.9f * opacity,
        )
    }
}

/** Every static brush and path of the rig, in its local design px; built once. */
private class SearchlightPaint {
    private val outerBeam = Path().apply {
        moveTo(252f, 700f)
        lineTo(308f, 700f)
        lineTo(560f, 0f)
        lineTo(0f, 0f)
        close()
    }
    private val outerBeamBrush = Brush.verticalGradient(
        listOf(GothamPalette.BeamOuter.copy(alpha = 0.07f), GothamPalette.BeamOuter.copy(alpha = 0.55f)),
        startY = 0f,
        endY = 700f,
    )
    private val innerBeam = Path().apply {
        moveTo(252f, 700f)
        lineTo(308f, 700f)
        lineTo(420f, 60f)
        lineTo(140f, 60f)
        close()
    }
    private val innerBeamBrush = Brush.verticalGradient(
        listOf(GothamPalette.BeamInner.copy(alpha = 0f), GothamPalette.BeamInner.copy(alpha = 0.5f)),
        startY = 60f,
        endY = 700f,
    )
    private val monogramGlow = EllipseGlow(
        105f, 4f, 350f, 240f,
        0f to GothamPalette.BeamOuter.copy(alpha = 0.55f),
        0.7f to GothamPalette.BeamOuter.copy(alpha = 0f),
    )
    private val monogramDisc = EllipseGlow(
        155f, 26f, 250f, 170f,
        0f to GothamPalette.MonogramCore.copy(alpha = 0.95f),
        0.72f to GothamPalette.BeamOuter.copy(alpha = 0.6f),
        1f to GothamPalette.BeamOuter.copy(alpha = 0.12f),
    )
    private val flare = EllipseGlow(
        165f, 586f, 230f, 230f,
        0f to GothamPalette.BeamInner.copy(alpha = 0.95f),
        0.35f to GothamPalette.FlareWarm.copy(alpha = 0.35f),
        0.68f to GothamPalette.FlareWarm.copy(alpha = 0f),
    )
    private val flareLine = Brush.horizontalGradient(
        listOf(
            GothamPalette.BeamInner.copy(alpha = 0f),
            GothamPalette.BeamInner.copy(alpha = 0.9f),
            GothamPalette.BeamInner.copy(alpha = 0f),
        ),
        startX = 130f,
        endX = 430f,
    )

    // Housing: an 88x82 box whose top-left is (236, 698).
    private val baseBrush = Brush.verticalGradient(
        listOf(GothamPalette.BaseTop, GothamPalette.BaseFoot),
        startY = 758f,
        endY = 780f,
    )
    private val drum = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(244f, 698f, 316f, 762f),
                topLeft = CornerRadius(8f),
                topRight = CornerRadius(8f),
                bottomRight = CornerRadius(4f),
                bottomLeft = CornerRadius(4f),
            ),
        )
    }
    private val drumBrush = Brush.horizontalGradient(
        0f to GothamPalette.DrumLeft,
        0.45f to GothamPalette.DrumShine,
        1f to GothamPalette.DrumRight,
        startX = 244f,
        endX = 316f,
    )

    /** Approximates `box-shadow: inset 0 -8px 12px rgba(0,0,0,0.5)` on the drum. */
    private val drumShade = Brush.verticalGradient(
        listOf(GothamPalette.Shadow.copy(alpha = 0f), GothamPalette.Shadow.copy(alpha = 0.5f)),
        startY = 742f,
        endY = 762f,
    )
    private val rimStroke = Stroke(width = 2f)

    /** Approximates the lens `box-shadow: 0 0 26px 10px rgba(255,240,180,0.85)`. */
    private val lensGlow = EllipseGlow(
        223f, 673f, 114f, 56f,
        0f to GothamPalette.FlareWarm.copy(alpha = 0.85f),
        0.38f to GothamPalette.FlareWarm.copy(alpha = 0.85f),
        0.707f to GothamPalette.FlareWarm.copy(alpha = 0f),
    )
    private val lens = EllipseGlow(
        246f, 696f, 68f, 10f,
        0f to GothamPalette.LensCore,
        0.6f to GothamPalette.LensMid,
        1f to GothamPalette.LensEdge,
    )

    /** Beam, projected monogram, flare and the lit lens: everything the lamp emits. */
    fun drawBeam(scope: DrawScope) = with(scope) {
        drawPath(outerBeam, outerBeamBrush)
        drawPath(innerBeam, innerBeamBrush)
        monogramGlow.draw(this)
        monogramDisc.draw(this)
        flare.draw(this)
        drawRect(flareLine, topLeft = Offset(130f, 698f), size = Size(300f, 3f))
        lensGlow.draw(this)
        lens.draw(this)
    }

    /** The lamp housing: base, brackets, drum and rim. */
    fun drawHousing(scope: DrawScope) = with(scope) {
        drawRoundRect(baseBrush, topLeft = Offset(246f, 758f), size = Size(68f, 22f), cornerRadius = CornerRadius(3f))
        drawRoundRect(GothamPalette.Bracket, topLeft = Offset(236f, 712f), size = Size(7f, 54f), cornerRadius = CornerRadius(3f))
        drawRoundRect(GothamPalette.Bracket, topLeft = Offset(317f, 712f), size = Size(7f, 54f), cornerRadius = CornerRadius(3f))
        drawPath(drum, drumBrush)
        clipPath(drum) {
            // 0deg stripes from the drum foot: 2px dark every 9px.
            var y = 762f - 2f
            while (y > 698f - 2f) {
                drawRect(GothamPalette.Shadow, topLeft = Offset(244f, y), size = Size(72f, 2f), alpha = 0.35f)
                y -= 9f
            }
            drawRect(drumShade, topLeft = Offset(244f, 742f), size = Size(72f, 20f))
        }
        drawOval(GothamPalette.Rim, topLeft = Offset(240f, 694f), size = Size(80f, 18f))
        drawOval(GothamPalette.RimEdge, topLeft = Offset(241f, 695f), size = Size(78f, 16f), style = rimStroke)
    }
}
