package co.jp.tv.themes.gotham

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** `brightness(0.62)` of an unfocused card, painted as a black veil of 1 - 0.62. */
private const val DimVeil = 0.38f

/** Layered rounded rects standing in for CSS `drop-shadow`. */
private const val ShadowSteps = 6

/** `border-radius: 50%` on the name stamp: an ellipse, not a capsule. */
private val EllipseShape = GenericShape { size, _ -> addOval(Rect(Offset.Zero, size)) }

private val PhotoFill = Brush.verticalGradient(listOf(GothamPalette.PhotoTop, GothamPalette.PhotoFoot))
private val SilhouetteShoulders = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

/** Paper fill direction: `linear-gradient(170deg, ...)`. */
private const val PaperAngleRad = 170.0 * PI / 180.0

/**
 * One pinned case file (260x360 design px) for the app at [index]. It is lifted, enlarged and
 * stamped when [focusedIndex] points at it; the others are scattered, tilted and dimmed by
 * their index exactly as the artboard derives them.
 */
@Composable
internal fun GothamCaseFile(name: String, index: Int, focusedIndex: Int, modifier: Modifier = Modifier) {
    val on = index == focusedIndex
    val distance = abs(index - focusedIndex)
    val spec = tween<Float>(GothamMotion.FocusMs, easing = GothamMotion.Focus)
    val lift by animateFloatAsState(if (on) -13f else GothamPalette.dropPx(index) / 2f, spec, label = "lift")
    val scale by animateFloatAsState(if (on) 1.2f else 0.94f, spec, label = "scale")
    val tilt by animateFloatAsState(if (on) 0f else GothamPalette.tiltDegrees(index), spec, label = "tilt")
    val opacity by animateFloatAsState(
        when {
            on -> 1f
            distance == 1 -> 0.85f
            else -> 0.7f
        },
        spec,
        label = "opacity",
    )
    val dim by animateFloatAsState(if (on) 0f else DimVeil, spec, label = "dim")
    val stamp by animateColorAsState(
        if (on) GothamPalette.Stamp else GothamPalette.Stamp.copy(alpha = 0f),
        tween(GothamMotion.StampMs, easing = GothamMotion.Focus),
        label = "stamp",
    )
    val number = (index + 1).toString().padStart(2, '0')

    Box(modifier.size(width = 130.dp, height = 180.dp)) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    translationY = lift.dp.toPx()
                    scaleX = scale
                    scaleY = scale
                    rotationZ = tilt
                    alpha = opacity
                },
        ) {
            Column(
                Modifier
                    .matchParentSize()
                    .paper(dim = { dim })
                    .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                CaseHeader(number)
                CasePhoto()
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        name,
                        modifier = Modifier
                            .graphicsLayer { rotationZ = -3f }
                            .border(1.5.dp, stamp, EllipseShape)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        style = TextStyle(
                            color = GothamPalette.PaperInk,
                            fontSize = if (name.length > 9) 13.5.sp else 17.sp,
                            lineHeight = if (name.length > 9) 14.85.sp else 18.7.sp,
                            fontFamily = JpFonts.Mono,
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.5.dp)) {
                    Box(Modifier.fillMaxWidth(0.92f).height(4.5.dp).background(GothamPalette.Redaction))
                    Box(Modifier.fillMaxWidth(0.64f).height(4.5.dp).background(GothamPalette.Redaction))
                }
            }
            CasePin(
                dim = { dim },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4.5).dp)
                    .size(10.dp),
            )
        }
    }
}

@Composable
private fun CaseHeader(number: String) {
    val style = TextStyle(
        color = GothamPalette.PaperInk,
        fontSize = 7.5.sp,
        fontFamily = JpFonts.Mono,
        letterSpacing = 0.12.em,
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        BasicText(stringResource(R.string.theme_gotham_case_number, number), style = style, maxLines = 1)
        BasicText(stringResource(R.string.theme_gotham_case_file), style = style, maxLines = 1)
    }
}

/** The mugshot: a framed grey photo with a head-and-shoulders silhouette. */
@Composable
private fun CasePhoto() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(59.dp)
            .background(PhotoFill)
            .border(1.5.dp, GothamPalette.PaperInk)
            .padding(1.5.dp)
            .clipToBounds(),
    ) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 11.dp)
                .size(23.dp)
                .background(GothamPalette.Silhouette, CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 37.dp)
                .size(width = 56.dp, height = 45.dp)
                .background(GothamPalette.Silhouette, SilhouetteShoulders),
        )
    }
}

/** Red push-pin: `radial-gradient(circle at 35% 30%, ...)` with a small drop shadow. */
@Composable
private fun CasePin(dim: () -> Float, modifier: Modifier = Modifier) {
    Spacer(
        modifier.drawWithCache {
            val r = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val origin = Offset(size.width * 0.35f, size.height * 0.30f)
            val shadowCenter = Offset(center.x, center.y + r * 0.4f)
            val pin = Brush.radialGradient(
                0f to GothamPalette.PinLight,
                0.55f to GothamPalette.PinRed,
                1f to GothamPalette.PinDark,
                center = origin,
                // CSS sizes it to the farthest corner, the bottom-right one.
                radius = hypot(size.width - origin.x, size.height - origin.y),
            )
            onDrawBehind {
                drawCircle(GothamPalette.Shadow, radius = r, center = shadowCenter, alpha = 0.3f)
                drawCircle(pin, radius = r, center = center)
                drawCircle(GothamPalette.Shadow, radius = r, center = center, alpha = dim())
            }
        },
    )
}

/**
 * Paper body: drop shadow behind, 170deg gradient, the content, then the dimming veil.
 * Shadow goes from `0 14px 14px rgba(0,0,0,0.5)` to `0 30px 28px rgba(0,0,0,0.6)` with focus.
 */
private fun Modifier.paper(dim: () -> Float): Modifier = drawWithCache {
    val dx = sin(PaperAngleRad).toFloat()
    val dy = -cos(PaperAngleRad).toFloat()
    val length = abs(size.width * dx) + abs(size.height * dy)
    val mid = Offset(size.width / 2f, size.height / 2f)
    val half = Offset(dx * length / 2f, dy * length / 2f)
    val fill = Brush.linearGradient(
        listOf(GothamPalette.PaperLight, GothamPalette.PaperDark),
        start = mid - half,
        end = mid + half,
    )
    val corner = CornerRadius(2.dp.toPx())
    val designPx = 0.5.dp.toPx()
    onDrawWithContent {
        val veil = dim()
        val focus = 1f - veil / DimVeil
        val shadowY = (14f + 16f * focus) * designPx
        val blur = (14f + 14f * focus) * designPx
        val shadowAlpha = 0.5f + 0.1f * focus
        for (step in 1..ShadowSteps) {
            val grow = blur * (step.toFloat() / ShadowSteps - 0.5f)
            drawRoundRect(
                GothamPalette.Shadow,
                topLeft = Offset(-grow, shadowY - grow),
                size = Size(size.width + 2 * grow, size.height + 2 * grow),
                cornerRadius = CornerRadius(corner.x + grow.coerceAtLeast(0f)),
                alpha = shadowAlpha / ShadowSteps,
            )
        }
        drawRoundRect(fill, cornerRadius = corner)
        drawContent()
        drawRoundRect(GothamPalette.Shadow, cornerRadius = corner, alpha = veil)
    }
}
