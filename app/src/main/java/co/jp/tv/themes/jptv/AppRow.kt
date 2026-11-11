package co.jp.tv.themes.jptv

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.LocalPaused

/**
 * One rail row. Focus is eased (350 ms): scale 1.03, dark glass fill, accent ring, and a soft
 * accent glow that breathes on a 4 s cycle while focused and not paused.
 *
 * @param riseDelayMs entrance delay, or null when the row should appear immediately.
 */
@Composable
internal fun AppRow(
    app: TvApp,
    focused: Boolean,
    accent: Color,
    riseDelayMs: Int?,
    modifier: Modifier = Modifier,
) {
    val focus by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(JpMotion.FocusMs, easing = JpMotion.Entrance),
        label = "rowFocus",
    )

    val paused = LocalPaused.current
    val breath = remember { Animatable(0f) }
    LaunchedEffect(focused, paused) {
        if (!focused || paused) return@LaunchedEffect
        while (true) {
            breath.animateTo(1f, tween(JpMotion.BreathHalfCycleMs, easing = JpMotion.InOut))
            breath.animateTo(0f, tween(JpMotion.BreathHalfCycleMs, easing = JpMotion.InOut))
        }
    }

    Row(
        modifier = modifier
            .zIndex(if (focused) 1f else 0f)
            .riseIn(delayMs = riseDelayMs ?: 0, distanceDp = 12f, enabled = riseDelayMs != null)
            .fillMaxWidth()
            .height(43.dp)
            .graphicsLayer {
                val s = 1f + 0.03f * focus
                scaleX = s
                scaleY = s
            }
            .glow(accent) { focus * (0.65f + 0.35f * breath.value) }
            .drawBehind {
                if (focus > 0f) {
                    drawRoundRect(
                        JpColors.FocusFill, cornerRadius = CornerRadius(JpDimens.RowRadius.toPx()),
                        alpha = focus,
                    )
                    drawRoundRect(
                        accent.copy(alpha = 0.7f * focus),
                        topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx()),
                        cornerRadius = CornerRadius(JpDimens.RowRadius.toPx()),
                        style = Stroke(JpDimens.Hairline.toPx()),
                    )
                }
            }
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        AppTile(app, focus, accent)
        BasicText(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = lerp(JpColors.Muted, JpColors.Paper, focus),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = JpFonts.Body,
            ),
        )
    }
}

/** Soft glow below a focused row (design: 0 18px 52px accent @ 0.3), drawn as an ellipse gradient. */
private fun Modifier.glow(accent: Color, strength: () -> Float): Modifier = drawWithCache {
    val spread = 26.dp.toPx()
    val rx = size.width / 2f + spread
    val ry = size.height / 2f + spread
    val center = Offset(size.width / 2f, size.height / 2f + 9.dp.toPx())
    val brush = Brush.radialGradient(
        0f to accent.copy(alpha = 0.30f),
        0.6f to accent.copy(alpha = 0.14f),
        1f to accent.copy(alpha = 0f),
        center = center,
        radius = rx,
    )
    onDrawBehind {
        val a = strength()
        if (a > 0.01f) {
            scale(1f, ry / rx, pivot = center) { drawCircle(brush, radius = rx, center = center, alpha = a) }
        }
    }
}

@Composable
private fun AppTile(app: TvApp, focus: Float, accent: Color) {
    val icons = LocalAppIcons.current
    val icon by produceState<ImageBitmap?>(null, app.packageName) { value = icons.load(app) }
    Box(
        modifier = Modifier
            .size(27.dp)
            .clip(JpDimens.IconShape)
            .background(lerp(JpColors.TileFill, accent, focus))
            .border(JpDimens.Hairline, JpColors.Line.copy(alpha = JpColors.Line.alpha * (1f - focus)), JpDimens.IconShape),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = icon
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(21.dp))
        } else {
            BasicText(
                text = initials(app.label),
                style = TextStyle(
                    color = lerp(JpColors.Paper, JpColors.Ink, focus),
                    fontSize = 7.5.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.06.em,
                ),
            )
        }
    }
}

internal fun initials(label: String): String =
    label.filter { it.isLetter() }.take(2).uppercase()
