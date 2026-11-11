package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

/**
 * Rainy neon city (design: ThemeCyberpunk.dc.html). Apps are neon signs along a wet street,
 * Left/Right moves along them; Up reaches the settings control, Down returns. The scene follows
 * [DayPhase]: dusk-blue day palette from 06:00, neon night from 18:00.
 */
object CyberpunkTheme : LauncherTheme {
    override val id = "cyberpunk"
    override val displayName = "Cyberpunk"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val city = remember { CyberpunkCity.generate() }
        val phase = if (DayPhase.of(state.now).isNight) CyberpunkPalette.Night else CyberpunkPalette.Day
        val count = state.apps.size
        val focused = if (count == 0) 0 else state.focusedIndex.coerceIn(0, count - 1)
        val time = rememberSceneTime()
        // The design's 10 apps shift the city by up to 9 steps; longer lists are normalized to that range.
        val parallaxIndex = if (count > CyberpunkMotion.DESIGN_APP_COUNT) {
            focused * (CyberpunkMotion.DESIGN_APP_COUNT - 1f) / (count - 1)
        } else {
            focused.toFloat()
        }
        val farShift = animateFloatAsState(
            -parallaxIndex * CyberpunkMotion.FAR_SHIFT,
            tween(CyberpunkMotion.ParallaxMs, easing = CyberpunkMotion.Ease),
            label = "farParallax",
        )
        val midShift = animateFloatAsState(
            -parallaxIndex * CyberpunkMotion.MID_SHIFT,
            tween(CyberpunkMotion.ParallaxMs, easing = CyberpunkMotion.Ease),
            label = "midParallax",
        )
        val selected = state.apps.getOrNull(focused)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            CyberpunkSkyline(city, phase, time, farShift, midShift)
            CyberpunkSigns(state.apps, focused, phase)
            CyberpunkWeather(city, phase, time)
            CyberpunkAtmosphere(phase)
            CyberpunkBrand(state.brand, time, Modifier.offset(96.dpx, 52.dpx))
            CyberpunkTopBar(
                state,
                actions,
                focus,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 46.dpx, end = 96.dpx),
            )
            if (selected != null) {
                val tint = CyberpunkPalette.AppTints[focused % CyberpunkPalette.AppTints.size]
                CyberpunkSelection(
                    selected.label,
                    tint,
                    focused,
                    count,
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 96.dpx, bottom = 96.dpx),
                )
            }
            CyberpunkScrubber(
                count,
                focused,
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 96.dpx, bottom = 96.dpx),
            )
        }
    }
}
