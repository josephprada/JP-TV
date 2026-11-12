package co.jp.tv.settings.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.apps.ui.LocalAppIcons
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.network.ui.SignalBars
import co.jp.tv.settings.data.DeviceInfo
import co.jp.tv.settings.domain.SettingsShortcut
import co.jp.tv.settings.domain.SettingsShortcuts
import co.jp.tv.settings.domain.ShortcutTarget
import co.jp.tv.themes.jptv.JpIcon
import co.jp.tv.themes.jptv.JpIconView
import co.jp.tv.themes.jptv.JpIcons
import java.time.LocalDateTime

private const val COLUMNS = 4
private val Coral = Color(0xFFFF7A6E)
private val Amber = Color(0xFFFF8A1F)
private val Ember = Color(0xFF5A0F12)
private val AppSwatches = listOf(0xFFDC2626, 0xFF1ED760, 0xFF2A5BD7, 0xFFF97316, 0xFF8B5CF6).map(::Color)

/** What the screen needs to print on its tiles; all of it cheap and permission-free. */
data class QuickSettingsInfo(
    val launcherName: String,
    val launcherVersion: String,
    val androidRelease: String,
    val deviceModel: String,
    /** First apps of the rail, drawn as the swatches of the Apps tile. */
    val apps: List<TvApp>,
    val storage: DeviceInfo.Storage?,
    /** Display height in pixels and refresh rate in Hz. */
    val display: Pair<Int, Int>?,
    /** Music volume 0..1, null when unknown. */
    val volume: Float?,
    /** Bluetooth state, null when the device has no adapter. */
    val bluetoothOn: Boolean?,
    val now: LocalDateTime,
    val hourFormat: ClockFormat.HourFormat,
    val titleFontId: String,
)

/**
 * The artboard's two red blobs behind the screen, without their 24 s drift: an accent glow at the top
 * and a dark ember one at the bottom right (`radial-gradient(circle, c 0, transparent 65%)` on 1100
 * and 1300 px circles, so the gradient reaches the box corner). Static brushes built once per size.
 */
private fun Modifier.settingsGlow(accent: Color): Modifier = drawWithCache {
    val k = size.width / 1920f
    val top = Brush.radialGradient(
        0f to accent.copy(alpha = 0.45f),
        0.65f to accent.copy(alpha = 0f),
        center = Offset(850f * k, 130f * k),
        radius = 777f * k,
    )
    val bottom = Brush.radialGradient(
        0f to Ember.copy(alpha = 0.9f),
        0.65f to Ember.copy(alpha = 0f),
        center = Offset(1650f * k, 900f * k),
        radius = 919f * k,
    )
    onDrawBehind {
        drawRect(bottom)
        drawRect(top)
    }
}

