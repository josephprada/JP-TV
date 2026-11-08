package co.jp.tv.themes.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.themeKeyNavigation
import java.time.LocalDateTime
import java.util.Locale

object TerminalTheme : LauncherTheme {
    override val id = "terminal"
    override val displayName = "Terminal"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(TerminalPalette.BackgroundGlow, TerminalPalette.Background),
                        radius = 1200f,
                    ),
                )
                .terminalScanlines()
                .focusRequester(focusRequester)
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            Column(Modifier.fillMaxSize()) {
                TerminalStatusBar(state.brand, state.network, state.now, actions)
                Row(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 28.dp)) {
                    TerminalInfoPanel(state.brand, state.apps.size, state.network, Modifier.weight(1f))
                    TerminalAppList(
                        apps = state.apps,
                        focusedIndex = state.focusedIndex,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .padding(start = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalStatusBar(brand: String, network: NetworkStatus, now: LocalDateTime, actions: HomeActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalPalette.Green)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BasicText(
                "[$brand]",
                style = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323),
            )
            BasicText("0:apps*", style = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323))
            BasicText(
                "1:red",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
                style = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323),
            )
            BasicText(
                "2:ajustes",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openSettings,
                ),
                style = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323),
            )
        }
        val latency = network.routerLatencyMs ?: network.internetLatencyMs
        val net = if (network.isConnected) "wifi ok" else "sin red"
        val lat = latency?.let { " · ${it}ms" }.orEmpty()
        val date = ClockFormat.date(now).lowercase(Locale.forLanguageTag("es"))
        BasicText(
            "$net$lat | ${ClockFormat.time(now)} $date",
            style = TextStyle(color = TerminalPalette.BarFg, fontSize = 15.sp, fontFamily = JpFonts.Vt323),
        )
    }
}

@Composable
private fun TerminalInfoPanel(brand: String, count: Int, network: NetworkStatus, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BasicText(
            """
                |   ___ ____   _____ 
                |  |_ _|  _ \ |_   _|
                |   | || |_) |  | |  
                |   | ||  __/   | |  
                |  |___|_|      |_|  
            """.trimMargin(),
            style = TextStyle(color = TerminalPalette.Green, fontSize = 17.sp, fontFamily = JpFonts.Vt323),
        )
        InfoLine("host", brand.lowercase())
        InfoLine("apps", "$count visibles")
        InfoLine("red", network.ssid ?: if (network.isConnected) "conectado" else "offline")
        InfoLine("ip", network.ip ?: "—")
        InfoLine("gateway", network.gateway ?: "—")
        InfoLine("ping", NetworkRules.formatLatency(network.routerLatencyMs ?: network.internetLatencyMs))
    }
}

@Composable
private fun InfoLine(key: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(key, style = TextStyle(color = TerminalPalette.Amber, fontSize = 16.sp, fontFamily = JpFonts.Vt323))
        BasicText(value, style = TextStyle(color = TerminalPalette.Green, fontSize = 16.sp, fontFamily = JpFonts.Vt323))
    }
}

@Composable
private fun TerminalAppList(apps: List<TvApp>, focusedIndex: Int, modifier: Modifier = Modifier) {
    val focused = apps.getOrNull(focusedIndex)
    Column(
        modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BasicText(
            "jp@tv:~$ ls --apps",
            style = TextStyle(color = TerminalPalette.GreenDim, fontSize = 20.sp, fontFamily = JpFonts.Vt323),
        )
        apps.forEachIndexed { index, app ->
            val prefix = if (index == focusedIndex) "> " else "  "
            val meta = if (app.isTvApp) " [tv]" else ""
            BasicText(
                "$prefix${app.label}$meta",
                style = TextStyle(
                    color = if (index == focusedIndex) TerminalPalette.Green else TerminalPalette.Green.copy(alpha = 0.75f),
                    fontSize = 20.sp,
                    fontFamily = JpFonts.Vt323,
                ),
            )
        }
        BasicText(
            "jp@tv:~$ open ${focused?.label?.lowercase() ?: "…"}_",
            style = TextStyle(color = TerminalPalette.Green, fontSize = 20.sp, fontFamily = JpFonts.Vt323),
            modifier = Modifier.padding(top = 16.dp),
        )
        BasicText(
            stringResource(R.string.theme_terminal_hint),
            style = TextStyle(color = TerminalPalette.GreenDim, fontSize = 15.sp, fontFamily = JpFonts.Vt323),
        )
    }
}

private fun Modifier.terminalScanlines(): Modifier = drawBehind {
    var y = 0f
    while (y < size.height) {
        drawRect(TerminalPalette.Scanline, topLeft = Offset(0f, y), size = Size(size.width, 1f))
        y += 2f
    }
}
