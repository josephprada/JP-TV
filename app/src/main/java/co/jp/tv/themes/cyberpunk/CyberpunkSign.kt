package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlin.math.roundToInt

/** Sign button: 140x440 at (60, 246) inside its 260 px column. */
private const val BODY_W = 140f
private const val BODY_H = 440f
private const val BODY_RADIUS = 20f

/** Upright vertical name: 28 px glyphs with -0.12em letter spacing. */
private const val NAME_FONT_PX = 28
private const val NAME_ADVANCE_PX = NAME_FONT_PX * (1f - 0.12f)

/**
 * One neon sign of the street (one per app): spill on the street, striped reflection, pole, and
 * the sign itself with bloom, tube, badge and upright name. Focus animates every property over
 * 0.6s with the design ease; [signOn] drives the `cy-on` power-up of the focused sign.
 */
@Composable
internal fun CyberpunkSign(
    name: String,
    tint: Color,
    focused: Boolean,
    distance: Int,
    bloom: Float,
    signOn: State<Float>,
    modifier: Modifier = Modifier,
) {
    val p by animateFloatAsState(
        if (focused) 1f else 0f,
        tween(CyberpunkMotion.FocusMs, easing = CyberpunkMotion.Ease),
        label = "signFocus",
    )
    val opacity by animateFloatAsState(
        when {
            focused -> 1f
            distance == 1 -> 0.9f
            else -> 0.75f
        },
        tween(CyberpunkMotion.FocusMs, easing = CyberpunkMotion.Ease),
        label = "signOpacity",
    )
    val brushes = remember(tint) { SignBrushes(tint) }
    val strokes = remember { SignStrokes() }
    Box(modifier.size(260.dpx, 1080.dpx)) {
        Spacer(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    inDesignPx {
                        ovalGradient(130f, 783f, 190f, 35f, brushes.spill, alpha = p)
                        // Reflection: gradient masked by 6 px solid / 3 px at 0.35 bands, from the bottom.
                        val reflect = 0.14f + (0.6f - 0.14f) * p
                        var bottom = 1074f
                        while (bottom > 794f) {
                            val solidTop = (bottom - 6f).coerceAtLeast(794f)
                            drawRect(brushes.reflect, Offset(50f, solidTop), Size(160f, bottom - solidTop), alpha = reflect)
                            val dimBottom = bottom - 6f
                            val dimTop = (bottom - 9f).coerceAtLeast(794f)
                            if (dimBottom > dimTop) {
                                drawRect(brushes.reflect, Offset(50f, dimTop), Size(160f, dimBottom - dimTop), alpha = reflect * 0.35f)
                            }
                            bottom -= 9f
                        }
                        drawRect(brushes.pole, Offset(126f, 686f), Size(8f, 94f))
                        drawRoundRect(CyberpunkPalette.PoleBase, Offset(106f, 774f), Size(48f, 8f), CornerRadius(2f, 2f))
                    }
                },
        )
        Box(
            Modifier
                .offset(60.dpx, 246.dpx)
                .size(BODY_W.dpx, BODY_H.dpx)
                .graphicsLayer {
                    val unit = 0.5.dp.toPx()
                    val k = 0.96f + (1.08f - 0.96f) * p
                    translationY = -8f * p * unit
                    scaleX = k
                    scaleY = k
                    alpha = opacity
                }
                .drawBehind {
                    inDesignPx {
                        ovalGradient(70f, 220f, 230f, 380f, brushes.bloom, alpha = p * bloom)
                    }
                },
        ) {
            SignBody(name, tint, focused, p, signOn, brushes, strokes)
        }
    }
}

@Composable
private fun SignBody(
    name: String,
    tint: Color,
    focused: Boolean,
    p: Float,
    signOn: State<Float>,
    brushes: SignBrushes,
    strokes: SignStrokes,
) {
    val shape = RoundedCornerShape((BODY_RADIUS).dpx)
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (focused) CyberpunkKeyframes.signOn(signOn.value) else 1f }
            .drawBehind {
                inDesignPx {
                    softRect(0f, 0f, BODY_W, BODY_H, tint.copy(alpha = 0.55f), blur = 44f, corner = BODY_RADIUS, alpha = p)
                    softRect(0f, 0f, BODY_W, BODY_H, tint, blur = 14f, corner = BODY_RADIUS, alpha = p)
                    softRect(0f, 16f, BODY_W, BODY_H, CyberpunkPalette.Black.copy(alpha = 0.5f), blur = 30f, corner = BODY_RADIUS, alpha = 1f - p)
                }
            }
            .clip(shape)
            .background(brushes.body)
            .drawBehind {
                inDesignPx {
                    innerGlow(0f, 0f, BODY_W, BODY_H, tint.copy(alpha = 0.35f), strokes.bodyGlow, BODY_RADIUS, alpha = p)
                    drawRoundRect(
                        lerp(tint.copy(alpha = 0.28f), tint, p),
                        Offset(1f, 1f),
                        Size(BODY_W - 2f, BODY_H - 2f),
                        CornerRadius(BODY_RADIUS - 1f, BODY_RADIUS - 1f),
                        style = strokes.border,
                    )
                    // Tube: inset 9 px inside the 2 px border.
                    drawRoundRect(
                        tint.copy(alpha = 0.14f + (0.6f - 0.14f) * p),
                        Offset(11.5f, 11.5f),
                        Size(BODY_W - 23f, BODY_H - 23f),
                        CornerRadius(12.5f, 12.5f),
                        style = strokes.hairline,
                    )
                    drawCircle(CyberpunkPalette.SignScrew, 3f, Offset(17f, 15f))
                    drawCircle(CyberpunkPalette.SignScrew, 3f, Offset(BODY_W - 17f, 15f))
                }
            },
    ) {
        SignBadge(name.take(1), tint, p, strokes, Modifier.align(Alignment.TopCenter).offset(y = 28.dpx))
        SignName(
            label = signLabel(name).uppercase(),
            tint = tint,
            p = p,
            modifier = Modifier
                .offset(y = 124.dpx)
                .fillMaxWidth()
                .height((BODY_H - 124f - 20f).dpx),
        )
    }
}

