package co.jp.tv.themes.lofi

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import co.jp.tv.R
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.DeviceProfile
import co.jp.tv.core.util.LocalPaused
import androidx.compose.ui.platform.LocalContext
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation
import co.jp.tv.wallpaper.rememberFrameMillis

/**
 * Lo-fi room, design/project/ThemeLofi.dc.html. Night from 18:00 to 05:59 ([DayPhase]), day
 * otherwise; `state.now` ticks every second, so the scene changes at 06:00 and 18:00 live.
 */
object LofiTheme : LauncherTheme {
    override val id = "lofi"
    override val displayName = "Lo-fi"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }

        val night = DayPhase.of(state.now).isNight
        // 0 = day, 1 = night; drives the artboard's `transition: opacity 2s ease` elements.
        val fade = animateFloatAsState(
            targetValue = if (night) 1f else 0f,
            animationSpec = tween(LofiMotion.PHASE_FADE_MS, easing = LofiMotion.Ease),
            label = "lofiPhase",
        )
        // One frame clock for every loop (rain, clouds, steam, tail, lamp, blink, grain);
        // it is never started while paused, so all loops stop together.
        val context = LocalContext.current
        val lowEnd = remember { DeviceProfile.isLowEnd(context) }
        val millis = rememberFrameMillis(paused = LocalPaused.current, maxFps = DeviceProfile.wallpaperMaxFps(context))
        val scatter = remember { LofiScatter.create() }
        val room = remember(night) { roomBrush(night) }

        val date = LofiDate.format(
            state.now,
            stringArrayResource(R.array.theme_lofi_days),
            stringArrayResource(R.array.theme_lofi_months),
        )
        val clockLine = stringResource(
            R.string.theme_lofi_clock_line,
            ClockFormat.time(state.now, state.config.hourFormat),
            date,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(room)
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionRight || (it == Key.DirectionDown && state.focusedIndex >= state.apps.lastIndex) },
                    toList = { it == Key.DirectionUp || it == Key.DirectionLeft },
                    toThemes = { it == Key.DirectionRight },
                    fromThemes = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            LofiWindow(night, fade, millis, scatter)
            LofiRoom(night, fade, millis)
            LofiSteam(millis)
            LofiClockLine(clockLine, millis)
            LofiNotebook(state.brand, night, state.apps, state.focusedIndex, Modifier.align(Alignment.TopEnd))
            LofiBottomBar(state.network, actions, focus, Modifier.align(Alignment.BottomCenter))
            LofiFilmGrain(millis, showGrain = !lowEnd)
        }
    }

    private fun roomBrush(night: Boolean): Brush = if (night) {
        Brush.verticalGradient(
            0f to LofiPalette.NightRoomTop,
            0.6f to LofiPalette.NightRoomMid,
            1f to LofiPalette.NightRoomBottom,
        )
    } else {
        Brush.verticalGradient(
            0f to LofiPalette.DayRoomTop,
            0.6f to LofiPalette.DayRoomMid,
            1f to LofiPalette.DayRoomBottom,
        )
    }
}
