package co.jp.tv.themes.zen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import java.time.LocalDateTime

object ZenTheme : LauncherTheme {
    override val id = "zen"
    override val displayName = "Zen"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        var menuOpen by remember { mutableStateOf(false) }
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(ZenPalette.BackgroundTop, ZenPalette.BackgroundMid, ZenPalette.BackgroundBottom),
                        radius = 1200f,
                    ),
                )
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { menuOpen && it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                )
                .themeKeyNavigation(
                    apps = state.apps,
                    focusedIndex = state.focusedIndex,
                    actions = actions,
                    axis = ThemeNavAxis.Horizontal,
                    onBeforeLaunch = {
                        if (!menuOpen) {
                            menuOpen = true
                            true
                        } else {
                            false
                        }
                    },
                    onBack = {
                        if (menuOpen) {
                            menuOpen = false
                            focus.list.requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                ),
        ) {
            ZenRipples()
            Column(
                Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                BasicText(
                    ClockFormat.time(state.now),
                    style = TextStyle(
                        color = ZenPalette.Paper,
                        fontSize = 130.sp,
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = JpFonts.Body,
                        letterSpacing = (-0.03).em,
                    ),
                )
                BasicText(
                    ClockFormat.date(state.now),
                    style = TextStyle(
                        color = ZenPalette.PaperSoft,
                        fontSize = 9.sp,
                        fontFamily = JpFonts.Mono,
                        letterSpacing = 0.3.em,
                    ),
                )
            }
            if (menuOpen) {
                ZenAppStrip(state.apps, state.focusedIndex, state.brand, actions, focus, Modifier.align(Alignment.BottomCenter))
            } else {
                BasicText(
                    stringResource(R.string.theme_zen_press_ok),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ZenPalette.Glass)
                        .border(1.dp, ZenPalette.GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    style = TextStyle(
                        color = ZenPalette.Paper,
                        fontSize = 9.sp,
                        fontFamily = JpFonts.Body,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.3.em,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ZenRipples() {
    val paused = LocalPaused.current
    if (paused) return
    val transition = rememberInfiniteTransition(label = "zenRipple")
    val scale1 by transition.animateFloat(
        0.2f,
        2.4f,
        infiniteRepeatable(tween(12_000, easing = JpMotion.Entrance), RepeatMode.Restart),
        label = "r1",
    )
    val alpha1 by transition.animateFloat(
        0.55f,
        0f,
        infiniteRepeatable(tween(12_000, easing = JpMotion.Entrance), RepeatMode.Restart),
        label = "a1",
    )
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(400.dp)
                .graphicsLayer { scaleX = scale1; scaleY = scale1; alpha = alpha1 }
                .border(1.dp, ZenPalette.Ripple, CircleShape),
        )
    }
}

@Composable
private fun ZenAppStrip(
    apps: List<TvApp>,
    focusedIndex: Int,
    brand: String,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(bottom = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            "$brand · [RED WI-FI] · AJUSTES",
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 32.dp, bottom = 12.dp)
                .themeControlTarget(focus)
                .clip(RoundedCornerShape(12.dp))
                .background(if (focus.onControl) ZenPalette.GlassBorder else Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openSettings,
                )
                .padding(horizontal = 10.dp, vertical = 5.dp),
            style = TextStyle(color = if (focus.onControl) ZenPalette.Paper else ZenPalette.PaperSoft, fontSize = 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.24.em),
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(ZenPalette.Glass)
                .border(1.dp, ZenPalette.GlassBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            apps.forEachIndexed { index, app ->
                val focused = index == focusedIndex
                BasicText(
                    app.label,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (focused) ZenPalette.GlassBorder else ZenPalette.Glass)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = TextStyle(
                        color = ZenPalette.Paper,
                        fontSize = 9.sp,
                        fontFamily = JpFonts.Body,
                        fontWeight = if (focused) FontWeight.Medium else FontWeight.Light,
                    ),
                )
            }
        }
    }
}
