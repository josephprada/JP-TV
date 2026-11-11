package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.jp.tv.apps.domain.TvApp
import kotlin.math.abs
import kotlin.math.roundToInt

/** Signs rendered on each side of the one centred under the track position. */
private const val SIGN_WINDOW = 5

/**
 * The street of neon signs, one per app, on a track centred on the focused app
 * (`translateX(960 - (focus * 260 + 130))`, 0.6s), faded at both screen edges.
 */
@Composable
internal fun CyberpunkSigns(
    apps: List<TvApp>,
    focusedIndex: Int,
    phase: CyberpunkPalette.Phase,
    modifier: Modifier = Modifier,
) {
    val track by animateFloatAsState(
        DESIGN_WIDTH / 2f - (focusedIndex * CyberpunkMotion.STEP + CyberpunkMotion.STEP / 2f),
        tween(CyberpunkMotion.FocusMs, easing = CyberpunkMotion.Ease),
        label = "signTrack",
    )
    val signOn = remember { Animatable(0f) }
    LaunchedEffect(focusedIndex) {
        signOn.snapTo(0f)
        signOn.animateTo(1f, tween(CyberpunkMotion.SignOnMs, easing = LinearEasing))
    }
    val window by remember(apps.size) {
        derivedStateOf {
            val centre = ((DESIGN_WIDTH / 2f - CyberpunkMotion.STEP / 2f - track) / CyberpunkMotion.STEP).roundToInt()
            (centre - SIGN_WINDOW).coerceAtLeast(0)..(centre + SIGN_WINDOW).coerceAtMost(apps.size - 1)
        }
    }
    val edgeMask = remember {
        Brush.horizontalGradient(
            0f to Color.Transparent,
            0.09f to Color.Black,
            0.91f to Color.Black,
            1f to Color.Transparent,
        )
    }
    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(edgeMask, blendMode = BlendMode.DstIn)
            },
    ) {
        for (i in window) {
            key(apps[i].packageName, i) {
                CyberpunkSign(
                    name = apps[i].label,
                    tint = CyberpunkPalette.AppTints[i % CyberpunkPalette.AppTints.size],
                    focused = i == focusedIndex,
                    distance = abs(i - focusedIndex),
                    bloom = phase.bloom,
                    signOn = signOn.asState(),
                    modifier = Modifier.offset {
                        IntOffset(((track + i * CyberpunkMotion.STEP) * 0.5.dp.toPx()).roundToInt(), 0)
                    },
                )
            }
        }
    }
}
