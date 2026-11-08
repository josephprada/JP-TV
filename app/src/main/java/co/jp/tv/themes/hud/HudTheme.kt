package co.jp.tv.themes.hud

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.themeKeyNavigation
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object HudTheme : LauncherTheme {
    override val id = "hud"
    override val displayName = "HUD"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        val selected = state.apps.getOrNull(state.focusedIndex)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(HudPalette.BackgroundGlow, HudPalette.Background)))
                .hudGrid()
                .focusRequester(focusRequester)
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            HudRings()
            HudTelemetry(state.network, actions, Modifier.align(Alignment.TopStart).padding(28.dp))
            BasicText(
                stringResource(R.string.theme_hud_target, selected?.label?.uppercase() ?: "—"),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp),
                style = TextStyle(color = HudPalette.Amber, fontSize = 8.sp, fontFamily = JpFonts.Orbitron, letterSpacing = 0.4.em),
            )
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BasicText(
                    ClockFormat.time(state.now),
                    style = TextStyle(color = HudPalette.Core, fontSize = 56.sp, fontFamily = JpFonts.Orbitron, letterSpacing = 0.04.em),
                )
                BasicText(
                    "${ClockFormat.date(state.now)} · ● REC",
                    style = TextStyle(color = HudPalette.Cyan, fontSize = 10.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.3.em),
                )
            }
            HudWheel(state.apps, state.focusedIndex, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun HudTelemetry(network: NetworkStatus, actions: HomeActions, modifier: Modifier = Modifier) {
    val signal = (network.signalLevel.coerceIn(0, 4) * 25)
    Column(
        modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = actions::openNetwork,
            )
            .background(HudPalette.PanelBg)
            .border(1.dp, HudPalette.Cyan.copy(alpha = 0.4f))
            .padding(12.dp),
    ) {
        BasicText("TELEMETRÍA", style = TextStyle(color = HudPalette.Cyan, fontSize = 7.5.sp, fontFamily = JpFonts.Orbitron, letterSpacing = 0.3.em))
        BasicText("SEÑAL  $signal%", style = TextStyle(color = HudPalette.Text, fontSize = 10.sp, fontFamily = JpFonts.Mono))
        Box(Modifier.size(width = 120.dp, height = 3.dp).background(HudPalette.Cyan.copy(alpha = 0.15f))) {
            Box(Modifier.fillMaxSize(fraction = signal / 100f).background(HudPalette.Cyan))
        }
        val ms = network.routerLatencyMs ?: network.internetLatencyMs
        BasicText("PING   ${ms?.let { "$it MS" } ?: "—"}", style = TextStyle(color = HudPalette.Text, fontSize = 10.sp, fontFamily = JpFonts.Mono))
        BasicText("IP     ${network.ip ?: "—"}", style = TextStyle(color = HudPalette.Text, fontSize = 10.sp, fontFamily = JpFonts.Mono))
    }
}

@Composable
private fun HudWheel(apps: List<TvApp>, focusedIndex: Int, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val radius = 210.dp
    val radiusPx = with(density) { radius.toPx() }
    Box(modifier, contentAlignment = Alignment.Center) {
        apps.forEachIndexed { index, app ->
            val count = apps.size.coerceAtLeast(1)
            val angle = (2 * PI * index / count) - (PI / 2)
            val dx = (cos(angle) * radiusPx).roundToInt()
            val dy = (sin(angle) * radiusPx).roundToInt()
            val focused = index == focusedIndex
            BasicText(
                app.label.uppercase(),
                modifier = Modifier
                    .offset { IntOffset(dx, dy) }
                    .graphicsLayer { scaleX = if (focused) 1.15f else 0.9f; scaleY = if (focused) 1.15f else 0.9f }
                    .border(
                        width = if (focused) 1.5.dp else 1.dp,
                        color = if (focused) HudPalette.Cyan else HudPalette.Cyan.copy(alpha = 0.35f),
                        shape = CircleShape,
                    )
                    .background(HudPalette.PanelBg, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = TextStyle(
                    color = if (focused) HudPalette.Core else HudPalette.Text,
                    fontSize = if (focused) 9.sp else 7.5.sp,
                    fontFamily = JpFonts.Orbitron,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HudRings() {
    val paused = LocalPaused.current
    val transition = rememberInfiniteTransition(label = "hudSpin")
    val spin by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(40_000, easing = JpMotion.Entrance), RepeatMode.Restart),
        label = "spin",
    )
    val spinR by transition.animateFloat(
        360f,
        0f,
        infiniteRepeatable(tween(12_000, easing = JpMotion.Entrance), RepeatMode.Restart),
        label = "spinR",
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!paused) {
            Box(Modifier.size(300.dp).graphicsLayer { rotationZ = spin }.border(1.dp, HudPalette.Cyan.copy(alpha = 0.35f), CircleShape))
            Box(Modifier.size(250.dp).graphicsLayer { rotationZ = spinR }.border(1.dp, HudPalette.Cyan.copy(alpha = 0.25f), CircleShape))
        }
        Box(Modifier.size(440.dp).border(1.dp, HudPalette.Cyan.copy(0.12f), CircleShape))
    }
}

private fun Modifier.hudGrid(): Modifier = drawBehind {
    var y = size.height
    val step = 40.dp.toPx()
    while (y > size.height * 0.35f) {
        drawRect(HudPalette.Grid, topLeft = Offset(0f, y), size = Size(size.width, 1f))
        y -= step
    }
}
