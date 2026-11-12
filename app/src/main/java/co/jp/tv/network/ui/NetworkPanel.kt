package co.jp.tv.network.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.State
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.jptv.JpIconView
import co.jp.tv.themes.jptv.JpIcons
import co.jp.tv.themes.shared.networkName
import co.jp.tv.network.domain.SsidAccess

private val OnlineGreen = Color(0xFF38D670)
private val OnlineText = Color(0xFF8FF0B2)
private val OfflineText = Color(0xFFFF8A82)
private val PanelFill = Color(0xFF121214).copy(alpha = 0.93f)

/**
 * Glass sheet that slides in from the right (dp = design px / 2).
 * Focus stays inside it; Back is handled by the host.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NetworkPanel(
    status: NetworkStatus,
    accent: Color,
    onTest: () -> Unit,
    onWifiSettings: () -> Unit,
    modifier: Modifier = Modifier,
    /** Opens the system location switch; shown when Android hides the Wi-Fi name because location is off. */
    onLocationSettings: () -> Unit = {},
    /** Asks for the location permission; shown when it is missing. */
    onGrantLocation: () -> Unit = {},
) {
    val slide = remember { Animatable(0f) }
    LaunchedEffect(Unit) { slide.animateTo(1f, tween(600, easing = JpMotion.Entrance)) }
    val slidePx = with(LocalDensity.current) { 40.dp.toPx() }
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }

    Box(modifier.fillMaxSize().background(JpColors.Ink.copy(alpha = 0.4f))) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(20.dp)
                .fillMaxHeight()
                .width(390.dp)
                .graphicsLayer { alpha = slide.value; translationX = (1f - slide.value) * slidePx }
                .glass(RoundedCornerShape(20.dp), fill = PanelFill)
                .padding(28.dp)
                .focusProperties { exit = { FocusRequester.Cancel } }
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Header(status)
            LinkCard(status)
            SsidHint(status, accent, onLocationSettings, onGrantLocation)
            Facts(status)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassButton(
                    stringResource(R.string.panel_test), accent, onTest,
                    Modifier.focusRequester(firstFocus), primary = true,
                )
                GlassButton(stringResource(R.string.panel_wifi_settings), accent, onWifiSettings)
            }
        }
    }
}

@Composable
private fun Header(status: NetworkStatus) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        BasicText(
            stringResource(R.string.panel_network_title),
            style = TextStyle(JpColors.Paper, 20.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
        )
        InternetPill(online = status.hasInternet)
    }
}

@Composable
private fun InternetPill(online: Boolean) {
    val tone = if (online) OnlineGreen else JpColors.AccentLift
    val textColor = if (online) OnlineText else OfflineText
    Row(
        modifier = Modifier
            .background(tone.copy(alpha = 0.12f), RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PulseDot(tone, pulsing = online)
        BasicText(
            stringResource(if (online) R.string.panel_online else R.string.panel_offline),
            style = TextStyle(textColor, 9.sp, fontFamily = JpFonts.Body),
        )
    }
}

@Composable
private fun PulseDot(color: Color, pulsing: Boolean) {
    val paused = LocalPaused.current
    val ring: State<Float> = if (pulsing && !paused) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "pulseRing",
        )
    } else {
        remember { mutableStateOf(0f) }
    }
    Box(
        Modifier
            .size(5.dp)
            .drawBehind {
                val p = ring.value
                if (p > 0f) {
                    drawCircle(color.copy(alpha = 0.6f * (1f - p)), radius = size.minDimension / 2f + 7.dp.toPx() * p)
                }
            }
            .background(color, CircleShape),
    )
}

@Composable
private fun LinkCard(status: NetworkStatus) {
    val title = when {
        !status.isConnected -> stringResource(R.string.network_offline)
        else -> networkName(status)
    }
    val detail = when {
        !status.isConnected -> stringResource(R.string.panel_no_link)
        status.transport == NetworkStatus.Transport.ETHERNET -> stringResource(R.string.panel_cable)
        else -> wifiDetail(status)
    }
    Row(
        modifier = Modifier
            .riseIn(delayMs = 150, durationMs = 600, distanceDp = 8f)
            .fillMaxWidth()
            .background(Color(0xFF050506).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .border(1.dp, JpColors.Paper.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        JpIconView(JpIcons.Wifi, 20.dp, if (status.isConnected) JpColors.Paper else JpColors.Faint)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            BasicText(title, style = TextStyle(JpColors.Paper, 14.sp, FontWeight.Medium, fontFamily = JpFonts.Body), maxLines = 1)
            BasicText(detail, style = TextStyle(JpColors.Muted, 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em))
        }
        SignalBars(
            level = status.signalLevel,
            barWidth = 3.5.dp,
            heights = listOf(5.dp, 8.5.dp, 12.dp, 16.dp),
            gap = 2.5.dp,
        )
    }
}

/** Explains why the Wi-Fi name is missing and offers the one action that fixes it. */
@Composable
private fun SsidHint(status: NetworkStatus, accent: Color, onLocationSettings: () -> Unit, onGrant: () -> Unit) {
    val needsLocation = status.ssidAccess == SsidAccess.NeedsLocation
    if (!needsLocation && status.ssidAccess != SsidAccess.NeedsPermission) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(
            stringResource(if (needsLocation) R.string.panel_ssid_needs_location else R.string.panel_ssid_needs_permission),
            style = TextStyle(JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
        )
        GlassButton(
            stringResource(if (needsLocation) R.string.panel_ssid_location_settings else R.string.panel_ssid_grant),
            accent,
            if (needsLocation) onLocationSettings else onGrant,
        )
    }
}

/** "5 GHZ · SEÑAL 62%", or "SEÑAL 3 DE 4" when Android reports no RSSI/frequency. */
@Composable
private fun wifiDetail(status: NetworkStatus): String {
    val percent = status.signalPercent ?: return stringResource(R.string.panel_signal, status.signalLevel)
    val signal = stringResource(R.string.panel_signal_percent, percent)
    return status.band?.let { "$it · $signal" } ?: signal
}

@Composable
private fun Facts(status: NetworkStatus) {
    val dash = stringResource(R.string.network_no_detail)
    val cells = buildList {
        add(stringResource(R.string.panel_ip) to (status.ip ?: dash))
        add(stringResource(R.string.panel_gateway) to (status.gateway ?: dash))
        add(stringResource(R.string.panel_dns) to (status.dns.firstOrNull() ?: dash))
        add(stringResource(R.string.panel_router_latency) to NetworkRules.formatLatency(status.routerLatencyMs))
        add(stringResource(R.string.panel_internet_latency) to NetworkRules.formatLatency(status.internetLatencyMs))
        // The MAC cell is omitted when Android does not expose it without extra permissions.
        status.mac?.let { add(stringResource(R.string.panel_mac) to NetworkRules.maskMac(it)) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cells.chunked(2).forEachIndexed { row, pair ->
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pair.forEachIndexed { col, (label, value) ->
                    Fact(label, value, delayMs = 200 + (row * 2 + col) * 50, modifier = Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Fact(label: String, value: String, delayMs: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .riseIn(delayMs = delayMs, durationMs = 600, distanceDp = 8f)
            .fillMaxHeight()
            .background(JpColors.Paper.copy(alpha = 0.05f), RoundedCornerShape(11.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        BasicText(label, style = TextStyle(JpColors.Faint, 6.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em))
        BasicText(value, style = TextStyle(JpColors.Paper, 13.sp, fontFamily = JpFonts.Body), maxLines = 2)
    }
}
