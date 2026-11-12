package co.jp.tv.themes.oceano

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.jp.tv.R
import co.jp.tv.core.design.JpMotion
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

/**
 * Océano: a coral reef seen from just under the surface as pixel art, one jelly per app. The whole scene
 * is one 320x180 bitmap scaled up with nearest-neighbour (see [OceanoPixelScene]); only the text is drawn at
 * screen resolution. Left and right move the focus and the row of jellies slides to keep it centred; the
 * scene is a day or a night (from [DayPhase]) and dithers from one to the other over 2.5 s when the hour
 * crosses 06:00 or 18:00. The sky above it follows the weather (see [OceanoWeather]).
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
        val nightFade = animateFloatAsState(
            if (DayPhase.of(state.now).isNight) 1f else 0f,
            tween(OceanoMotion.PhaseFadeMs, easing = OceanoMotion.PhaseFade),
            label = "oceanoNight",
        )
        // The sky follows the reading's condition; it only changes when the condition does, not on every tick.
        val oceanoWeather = OceanoWeather.of(state.weather?.condition)
        val focusedIndex = state.focusedIndex.coerceIn(0, (state.apps.size - 1).coerceAtLeast(0))
        // Focus moves settle like every other theme's; the jellies snap to the pixel grid on the way.
        val scroll = animateFloatAsState(
            focusedIndex.toFloat(),
            tween(JpMotion.NavMs, easing = JpMotion.Entrance),
            label = "oceanoScroll",
        )
        val appsDescription = stringResource(R.string.theme_oceano_apps)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                    forward = { it == Key.DirectionRight },
                    backward = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            val pxPerPixel = constraints.maxWidth / OceanoPixels.W.toFloat()
            OceanoPixelView(
                clock = clock,
                night = remember(nightFade) { { nightFade.value } },
                scroll = remember(scroll) { { scroll.value } },
                focusedIndex = focusedIndex,
                apps = state.apps,
                weather = oceanoWeather,
            )
            OceanoJellyLabels(
                apps = state.apps,
                focusedIndex = focusedIndex,
                scroll = scroll,
                pxPerPixel = pxPerPixel,
                modifier = Modifier.semantics { contentDescription = appsDescription },
            )
            OceanoBrand(state.brand, Modifier.align(Alignment.TopStart).padding(start = OceanoDesign.dp(96f), top = OceanoDesign.dp(60f)))
            OceanoTopRight(
                state,
                actions,
                focus,
                Modifier.align(Alignment.TopEnd).padding(end = OceanoDesign.dp(96f), top = OceanoDesign.dp(48f)),
            )
            OceanoPosition(
                focusedIndex,
                state.apps.size,
                Modifier.align(Alignment.BottomStart).padding(start = OceanoDesign.dp(1420f), bottom = OceanoDesign.dp(46f)),
            )
        }
    }

    /** The reef under the sky with no jellies, nothing to focus and no text: the screensaver's scene. */
    @Composable
    override fun Ambient(state: HomeState) {
        val clock = rememberOceanoClock()
        val nightFade = animateFloatAsState(
            if (DayPhase.of(state.now).isNight) 1f else 0f,
            tween(OceanoMotion.PhaseFadeMs, easing = OceanoMotion.PhaseFade),
            label = "oceanoAmbientNight",
        )
        OceanoPixelView(
            clock = clock,
            night = remember(nightFade) { { nightFade.value } },
            scroll = NoScroll,
            focusedIndex = 0,
            apps = emptyList(),
            weather = OceanoWeather.of(state.weather?.condition),
        )
    }
}

private val NoScroll: () -> Float = { 0f }