@Composable
private fun SignBadge(initial: String, tint: Color, p: Float, strokes: SignStrokes, modifier: Modifier) {
    Box(
        modifier
            .size(78.dpx)
            .drawBehind {
                inDesignPx {
                    softRect(0f, 0f, 78f, 78f, tint.copy(alpha = 0.7f), blur = 16f, corner = 39f, alpha = p)
                    innerGlow(0f, 0f, 78f, 78f, tint.copy(alpha = 0.5f), strokes.badgeGlow, 39f, alpha = p)
                    drawCircle(lerp(tint.copy(alpha = 0.3f), tint, p), 38f, Offset(39f, 39f), style = strokes.border)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        CyberpunkGlowText(
            initial,
            TextStyle(
                color = lerp(tint.copy(alpha = 0.55f), CyberpunkPalette.White, p),
                fontSize = 36.spx,
                fontWeight = FontWeight.Bold,
                fontFamily = CyberpunkFonts.Display,
                textAlign = TextAlign.Center,
            ),
            glows = listOf(tint.copy(alpha = p) to 6f, tint.copy(alpha = p) to 18f),
        )
    }
}

/** `writing-mode: vertical-rl; text-orientation: upright`: one glyph per slot, centred. */
@Composable
private fun SignName(label: String, tint: Color, p: Float, modifier: Modifier) {
    val style = TextStyle(
        color = lerp(tint.copy(alpha = 0.5f), CyberpunkPalette.White, p),
        fontSize = NAME_FONT_PX.spx,
        fontWeight = FontWeight.SemiBold,
        fontFamily = CyberpunkFonts.Display,
        letterSpacing = 0.em,
        textAlign = TextAlign.Center,
    )
    val glows = listOf(
        tint.copy(alpha = p) to 4f,
        tint.copy(alpha = p) to 14f,
        tint.copy(alpha = 0.7f * p) to 30f,
    )
    Box(modifier, contentAlignment = Alignment.Center) {
        Layout(
            content = { label.forEach { CyberpunkGlowText(it.toString(), style, glows) } },
        ) { measurables, _ ->
            val placeables = measurables.map { it.measure(Constraints()) }
            val step = NAME_ADVANCE_PX.dpx.toPx()
            val width = placeables.maxOfOrNull { it.width } ?: 0
            val height = (step * placeables.size).roundToInt()
            layout(width, height) {
                placeables.forEachIndexed { i, glyph ->
                    glyph.place((width - glyph.width) / 2, (i * step + step / 2f - glyph.height / 2f).roundToInt())
                }
            }
        }
    }
}

/** Design label: names longer than 10 characters keep 8, trimmed, plus "...". */
private fun signLabel(name: String): String =
    if (name.length > 10) name.take(8).trimEnd() + "..." else name

private class SignBrushes(tint: Color) {
    val spill = Brush.radialGradient(
        0f to tint.copy(alpha = 0.5f),
        0.7f to tint.copy(alpha = 0f),
        center = Offset(130f, 783f),
        radius = 190f * FARTHEST_CORNER,
    )
    val bloom = Brush.radialGradient(
        0f to tint.copy(alpha = 0.38f),
        0.4f to tint.copy(alpha = 0.1f),
        0.68f to tint.copy(alpha = 0f),
        center = Offset(70f, 220f),
        radius = 230f * FARTHEST_CORNER,
    )
    val reflect = Brush.verticalGradient(listOf(tint, tint.copy(alpha = 0f)), startY = 794f, endY = 1074f)
    val pole = Brush.horizontalGradient(
        listOf(CyberpunkPalette.PoleEdge, CyberpunkPalette.PoleCenter, CyberpunkPalette.PoleEdge),
        startX = 126f,
        endX = 134f,
    )
    val body = Brush.verticalGradient(listOf(CyberpunkPalette.SignBodyTop, CyberpunkPalette.SignBodyBottom))
}

private class SignStrokes {
    val border = Stroke(width = 2f)
    val hairline = Stroke(width = 1f)
    val bodyGlow = Stroke(width = 26f / INNER_GLOW_STEPS)
    val badgeGlow = Stroke(width = 12f / INNER_GLOW_STEPS)
}
