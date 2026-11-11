package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp

private const val TRACK_W = 620f
private const val TRACK_H = 48f

/** Bottom-right scrubber: one tick per app, the focused one tall and tinted (0.6s). */
@Composable
internal fun CyberpunkScrubber(count: Int, focusedIndex: Int, modifier: Modifier = Modifier) {
    val ticks: List<State<Float>> = List(count) { i ->
        key(i) {
            animateFloatAsState(
                if (i == focusedIndex) 1f else 0f,
                tween(CyberpunkMotion.FocusMs, easing = CyberpunkMotion.Ease),
                label = "tick",
            )
        }
    }
    val line = remember {
        Brush.horizontalGradient(
            0f to CyberpunkPalette.Cyan.copy(alpha = 0f),
            0.15f to CyberpunkPalette.Cyan.copy(alpha = 0.45f),
            0.85f to CyberpunkPalette.Pink.copy(alpha = 0.45f),
            1f to CyberpunkPalette.Pink.copy(alpha = 0f),
            startX = -30f,
            endX = TRACK_W + 30f,
        )
    }
    Spacer(
        modifier
            .size(TRACK_W.dpx, TRACK_H.dpx)
            .drawBehind {
                inDesignPx {
                    drawRect(line, Offset(-30f, TRACK_H / 2f), Size(TRACK_W + 60f, 1f))
                    val gap = if (count > 1) TRACK_W / (count - 1) else 0f
                    ticks.forEachIndexed { i, state ->
                        val p = state.value
                        val tint = CyberpunkPalette.AppTints[i % CyberpunkPalette.AppTints.size]
                        val w = 2f + 2f * p
                        val h = 10f + 20f * p
                        val left = i * gap - w / 2f
                        val top = TRACK_H / 2f - h / 2f
                        softRect(left, top, w, h, tint, blur = 14f, corner = 2f, alpha = p)
                        softRect(left, top, w, h, CyberpunkPalette.White, blur = 4f, corner = 2f, alpha = p)
                        drawRoundRect(
                            lerp(CyberpunkPalette.Ice.copy(alpha = 0.5f), tint, p),
                            Offset(left, top),
                            Size(w, h),
                            CornerRadius(2f, 2f),
                        )
                    }
                }
            },
    )
}
