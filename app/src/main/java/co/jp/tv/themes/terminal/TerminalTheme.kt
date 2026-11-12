package co.jp.tv.themes.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.launchOrigin
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.RailWindow
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themesControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.ui.weatherLabel
import java.time.LocalDateTime

/** App rows shown at once; the window follows the focus (the design never scrolls: 8 apps). */
private const val VisibleRows = 14

/** Entrance stagger stops growing after this many rows so a long list never waits seconds. */
private const val MaxStaggerRows = 12

private val Whitespace = Regex("\\s+")

object TerminalTheme : LauncherTheme {
    override val id = "terminal"
    override val displayName = "Terminal"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val flicker: State<Float> = rememberTerminalFlicker()
        val glow = rememberTerminalGlow()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = flicker.value }
                .terminalEllipse(
                    listOf(TerminalPalette.BackgroundGlow, TerminalPalette.Background),
                    listOf(0f, 1f),
                    widthFactor = 1.1f,
                    heightFactor = 1f,
                )
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionLeft || (it == Key.DirectionUp && state.focusedIndex <= 0) },
                    toList = { it == Key.DirectionDown },
                    toThemes = { it == Key.DirectionRight },
                    fromThemes = { it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            TerminalInfoPanel(
                state.brand,
                state.apps.size,
                state.network,
                glow,
                Modifier
                    .padding(start = 32.dp, top = 55.dp)
                    .width(430.dp),
            )
            TerminalAppList(
                apps = state.apps,
                focusedIndex = state.focusedIndex,
                glow = glow,
                actions = actions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 500.dp, top = 55.dp, end = 32.dp),
            )
            TerminalStatusBar(state.brand, state.network, state.now, state.config.hourFormat, actions, focus, state.weather)
            TerminalCrtOverlay()
        }
    }
}

@Composable
private fun Modifier.terminalTap(onClick: () -> Unit): Modifier = this
    .focusProperties { canFocus = false }
    .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    )

@Composable
private fun TerminalStatusBar(
    brand: String,
    network: NetworkStatus,
    now: LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    actions: HomeActions,
    focus: ThemeControlFocus,
    weather: Weather? = null,
) {
    val bar = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(TerminalPalette.Green)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText("[${brand.lowercase()}] 0:apps* 1:", style = bar)
            BasicText("red", modifier = Modifier.terminalTap(actions::openNetwork), style = bar)
            BasicText(" 2:", style = bar)
            BasicText(
                stringResource(R.string.cd_settings).lowercase(),
                modifier = Modifier
                    .themeControlTarget(focus)
                    .background(if (focus.onControl) TerminalPalette.BarFg else TerminalPalette.Green)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openSettings,
                    ),
                style = bar.copy(color = if (focus.onControl) TerminalPalette.Green else TerminalPalette.BarFg),
            )
            // Not in the artboard: a third tmux window so the Themes screen is one D-pad step from Settings.
            BasicText(" 3:", style = bar)
            BasicText(
                stringResource(R.string.cd_themes).lowercase(),
                modifier = Modifier
                    .themesControlTarget(focus)
                    .background(if (focus.onThemes) TerminalPalette.BarFg else TerminalPalette.Green)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openThemes,
                    ),
                style = bar.copy(color = if (focus.onThemes) TerminalPalette.Green else TerminalPalette.BarFg),
            )
        }
        val latency = network.routerLatencyMs ?: network.internetLatencyMs
        val net = when {
            !network.isConnected -> stringResource(R.string.theme_terminal_net_down)
            network.transport == NetworkStatus.Transport.ETHERNET -> stringResource(R.string.theme_terminal_net_eth)
            else -> stringResource(R.string.theme_terminal_net_wifi)
        }
        val lat = latency?.let { " · ${it}ms" }.orEmpty()
        val days = stringArrayResource(R.array.theme_terminal_days)
        val months = stringArrayResource(R.array.theme_terminal_months)
        val date = "${days[now.dayOfWeek.value - 1]} ${"%02d".format(now.dayOfMonth)} ${months[now.monthValue - 1]}"
        val wx = weather?.let { " | ${it.temperatureC}° ${weatherLabel(it.condition, it.isDay).uppercase()}${if (it.stale) "?" else ""}" }.orEmpty()
        BasicText("$net$lat | ${ClockFormat.time(now, hourFormat)} $date$wx", style = bar)
    }
}

