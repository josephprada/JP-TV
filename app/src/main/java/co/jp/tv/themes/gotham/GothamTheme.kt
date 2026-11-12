package co.jp.tv.themes.gotham

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

private fun skyBrush(look: GothamLook) = Brush.verticalGradient(
    0f to look.skyTop,
    0.55f to look.skyMid,
    1f to look.skyBottom,
)

private val NightSky = skyBrush(GothamLook.Night)
private val DaySky = skyBrush(GothamLook.Day)

/** Both phases' cities, each generated the first time its phase is shown (geometry is shared, colours differ). */
private class GothamCities {
    private val night by lazy(LazyThreadSafetyMode.NONE) { GothamCity(GothamLook.Night) }
    private val day by lazy(LazyThreadSafetyMode.NONE) { GothamCity(GothamLook.Day) }

    fun of(phase: DayPhase): GothamCity = if (phase.isNight) night else day
}

/**
 * "Gotham": a rainy night skyline with a rooftop searchlight projecting the JP monogram on the
 * clouds; apps are pinned case files on a horizontal strip. Matches design/project/ThemeGotham.dc.html.
 * Follows [DayPhase]: by day the same city sits under an overcast grey-blue sky with the searchlight off
 * and dark windows, dissolving from one to the other when the hour crosses 06:00 or 18:00. The weather
 * drives the rain, clouds, fog veil and lightning ([GothamSky]).
 */
object GothamTheme : LauncherTheme {
    override val id = "gotham"
    override val displayName = "Gotham"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val cities = remember { GothamCities() }
        val phase = DayPhase.of(state.now)
        // The sky follows the reading's condition only, so temperature and clock ticks never rebuild it.
        val sky = remember(state.weather?.condition) { GothamSky.of(state.weather?.condition) }
        val apps = state.apps
        val focused = state.focusedIndex
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NightSky)
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                    toThemes = { it == Key.DirectionRight },
                    fromThemes = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(apps, focused, actions, ThemeNavAxis.Horizontal),
        ) {
            Crossfade(
                targetState = phase,
                animationSpec = tween(GothamMotion.PhaseFadeMs, easing = LinearEasing),
                label = "gothamPhase",
            ) { shown ->
                val look = GothamLook.of(shown)
                Box(Modifier.fillMaxSize().background(if (shown.isNight) NightSky else DaySky)) {
                    GothamTimeline { time -> GothamScene(cities.of(shown), look, sky, time) }
                }
            }
            GothamCaseFiles(apps, focused, Modifier.align(Alignment.TopStart))
            if (apps.isNotEmpty()) {
                GothamArrowButton(
                    glyph = GothamGlyph.ChevronLeft,
                    description = stringResource(R.string.theme_gotham_previous_app),
                    onClick = { actions.focus((focused - 1).coerceAtLeast(0)) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 32.dp, y = 260.dp),
                )
                GothamArrowButton(
                    glyph = GothamGlyph.ChevronRight,
                    description = stringResource(R.string.theme_gotham_next_app),
                    onClick = { actions.focus((focused + 1).coerceAtMost(apps.lastIndex)) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 32.dp)
                        .offset(y = 260.dp),
                )
            }
            val look = GothamLook.of(phase)
            GothamTimeline { time -> GothamRain(cities.of(phase).rain, time, amount = sky.rain(phase.isNight), color = look.rain) }
            BasicText(
                state.brand,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 40.dp, y = 26.dp)
                    .riseIn(delayMs = 0, durationMs = GothamMotion.EntranceMs, distanceDp = GothamMotion.EntranceRiseDp),
                style = TextStyle(
                    color = GothamPalette.Text,
                    fontSize = 34.sp,
                    lineHeight = 34.sp,
                    fontFamily = JpFonts.BebasNeue,
                    letterSpacing = 0.14.em,
                ),
                maxLines = 1,
            )
            GothamTopBar(
                network = state.network,
                weather = state.weather,
                clock = ClockFormat.time(state.now, state.config.hourFormat),
                actions = actions,
                focus = focus,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 40.dp)
                    .riseIn(
                        delayMs = GothamMotion.TopBarDelayMs,
                        durationMs = GothamMotion.EntranceMs,
                        distanceDp = GothamMotion.EntranceRiseDp,
                    ),
            )
            if (apps.isNotEmpty()) {
                BasicText(
                    "${twoDigits(focused.coerceIn(0, apps.lastIndex) + 1)} / ${twoDigits(apps.size)}",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 40.dp, bottom = 22.dp),
                    style = TextStyle(
                        color = GothamPalette.Text.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontFamily = JpFonts.SpecialElite,
                        letterSpacing = 0.2.em,
                    ),
                )
            }
        }
    }
}

private fun twoDigits(n: Int): String = n.toString().padStart(2, '0')
