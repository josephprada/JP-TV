package co.jp.tv.settings.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.domain.SettingsShortcut
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget

private const val COLUMNS = 4

/** What the screen needs to print on its tiles; all of it cheap and permission-free. */
data class QuickSettingsInfo(
    val launcherName: String,
    val launcherVersion: String,
    val androidRelease: String,
    val deviceModel: String,
    val appCount: Int,
    val storage: DeviceInfo.Storage?,
    /** Display height in pixels and refresh rate in Hz. */
    val display: Pair<Int, Int>?,
)

/** Compact header, a 4-column grid of shortcuts and a big "all Android settings" button. dp = design px / 2. */
@Composable
fun QuickSettingsScreen(
    info: QuickSettingsInfo,
    network: NetworkStatus,
    accent: Color,
    onShortcut: (SettingsShortcut) -> Unit,
    onAllSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tiles = SettingsShortcuts.tiles + SettingsShortcuts.upcoming
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JpColors.Ink.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Header(info)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(COLUMNS).forEachIndexed { row, rowTiles ->
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTiles.forEachIndexed { col, shortcut ->
                        val index = row * COLUMNS + col
                        Tile(
                            shortcut = shortcut,
                            value = tileValue(shortcut, info, network),
                            accent = accent,
                            delayMs = 150 + index * 50,
                            onClick = { onShortcut(shortcut) },
                            modifier = Modifier
                                .weight(1f)
                                .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
                        )
                    }
                }
            }
        }
        Footer(accent, onAllSettings)
    }
}

@Composable
private fun Header(info: QuickSettingsInfo) {
    Row(
        modifier = Modifier.padding(start = 8.dp, top = 4.dp).riseIn(delayMs = 0, durationMs = JpMotion.RiseMs, distanceDp = 8f),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BasicText(
            stringResource(R.string.settings_title),
            style = TextStyle(JpColors.Paper, 20.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
        )
        BasicText(
            stringResource(R.string.settings_version, info.launcherName, info.launcherVersion, info.androidRelease, info.deviceModel).uppercase(),
            style = TextStyle(JpColors.Faint, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun Footer(accent: Color, onAllSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).riseIn(delayMs = 600, durationMs = JpMotion.RiseMs, distanceDp = 8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BasicText(
            stringResource(R.string.settings_footer),
            style = TextStyle(JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
        )
        GlassButton(stringResource(R.string.settings_all), accent, onAllSettings, primary = true)
    }
}

@Composable
private fun tileValue(shortcut: SettingsShortcut, info: QuickSettingsInfo, network: NetworkStatus): String? =
    when (shortcut.id) {
        SettingsShortcut.Id.NETWORK -> networkSummary(network)
        SettingsShortcut.Id.DISPLAY -> info.display?.let { (height, hz) -> stringResource(R.string.tile_display_value, height, hz) }
        SettingsShortcut.Id.APPS -> pluralStringResource(R.plurals.tile_apps_value, info.appCount, info.appCount)
        SettingsShortcut.Id.STORAGE -> info.storage?.let {
            stringResource(R.string.tile_storage_value, SettingsShortcuts.formatGb(it.freeBytes), SettingsShortcuts.formatGb(it.totalBytes))
        }
        SettingsShortcut.Id.THEME -> stringResource(R.string.settings_coming_soon)
        else -> null
    }

@Composable
private fun networkSummary(network: NetworkStatus): String? = when {
    network.state == NetworkStatus.State.UNKNOWN -> null
    !network.isConnected -> stringResource(R.string.network_offline).uppercase()
    else -> {
        val name = network.ssid ?: stringResource(
            if (network.transport == NetworkStatus.Transport.ETHERNET) R.string.network_ethernet else R.string.network_wifi,
        )
        val latency = network.routerLatencyMs ?: network.internetLatencyMs
        (if (latency == null) name else "$name · ${NetworkRules.formatLatency(latency)}").uppercase()
    }
}

private data class TileText(val glyph: Int, val name: Int)

private fun textFor(id: SettingsShortcut.Id) = when (id) {
    SettingsShortcut.Id.NETWORK -> TileText(R.string.glyph_network, R.string.tile_network)
    SettingsShortcut.Id.BLUETOOTH -> TileText(R.string.glyph_bluetooth, R.string.tile_bluetooth)
    SettingsShortcut.Id.DISPLAY -> TileText(R.string.glyph_display, R.string.tile_display)
    SettingsShortcut.Id.SOUND -> TileText(R.string.glyph_sound, R.string.tile_sound)
    SettingsShortcut.Id.APPS -> TileText(R.string.glyph_apps, R.string.tile_apps)
    SettingsShortcut.Id.STORAGE -> TileText(R.string.glyph_storage, R.string.tile_storage)
    SettingsShortcut.Id.DATE_TIME -> TileText(R.string.glyph_date_time, R.string.tile_date_time)
    SettingsShortcut.Id.THEME -> TileText(R.string.glyph_theme, R.string.tile_theme)
}

@Composable
private fun Tile(
    shortcut: SettingsShortcut,
    value: String?,
    accent: Color,
    delayMs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = shortcut.target != ShortcutTarget.Disabled
    val text = textFor(shortcut.id)
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "tile")
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .riseIn(delayMs = delayMs, durationMs = JpMotion.RiseMs, distanceDp = 8f)
            .fillMaxSize()
            .graphicsLayer { scaleX = 1f + 0.03f * p; scaleY = 1f + 0.03f * p; alpha = if (enabled) 1f else 0.45f }
            .glass(shape, fill = if (focused) JpColors.FocusFill else null)
            .border(JpDimens.Hairline, accent.copy(alpha = 0.8f * p), shape)
            .then(
                if (enabled) {
                    Modifier
                        .onFocusChanged { focused = it.isFocused }
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            Modifier.size(32.dp).background(JpColors.Paper.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                stringResource(text.glyph),
                style = TextStyle(JpColors.Paper, 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.06.em),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            BasicText(
                stringResource(text.name),
                style = TextStyle(JpColors.Paper, 14.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
                maxLines = 1,
            )
            if (value != null) {
                BasicText(
                    value,
                    style = TextStyle(JpColors.Muted, 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                    maxLines = 1,
                )
            }
        }
    }
}