@Composable
private fun TerminalInfoPanel(
    brand: String,
    count: Int,
    network: NetworkStatus,
    glow: Shadow,
    modifier: Modifier = Modifier,
) {
    val unknown = "—"
    val ssid = network.ssid
        ?: if (network.isConnected) stringResource(R.string.theme_terminal_net_connected)
        else stringResource(R.string.theme_terminal_net_offline)
    val rows = listOf(
        "host" to brand.lowercase(),
        "apps" to stringResource(R.string.theme_terminal_apps_value, count),
        "ip" to (network.ip ?: unknown),
        "dns" to network.dns.joinToString(", ").ifEmpty { unknown },
        "red" to ssid,
        "ping" to NetworkRules.formatLatency(network.routerLatencyMs ?: network.internetLatencyMs),
        "tema" to stringResource(R.string.theme_terminal_theme_value),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        TerminalBanner(Modifier.padding(bottom = 12.dp))
        rows.forEachIndexed { index, (key, value) ->
            val text = remember(key, value) {
                buildAnnotatedString {
                    withStyle(SpanStyle(color = TerminalPalette.Amber)) { append("$key:".padEnd(7, ' ')) }
                    append(" $value")
                }
            }
            BasicText(
                text,
                modifier = Modifier.terminalLineIn(150 + index * 120),
                style = TextStyle(
                    color = TerminalPalette.Green,
                    fontSize = 16.sp,
                    lineHeight = 1.1.em,
                    fontFamily = JpFonts.Vt323,
                    shadow = glow,
                ),
            )
        }
    }
}

@Composable
private fun TerminalAppList(
    apps: List<TvApp>,
    focusedIndex: Int,
    glow: Shadow,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    val kindTv = stringResource(R.string.theme_terminal_kind_tv)
    val kindApp = stringResource(R.string.theme_terminal_kind_app)
    val slugs = remember(apps) { apps.map { slug(it.label) } }
    val windowTop = remember { IntArray(1) }
    val top = RailWindow.top(windowTop[0], focusedIndex.coerceIn(0, (apps.size - 1).coerceAtLeast(0)), apps.size, VisibleRows)
    windowTop[0] = top
    val shown = apps.size.coerceAtMost(VisibleRows)
    val blink: State<Float> = rememberTerminalBlink()
    val line = TextStyle(fontSize = 20.sp, lineHeight = 1.15.em, fontFamily = JpFonts.Vt323)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        BasicText(
            "jp@tv:~$ ls --apps",
            style = line.copy(color = TerminalPalette.GreenDim, shadow = glow),
        )
        for (index in top until (top + shown).coerceAtMost(apps.size)) {
            val app = apps[index]
            val selected = index == focusedIndex
            TerminalAppRow(
                name = slugs[index],
                kind = if (app.isTvApp) kindTv else kindApp,
                selected = selected,
                delayMs = 900 + index.coerceAtMost(MaxStaggerRows) * 80,
                style = line,
                glow = glow,
                onClick = { actions.focus(index) },
            )
        }
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                "jp@tv:~$ open ${slugs.getOrNull(focusedIndex) ?: "…"}",
                style = line.copy(color = TerminalPalette.Green, shadow = glow),
            )
            Box(
                Modifier
                    .padding(start = 3.dp)
                    .offset(y = 1.dp)
                    .size(width = 10.dp, height = 19.dp)
                    .graphicsLayer { alpha = blink.value }
                    .background(TerminalPalette.Green),
            )
        }
        BasicText(
            stringResource(R.string.theme_terminal_hint),
            style = line.copy(color = TerminalPalette.GreenDim, fontSize = 15.sp, shadow = glow),
        )
    }
}

@Composable
private fun TerminalAppRow(
    name: String,
    kind: String,
    selected: Boolean,
    delayMs: Int,
    style: TextStyle,
    glow: Shadow,
    onClick: () -> Unit,
) {
    val text: AnnotatedString = remember(name, kind, selected) {
        val metaColor: Color = if (selected) TerminalPalette.BarFg else TerminalPalette.GreenDim
        buildAnnotatedString {
            append(if (selected) "> " else "  ")
            append(name)
            withStyle(SpanStyle(color = metaColor)) { append("  [$kind]") }
        }
    }
    BasicText(
        text,
        modifier = Modifier
            .terminalLineIn(delayMs)
            .launchOrigin(selected)
            .terminalTap(onClick)
            .background(if (selected) TerminalPalette.Green else Color.Transparent)
            .padding(horizontal = 5.dp),
        style = if (selected) {
            style.copy(color = TerminalPalette.BarFg)
        } else {
            style.copy(color = TerminalPalette.Green, shadow = glow)
        },
    )
}

/** "Disney+" -> "disney-plus", "Prime Video" -> "prime-video". */
private fun slug(label: String): String =
    label.trim().lowercase().replace("+", "-plus").replace(Whitespace, "-")
