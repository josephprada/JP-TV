package co.jp.tv.themes.pixel

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.LocalPaused

private const val BodyWidthDp = 120f
private const val BodyHeightDp = 135f

/** The cartridge outline: a rectangle with notched bottom corners. */
private val CartridgeShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0f, h * 0.04f)
    lineTo(w * 0.03f, 0f)
    lineTo(w * 0.97f, 0f)
    lineTo(w, h * 0.04f)
    lineTo(w, h * 0.90f)
    lineTo(w * 0.93f, h)
    lineTo(w * 0.07f, h)
    lineTo(0f, h * 0.90f)
    close()
}

/** The focused cartridge rocks 1.6 degrees each way (pk-wobble). No transition while idle or paused. */
@Composable
private fun rememberWobble(active: Boolean): State<Float> {
    val paused = LocalPaused.current
    return if (!active || paused) {
        remember { mutableStateOf(0f) }
    } else {
        rememberInfiniteTransition(label = "pixelWobble").animateFloat(
            initialValue = -1.6f,
            targetValue = 1.6f,
            animationSpec = infiniteRepeatable(tween(1600, easing = JpMotion.InOut), RepeatMode.Reverse),
            label = "wobble",
        )
    }
}

/**
 * One cartridge on the shelf. [focused] lifts and enlarges it; [distance] (cartridges away from
 * the focused one) sets how faded it is. Place it with [modifier].
 */
@Composable
internal fun PixelCartridge(
    label: String,
    tint: PixelTint,
    focused: Boolean,
    distance: Int,
    modifier: Modifier = Modifier,
) {
    val spec = tween<Float>(PixelShelfMs, easing = PixelShelfEase)
    val focus by animateFloatAsState(if (focused) 1f else 0f, spec, label = "pixelFocus")
    val opacity by animateFloatAsState(
        when {
            focused -> 1f
            distance == 1 -> 0.92f
            distance == 2 -> 0.8f
            else -> 0.7f
        },
        spec,
        label = "pixelOpacity",
    )
    val wobble by rememberWobble(focused)
    Box(
        modifier
            .size(BodyWidthDp.dp, BodyHeightDp.dp)
            .graphicsLayer {
                val scale = lerp(0.9f, 1.2f, focus)
                scaleX = scale
                scaleY = scale
                translationY = -17.dp.toPx() * focus
                alpha = opacity
                rotationZ = wobble
                shadowElevation = lerp(7.dp.toPx(), 14.dp.toPx(), focus)
                shape = CartridgeShape
                clip = false
                ambientShadowColor = lerp(PixelPalette.DropShadowIdle, tint.top.copy(alpha = 0.6f), focus)
                spotShadowColor = lerp(PixelPalette.DropShadowIdle, PixelPalette.DropShadowFocused, focus)
            }
            .drawWithCache {
                val w = size.width
                val h = size.height
                val body = Path().apply {
                    moveTo(0f, h * 0.04f)
                    lineTo(w * 0.03f, 0f)
                    lineTo(w * 0.97f, 0f)
                    lineTo(w, h * 0.04f)
                    lineTo(w, h * 0.90f)
                    lineTo(w * 0.93f, h)
                    lineTo(w * 0.07f, h)
                    lineTo(0f, h * 0.90f)
                    close()
                }
                val bodyBrush = Brush.verticalGradient(listOf(PixelPalette.BodyTop, PixelPalette.BodyBottom))
                val gripX = 13.dp.toPx()
                val gripTop = 7.dp.toPx()
                val gripW = w - 2f * gripX
                val gripBand = 1.5.dp.toPx()
                val labelLeft = 10.dp.toPx()
                val labelTop = 28.dp.toPx()
                val labelW = w - 2f * labelLeft
                val labelH = 84.dp.toPx()
                val radius = 5.dp.toPx()
                val ring = 2.dp.toPx()
                val labelPath = Path().apply {
                    addRoundRect(RoundRect(labelLeft, labelTop, labelLeft + labelW, labelTop + labelH, radius, radius))
                }
                val labelBrush = cssGradient(
                    160f,
                    Size(labelW, labelH),
                    Offset(labelLeft, labelTop),
                    listOf(0f to tint.top, 1f to tint.bottom),
                )
                val ringStroke = Stroke(width = ring)
                val sheen = cssGradient(
                    105f,
                    Size(w, h),
                    Offset.Zero,
                    listOf(0f to PixelPalette.SheenStart, 0.28f to PixelPalette.SheenEnd),
                )
                onDrawWithContent {
                    clipPath(body) {
                        drawRect(bodyBrush, size = Size(w, h))
                        for (i in 0 until 5) {
                            val top = gripTop + i * 2f * gripBand
                            drawRect(PixelPalette.StripeDark, Offset(gripX, top), Size(gripW, gripBand))
                            drawRect(PixelPalette.StripeLight, Offset(gripX, top + gripBand), Size(gripW, gripBand))
                        }
                        drawPath(labelPath, labelBrush)
                        clipPath(labelPath) {
                            drawRect(PixelPalette.LabelShade, Offset(labelLeft, labelTop + labelH - 7.dp.toPx()), Size(labelW, 7.dp.toPx()))
                        }
                        drawRoundRect(
                            PixelPalette.LabelRing,
                            Offset(labelLeft + ring / 2f, labelTop + ring / 2f),
                            Size(labelW - ring, labelH - ring),
                            CornerRadius(radius - ring / 2f),
                            style = ringStroke,
                        )
                        drawRect(PixelPalette.Pin, Offset(38.dp.toPx(), h - 10.dp.toPx()), Size(w - 76.dp.toPx(), 10.dp.toPx()))
                    }
                    drawContent()
                    clipPath(body) {
                        drawRect(sheen, size = Size(w, h))
                        // brightness(0.8) on cartridges that are not focused
                        drawRect(PixelPalette.DimOverlay.copy(alpha = 0.2f * (1f - focus)), size = Size(w, h))
                    }
                }
            },
    ) {
        Box(
            Modifier
                .offset(10.dp, 28.dp)
                .size(100.dp, 84.dp)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                label.uppercase(),
                style = TextStyle(
                    color = tint.text,
                    fontSize = if (label.length > 9) 13.5.sp else 17.sp,
                    lineHeight = 1.05.em,
                    letterSpacing = 0.01.em,
                    textAlign = TextAlign.Center,
                    fontFamily = JpFonts.Righteous,
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