/** Compact header, a 4-column grid of shortcuts and a big "all Android settings" button. dp = design px / 2. */
@Composable
fun QuickSettingsScreen(
    info: QuickSettingsInfo,
    network: NetworkStatus,
    accent: Color,
    onShortcut: (SettingsShortcut) -> Unit,
    onAllSettings: () -> Unit,
    onEditApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tiles = SettingsShortcuts.tiles
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .settingsGlow(accent)
            .background(JpColors.Ink.copy(alpha = 0.5f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Header(info, accent, onEditApps)
        // Tiles keep their natural height (the artboard leaves the rest of the screen empty).
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(COLUMNS).forEachIndexed { row, rowTiles ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTiles.forEachIndexed { col, shortcut ->
                        val index = row * COLUMNS + col
                        Tile(
                            shortcut = shortcut,
                            info = info,
                            network = network,
                            accent = accent,
                            delayMs = 150 + index * 50,
                            onClick = { onShortcut(shortcut) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .then(if (index == 0) Modifier.focusRequester(firstFocus) else Modifier),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Footer(accent, onAllSettings)
    }
}

@Composable
private fun Header(info: QuickSettingsInfo, accent: Color, onEditApps: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 4.dp)
            .riseIn(delayMs = 0, durationMs = JpMotion.RiseMs, distanceDp = 8f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
        GlassButton(stringResource(R.string.settings_edit_apps), accent, onEditApps)
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
        GlassButton(stringResource(R.string.settings_all), accent, onAllSettings, primary = true, large = true)
    }
}

private data class TileText(val icon: JpIcon, val name: Int)

private fun textFor(id: SettingsShortcut.Id) = when (id) {
    SettingsShortcut.Id.NETWORK -> TileText(JpIcons.Wifi, R.string.tile_network)
    SettingsShortcut.Id.BLUETOOTH -> TileText(JpIcons.Bluetooth, R.string.tile_bluetooth)
    SettingsShortcut.Id.DISPLAY -> TileText(JpIcons.Display, R.string.tile_display)
    SettingsShortcut.Id.SOUND -> TileText(JpIcons.Sound, R.string.tile_sound)
    SettingsShortcut.Id.APPS -> TileText(JpIcons.Apps, R.string.tile_apps)
    SettingsShortcut.Id.STORAGE -> TileText(JpIcons.Storage, R.string.tile_storage)
    SettingsShortcut.Id.DATE_TIME -> TileText(JpIcons.Clock, R.string.tile_date_time)
    SettingsShortcut.Id.THEME -> TileText(JpIcons.Palette, R.string.tile_theme)
}

@Composable
private fun Tile(
    shortcut: SettingsShortcut,
    info: QuickSettingsInfo,
    network: NetworkStatus,
    accent: Color,
    delayMs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = shortcut.target != ShortcutTarget.Disabled
    val text = textFor(shortcut.id)
    var focused by remember { mutableStateOf(false) }
    val p by animateFloatAsState(if (focused) 1f else 0f, tween(JpMotion.FocusMs, easing = JpMotion.Entrance), label = "tile")
    val lift = with(LocalDensity.current) { 2.dp.toPx() }
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier = modifier
            .riseIn(delayMs = delayMs, durationMs = JpMotion.RiseMs, distanceDp = 8f)
            .graphicsLayer { translationY = -lift * p; alpha = if (enabled) 1f else JpColors.DisabledAlpha }
            .glass(shape, fill = if (focused) JpColors.FocusFill else null)
            .border(1.5.dp, accent.copy(alpha = 0.6f * p), shape)
            .then(
                if (enabled) {
                    Modifier
                        .onFocusChanged { focused = it.isFocused }
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconChip(text.icon, accent)
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) { TileContent(shortcut.id, info, network, accent) }
        BasicText(
            stringResource(text.name),
            style = TextStyle(JpColors.Paper, 11.5.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
            maxLines = 1,
        )
    }
}

@Composable
private fun IconChip(icon: JpIcon, accent: Color) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        Modifier
            .size(30.dp)
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.1f))), shape)
            .border(JpDimens.Hairline, JpColors.AccentLift.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        JpIconView(icon, 15.dp, Coral)
    }
}

