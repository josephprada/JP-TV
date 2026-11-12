package co.jp.tv.themes.hud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation
import java.time.LocalDateTime

object HudTheme : LauncherTheme {
    override val id = "hud"
    override val displayName = "HUD"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val selected = state.apps.getOrNull(state.focusedIndex)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hudBackdrop()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            HudScanLine()
            // The reticle, the clock and the wheel share one centre, 20px above the screen centre.
            Box(Modifier.fillMaxSize().offset(y = (-10).dp), contentAlignment = Alignment.Center) {
                HudRings()
                HudClock(state.now, state.config.hourFormat)
                HudWheel(state.apps, state.focusedIndex, onPick = actions::focus, modifier = Modifier.fillMaxSize())
            }
            BasicText(
                stringResource(R.string.theme_hud_target, selected?.label?.uppercase() ?: "—"),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp),
                style = TextStyle(color = HudPalette.Amber, fontSize = 8.sp, fontFamily = JpFonts.Orbitron, letterSpacing = 0.4.em),
            )
            HudTelemetryPanel(state.network, actions, Modifier.align(Alignment.TopStart).padding(28.dp))
            HudUnitPanel(
                state.brand,
                state.apps.size,
                state.network,
                actions,
                focus,
                Modifier.align(Alignment.TopEnd).padding(28.dp),
            )
        }
    }
}

@Composable
private fun HudClock(now: LocalDateTime, hourFormat: ClockFormat.HourFormat) {
    val density = LocalDensity.current
    val glow = Shadow(HudPalette.ClockGlow, Offset.Zero, with(density) { 12.dp.toPx() })
    val parts = ClockFormat.parts(now, hourFormat)
    val days = stringArrayResource(R.array.theme_hud_days)
    val months = stringArrayResource(R.array.theme_hud_months)
    val date = "${days[now.dayOfWeek.value - 1]} · ${"%02d".format(now.dayOfMonth)} ${months[now.monthValue - 1]} · "
    val blink = rememberHudBlink()
    val sub = TextStyle(color = HudPalette.Cyan, fontSize = 10.sp, fontFamily = JpFonts.ShareTechMono, letterSpacing = 0.3.em)
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row {
            BasicText(
                "${parts.hours}:${parts.minutes}",
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = HudPalette.Core,
                    fontSize = 56.sp,
                    fontFamily = JpFonts.Orbitron,
                    letterSpacing = 0.04.em,
                    shadow = glow,
                ),
            )
            // 12-hour clocks show AM/PM as a small suffix so the digits keep the designed size.
            parts.meridiem?.let {
                BasicText(
                    " $it",
                    modifier = Modifier.alignByBaseline(),
                    style = TextStyle(
                        color = HudPalette.Core,
                        fontSize = 20.sp,
                        fontFamily = JpFonts.Orbitron,
                        letterSpacing = 0.04.em,
                        shadow = glow,
                    ),
                )
            }
        }
        Row {
            BasicText(date, style = sub)
            BasicText("●", modifier = Modifier.graphicsLayer { alpha = blink.value }, style = sub)
            BasicText(" ${stringResource(R.string.theme_hud_rec)}", style = sub)
        }
    }
}

/** radial-gradient(80% 70% at 50% 45%, #0a1824 0%, #03070c 70%) */
private fun Modifier.hudBackdrop(): Modifier = drawWithCache {
    val ry = size.height * 0.7f
    val sx = (size.width * 0.8f) / ry
    val center = Offset(size.width * 0.5f, size.height * 0.45f)
    val brush = Brush.radialGradient(
        0f to HudPalette.BackgroundGlow,
        0.7f to HudPalette.Background,
        center = center,
        radius = ry,
    )
    onDrawBehind {
        drawRect(HudPalette.Background)
        scale(sx, 1f, pivot = center) { drawRect(brush, size = size) }
    }
}
