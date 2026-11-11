package co.jp.tv.themes.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.riseIn
import co.jp.tv.themes.LauncherTheme

private val IdleFill = Color(0xFF121214).copy(alpha = 0.45f)
private val OnFill = Color(0xFF050506).copy(alpha = 0.7f)
private val IdleBorder = JpColors.Paper.copy(alpha = 0.1f)

/**
 * One theme in the picker. The active theme and the focused card share the "on" look from the artboard
 * (scale 1.02, dark fill, 2 px accent ring, accent glow); only the active theme reads ACTIVO.
 */
@Composable
internal fun ThemeCard(
    theme: LauncherTheme,
    active: Boolean,
    accent: Color,
    delayMs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val on by animateFloatAsState(
        targetValue = if (active || focused) 1f else 0f,
        animationSpec = tween(JpMotion.FocusMs, easing = JpMotion.Entrance),
        label = "themeCardOn",
    )
    Column(
        modifier = modifier
            .riseIn(delayMs = delayMs, distanceDp = 10f)
            .graphicsLayer {
                val s = 1f + 0.02f * on
                scaleX = s
                scaleY = s
            }
            .drawBehind {
                val u = 0.5.dp.toPx()
                val corner = CornerRadius(15.dp.toPx())
                if (on > 0f) {
                    drawGlowRect(Offset(0f, 20f * u), size, corner.x, 60f * u, accent.copy(alpha = 0.3f * on))
                }
                drawRoundRect(lerp(IdleFill, OnFill, on), cornerRadius = corner)
                if (on < 1f) {
                    drawRoundRect(
                        IdleBorder.copy(alpha = IdleBorder.alpha * (1f - on)),
                        topLeft = Offset(u / 2f, u / 2f),
                        size = Size(size.width - u, size.height - u),
                        cornerRadius = corner,
                        style = Stroke(u),
                    )
                }
                if (on > 0f) {
                    // box-shadow 0 0 0 2px: a ring just outside the card.
                    drawRoundRect(
                        accent.copy(alpha = 0.8f * on),
                        topLeft = Offset(-u, -u),
                        size = Size(size.width + 2f * u, size.height + 2f * u),
                        cornerRadius = CornerRadius(corner.x + u),
                        style = Stroke(2f * u),
                    )
                }
            }
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ThemePreview(theme.id)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            BasicText(
                text = theme.displayName,
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(color = JpColors.Paper, fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = JpFonts.Body),
            )
            BasicText(
                text = if (active) stringResource(R.string.themes_active) else stringResource(R.string.themes_try),
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = lerp(JpColors.Faint, accent, on),
                    fontSize = 6.sp,
                    fontFamily = JpFonts.Mono,
                    letterSpacing = 0.14.em,
                ),
            )
        }
        BasicText(
            text = themeBlurb(theme.id),
            style = TextStyle(color = JpColors.Muted, fontSize = 8.sp, lineHeight = 11.6.sp, fontFamily = JpFonts.Body),
        )
    }
}