/** The live miniature each tile carries between its icon and its name. */
@Composable
private fun TileContent(id: SettingsShortcut.Id, info: QuickSettingsInfo, network: NetworkStatus, accent: Color) {
    when (id) {
        SettingsShortcut.Id.NETWORK -> SignalBars(
            level = if (network.isConnected) network.signalLevel else 0,
            barWidth = 4.5.dp,
            heights = listOf(5.dp, 9.dp, 13.dp, 17.dp),
            gap = 3.dp,
        )
        SettingsShortcut.Id.BLUETOOTH -> info.bluetoothOn?.let { Toggle(it, accent) }
        SettingsShortcut.Id.DISPLAY -> info.display?.let { (height, hz) ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Chip("${height}P")
                Chip("$hz HZ")
            }
        }
        SettingsShortcut.Id.SOUND -> info.volume?.let { VolumeBar(it, accent) }
        SettingsShortcut.Id.APPS -> Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            AppSwatches.forEachIndexed { i, fallback -> AppSwatch(info.apps.getOrNull(i), fallback) }
        }
        SettingsShortcut.Id.STORAGE -> info.storage?.let { StorageBar(it, accent) }
        SettingsShortcut.Id.DATE_TIME -> {
            val parts = ClockFormat.parts(info.now, info.hourFormat)
            BasicText(
                "${parts.hours}:${parts.minutes}" + (parts.meridiem?.let { " $it" } ?: ""),
                style = TextStyle(JpColors.Paper, 26.sp, fontFamily = rememberTitleFont(info.titleFontId), letterSpacing = (-0.01).em),
                maxLines = 1,
            )
        }
        SettingsShortcut.Id.THEME -> Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            val current = accent
            val dots = (listOf(current) + DotColors.filter { it != current }).take(4)
            dots.forEachIndexed { i, color ->
                Box(
                    Modifier
                        .size(20.dp)
                        .then(if (i == 0) Modifier.border(2.5.dp, JpColors.Paper, CircleShape).padding(2.5.dp) else Modifier)
                        .background(color, CircleShape),
                )
            }
        }
    }
}

private val DotColors = listOf(0xFFE63329, 0xFFFF8A1F, 0xFF3D8BFD, 0xFFB26BFF).map(::Color)

@Composable
private fun Chip(label: String) {
    val shape = RoundedCornerShape(7.dp)
    BasicText(
        label,
        style = TextStyle(JpColors.Paper, 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.1.em),
        modifier = Modifier
            .background(JpColors.Paper.copy(alpha = 0.08f), shape)
            .border(JpDimens.Hairline, JpColors.Paper.copy(alpha = 0.14f), shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun Toggle(on: Boolean, accent: Color) {
    Box(
        Modifier.size(width = 32.dp, height = 18.dp).background(if (on) accent else JpColors.Paper.copy(alpha = 0.16f), RoundedCornerShape(9.dp)).padding(2.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(14.dp).background(JpColors.Paper, CircleShape))
    }
}

@Composable
private fun VolumeBar(fraction: Float, accent: Color) {
    val f = fraction.coerceIn(0.001f, 0.999f)
    Box(Modifier.fillMaxWidth().height(13.dp), contentAlignment = Alignment.CenterStart) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(JpColors.Paper.copy(alpha = 0.14f), RoundedCornerShape(3.dp)))
        Box(Modifier.fillMaxWidth(f).height(6.dp).background(accent, RoundedCornerShape(3.dp)))
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(f))
            Box(Modifier.size(13.dp).background(JpColors.Paper, CircleShape))
            Spacer(Modifier.weight(1f - f))
        }
    }
}

@Composable
private fun AppSwatch(app: TvApp?, fallback: Color) {
    val icons = LocalAppIcons.current
    val icon by produceState<ImageBitmap?>(null, app?.packageName) { value = app?.let { icons.load(it) } }
    val shape = RoundedCornerShape(6.dp)
    Box(Modifier.size(19.dp).clip(shape).background(fallback), contentAlignment = Alignment.Center) {
        icon?.let { Image(it, contentDescription = null, modifier = Modifier.size(19.dp)) }
    }
}

@Composable
private fun StorageBar(storage: DeviceInfo.Storage, accent: Color) {
    val used = if (storage.totalBytes <= 0) 0f else (1f - storage.freeBytes.toFloat() / storage.totalBytes).coerceIn(0.02f, 1f)
    val shape = RoundedCornerShape(3.5.dp)
    Box(Modifier.fillMaxWidth().height(7.dp).clip(shape).background(JpColors.Paper.copy(alpha = 0.14f), shape)) {
        Box(Modifier.fillMaxWidth(used).fillMaxHeight().background(Brush.horizontalGradient(listOf(accent, Amber)), shape))
    }
}
