package co.jp.tv.themes.oceano

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.jp.tv.R
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

/**
 * Océano: a coral reef seen from just under the surface, one moon jelly per app. Left and right
 * move the focus and the row of jellies slides to keep it centred; the scene is a day or a night
 * (from [DayPhase]) and crossfades between them over 2.5 s when the hour crosses 06:00 or 18:00.
 */
object OceanoTheme : LauncherTheme {
    override val id = "oceano"
    override val displayName = "Océano"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val clock = rememberOceanoClock()
        val water = remember {
            Brush.verticalGradient(
                0f to OceanoPalette.WaterSurface,
                0.14f to OceanoPalette.WaterShallow,
                0.36f to OceanoPalette.WaterMid,
                0.62f to OceanoPalette.WaterDeep,
                1f to OceanoPalette.WaterAbyss,
            )
        }
        val nightFade = animateFloatAsState(
            if (DayPhase.of(state.now).isNight) 1f else 0f,
            tween(OceanoMotion.PhaseFadeMs, easing = OceanoMotion.PhaseFade),
            label = "oceanoNight",
        )
        val night = remember(nightFade) { { nightFade.value } }
        val focusedIndex = state.focusedIndex.coerceIn(0, (state.apps.size - 1).coerceAtLeast(0))
        val scroll = animateFloatAsState(
            focusedIndex.toFloat(),
            tween(OceanoMotion.ScrollMs, easing = OceanoMotion.Scroll),
            label = "oceanoScroll",
        )
        val appsDescription = stringResource(R.string.theme_oceano_apps)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(water)
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            OceanoScene(clock, night)
            OceanoJellyTrack(
                apps = state.apps,
                focusedIndex = focusedIndex,
                scroll = scroll,
                clock = clock,
                night = night,
                modifier = Modifier.semantics { contentDescription = appsDescription },
            )
            OceanoFrontMotes(clock)
            OceanoBrand(state.brand, Modifier.align(Alignment.TopStart).padding(start = OceanoDesign.dp(96f), top = OceanoDesign.dp(60f)))
            OceanoTopRight(
                state,
                actions,
                focus,
                Modifier.align(Alignment.TopEnd).padding(end = OceanoDesign.dp(96f), top = OceanoDesign.dp(48f)),
            )
            OceanoScrubber(
                count = state.apps.size,
                scroll = scroll,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = OceanoDesign.dp(560f), bottom = OceanoDesign.dp(40f)),
            )
            OceanoPosition(
                focusedIndex,
                state.apps.size,
                Modifier.align(Alignment.BottomStart).padding(start = OceanoDesign.dp(1420f), bottom = OceanoDesign.dp(54f)),
            )
        }
    }
}
