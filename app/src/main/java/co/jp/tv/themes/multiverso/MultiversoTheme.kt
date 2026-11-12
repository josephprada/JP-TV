package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation
import co.jp.tv.wallpaper.rememberFrameMillis

/**
 * "Multiverso": every app is a branching timeline off one braided main line through a starfield.
 * Left/Right moves along the line; Up reaches the Settings control, Down returns to the apps.
 * Matches design/project/ThemeMultiverso.dc.html; no day/night cycle.
 */
object MultiversoTheme : LauncherTheme {
    override val id = "multiverso"
    override val displayName = "Multiverso"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val context = LocalContext.current
        val maxFps = remember { DeviceProfile.wallpaperMaxFps(context) }
        // Every loop (twinkle, nebulae, flows, ripples, rings) reads this clock; it does not tick while paused.
        val frame = rememberFrameMillis(LocalPaused.current, maxFps)
        val focusedIndex = state.focusedIndex.coerceIn(0, (state.apps.size - 1).coerceAtLeast(0))
        val parallax = animateFloatAsState(
            focusedIndex.toFloat(),
            tween(MultiversoMotion.ParallaxMs, easing = MultiversoMotion.Swift),
            label = "multiversoParallax",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MultiversoPalette.BackgroundEdge)
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
            MultiversoSky(frame, parallax)
            MultiversoTimeline(frame)
            MultiversoTrack(state.apps, focusedIndex, frame)
            MultiversoVignette()
            MultiversoHeader(state, actions, focus, Modifier.align(Alignment.TopStart))
            MultiversoScrubber(state.apps.size, focusedIndex)
        }
    }
}

/** `radial-gradient(75% 70% at 50% 54%, transparent 55%, rgba(2,3,16,0.85) 100%)`. */
@Composable
private fun MultiversoVignette() {
    val brush = remember {
        unitRadial(
            0f to MultiversoPalette.Vignette.copy(alpha = 0f),
            0.55f to MultiversoPalette.Vignette.copy(alpha = 0f),
            1f to MultiversoPalette.Vignette,
        )
    }
    Canvas(Modifier.fillMaxSize()) {
        scale(density / 2f, pivot = Offset.Zero) {
            drawEllipseGradient(brush, Offset(960f, 583.2f), 1440f, 756f, fill = true)
        }
    }
}
