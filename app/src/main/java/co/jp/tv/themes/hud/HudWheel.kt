package co.jp.tv.themes.hud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.launchOrigin
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val SlotCount = 8
private const val SlotStepDegrees = 45.0
private const val EnterStaggerMs = 60

/**
 * The app wheel: the focused app always sits at the top and the others follow at 45 degrees each.
 * Up to eight apps are all on the wheel (as in the design); with more, only the eight around the
 * focus are shown (three before it, four after it).
 */
@Composable
internal fun HudWheel(
    apps: List<TvApp>,
    focusedIndex: Int,
    onPick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val radiusPx = with(LocalDensity.current) { 190.dp.toPx() }
    var settled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1_000)
        settled = true
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        apps.forEachIndexed { index, app ->
            val offset = index - focusedIndex
            val visible = apps.size <= SlotCount || offset in -3..4
            if (visible) {
                key(app.packageName) {
                    val angle = (-90.0 + offset * SlotStepDegrees) * PI / 180.0
                    HudNode(
                        label = app.label.uppercase(),
                        target = Offset((radiusPx * cos(angle)).toFloat(), (radiusPx * sin(angle)).toFloat()),
                        focused = index == focusedIndex,
                        enterDelayMs = if (settled) 0 else index.coerceAtMost(SlotCount - 1) * EnterStaggerMs,
                        onClick = { onPick(index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HudNode(
    label: String,
    target: Offset,
    focused: Boolean,
    enterDelayMs: Int,
    onClick: () -> Unit,
) {
    val position by animateOffsetAsState(target, tween(JpMotion.NavMs, easing = JpMotion.Entrance), label = "hudNodePos")
    val background by animateColorAsState(
        if (focused) HudPalette.Cyan else HudPalette.NodeIdle,
        tween(JpMotion.FocusMs),
        label = "hudNodeBg",
    )
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(enterDelayMs.toLong())
        // Nodes revealed by turning the wheel fade in quickly; the first-pass stagger keeps its slower reveal.
        enter.animateTo(1f, tween(if (enterDelayMs == 0) JpMotion.NavMs else 500, easing = JpMotion.Entrance))
    }
    Box(
        modifier = Modifier
            .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
            .size(width = 90.dp, height = 34.dp)
            .launchOrigin(focused)
            .graphicsLayer {
                alpha = enter.value
                val scale = 0.6f + 0.4f * enter.value
                scaleX = scale
                scaleY = scale
            }
            .drawWithCache {
                val w = size.width
                val h = size.height
                val hexagon = Path().apply {
                    moveTo(w * 0.12f, 0f)
                    lineTo(w * 0.88f, 0f)
                    lineTo(w, h * 0.5f)
                    lineTo(w * 0.88f, h)
                    lineTo(w * 0.12f, h)
                    lineTo(0f, h * 0.5f)
                    close()
                }
                onDrawBehind { drawPath(hexagon, background) }
            }
            .focusProperties { canFocus = false }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            label,
            modifier = Modifier.padding(horizontal = 10.dp),
            style = TextStyle(
                color = if (focused) HudPalette.Background else HudPalette.Text,
                fontSize = 8.5.sp,
                fontFamily = JpFonts.Orbitron,
                letterSpacing = 0.12.em,
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
