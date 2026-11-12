package co.jp.tv.preferences.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.preferences.domain.AccentPreset
import co.jp.tv.preferences.domain.AccentPresets
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.domain.TitleFont
import co.jp.tv.preferences.domain.TitleFonts
import co.jp.tv.wallpaper.WallpaperRegistry
import co.jp.tv.weather.domain.WeatherPlace
import java.time.LocalDateTime

/** Fixed sample instant the preview clock shows, so only the chosen format changes. */
private val PreviewTime: LocalDateTime = LocalDateTime.of(2025, 11, 8, 21, 40)

/** Clock format chips in display order, each with its label resource. */
private val HourFormatOptions = listOf(
    HourFormat.H24 to R.string.customize_hour_format_24,
    HourFormat.H12 to R.string.customize_hour_format_12,
)

/**
 * Draft customize panel: accent, title font, wallpaper and brand chips with a live preview.
 * [onApply] commits the draft; Back discards it (caller keeps the previous config).
 */
@Composable
fun CustomizeScreen(
    initial: LauncherConfig,
    onApply: (LauncherConfig) -> Unit,
    modifier: Modifier = Modifier,
    searchPlaces: suspend (String) -> List<WeatherPlace> = { emptyList() },
) {
    var draft by remember(initial) { mutableStateOf(initial) }
    var permissionRevision by remember { mutableIntStateOf(0) }
    val requestMedia = rememberMediaPermissionRequester { permissionRevision++ }
    val accent = Color(draft.accent)
    val titleFont = rememberTitleFont(draft.titleFontId)
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JpColors.Ink)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BasicText(
                text = stringResource(R.string.customize_title),
                style = TextStyle(color = JpColors.Paper, fontSize = 20.sp, fontFamily = JpFonts.Body),
            )
            BasicText(
                text = stringResource(R.string.customize_breadcrumb),
                style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
            )
        }

        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(
                Modifier
                    .width(380.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OptionsCard(stringResource(R.string.customize_brand), delayMs = 0) {
                    BrandField(
                        value = draft.brand,
                        onValueChange = { draft = draft.copy(brand = it) },
                        accent = accent,
                        titleFont = titleFont,
                        modifier = Modifier.focusRequester(firstFocus),
                    )
                }
                OptionsCard(stringResource(R.string.customize_accent), delayMs = 40) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccentPresets.all.forEach { preset ->
                            AccentSwatch(
                                preset = preset,
                                selected = draft.accent == preset.argb,
                                onClick = { draft = draft.copy(accent = preset.argb) },
                            )
                        }
                    }
                    val selected = AccentPresets.byArgb(draft.accent)
                    BasicText(
                        text = "${selected.displayName} · ${selected.hex}",
                        style = TextStyle(color = JpColors.Muted, fontSize = 9.sp, fontFamily = JpFonts.Body),
                    )
                }
                OptionsCard(stringResource(R.string.customize_font), delayMs = 80) {
                    TitleFonts.all.forEach { font ->
                        FontRow(
                            font = font,
                            selected = draft.titleFontId == font.id,
                            accent = accent,
                            onClick = { draft = draft.copy(titleFontId = font.id) },
                        )
                    }
                }
                OptionsCard(stringResource(R.string.customize_wallpaper), delayMs = 120) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        WallpaperRegistry.all.forEach { wall ->
                            Chip(
                                label = wall.displayName,
                                selected = draft.wallpaperId == wall.id,
                                accent = accent,
                                onClick = {
                                    draft = draft.copy(wallpaperId = wall.id)
                                    requestMedia(wall.id)
                                },
                            )
                        }
                    }
                }
                OptionsCard(stringResource(R.string.customize_hour_format), delayMs = 160) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HourFormatOptions.forEach { (format, label) ->
                            Chip(
                                label = stringResource(label),
                                selected = draft.hourFormat == format,
                                accent = accent,
                                onClick = { draft = draft.copy(hourFormat = format) },
                            )
                        }
                    }
                }
                OptionsCard(stringResource(R.string.customize_weather), delayMs = 200) {
                    WeatherPlaceCard(
                        selected = draft.weatherPlace,
                        onSelect = { draft = draft.copy(weatherPlace = it) },
                        search = searchPlaces,
                        accent = accent,
                    )
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, JpColors.Paper.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .background(Color(0xFF07070A), RoundedCornerShape(20.dp))
                    // Wallpapers draw orbs past their bounds; the preview must not paint over the options column.
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                key(permissionRevision) {
                    WallpaperRegistry.byId(draft.wallpaperId)
                        .Render(accent, paused = LocalPaused.current, modifier = Modifier.fillMaxSize())
                }
                PreviewRail(brand = LauncherConfig.sanitizeBrand(draft.brand), accent = accent, titleFont = titleFont)
                PreviewClock(accent = accent, titleFont = titleFont, hourFormat = draft.hourFormat)
                ApplyButton(
                    accent = accent,
                    onClick = { onApply(draft) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun OptionsCard(title: String, delayMs: Int, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .riseIn(delayMs = delayMs)
            .background(JpColors.Paper.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
            .border(1.dp, JpColors.Paper.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        BasicText(
            text = title,
            style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        )
        content()
    }
}

@Composable
private fun AccentSwatch(preset: AccentPreset, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val color = Color(preset.argb)
    val active = selected || focused
    Box(
        Modifier
            .size(32.dp)
            .graphicsLayer {
                val s = if (selected) 1.1f else 1f
                scaleX = s
                scaleY = s
            }
            .background(color, CircleShape)
            .then(if (active) Modifier.border(3.dp, JpColors.Ink, CircleShape).border(2.dp, color, CircleShape) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick),
    )
}

@Composable
private fun FontRow(font: TitleFont, selected: Boolean, accent: Color, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val family = rememberTitleFont(font.id)
    val active = selected || focused
    Row(
        Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(if (active) Color(0xB3050506) else Color.Transparent, RoundedCornerShape(10.dp))
            .then(if (active) Modifier.border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp)) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText("21:40", style = TextStyle(color = JpColors.Paper, fontSize = 17.sp, fontFamily = family), modifier = Modifier.width(60.dp))
        BasicText(font.displayName, style = TextStyle(color = JpColors.Muted, fontSize = 9.sp, fontFamily = JpFonts.Body))
    }
}

@Composable
internal fun Chip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val active = selected || focused
    BasicText(
        text = label,
        style = TextStyle(color = JpColors.Paper, fontSize = 9.5.sp, fontFamily = JpFonts.Body),
        modifier = modifier
            .background(
                if (active) Color(0xB3050506) else JpColors.Paper.copy(alpha = 0.06f),
                RoundedCornerShape(11.dp),
            )
            .then(
                if (active) Modifier.border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(11.dp))
                else Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(11.dp)),
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
    )
}

@Composable
private fun ApplyButton(accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    BasicText(
        text = stringResource(R.string.customize_apply),
        style = TextStyle(color = JpColors.Paper, fontSize = 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
        modifier = modifier
            .background(Color(0xB3050506), RoundedCornerShape(14.dp))
            .border(1.dp, accent.copy(alpha = if (focused) 1f else 0.7f), RoundedCornerShape(14.dp))
            .onFocusChanged { focused = it.isFocused }
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

@Composable
private fun PreviewRail(brand: String, accent: Color, titleFont: FontFamily) {
    Column(
        Modifier
            .padding(16.dp)
            .width(190.dp)
            .fillMaxHeight()
            .glass(RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BasicText(brand, style = TextStyle(color = accent, fontSize = 23.sp, fontFamily = titleFont), modifier = Modifier.padding(start = 6.dp, bottom = 8.dp), maxLines = 1)
        PreviewAppRow("YouTube", focused = true, accent = accent)
        PreviewAppRow("Netflix", focused = false, accent = accent)
        PreviewAppRow("Spotify", focused = false, accent = accent)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun PreviewAppRow(label: String, focused: Boolean, accent: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(32.dp)
            .then(
                if (focused) Modifier
                    .background(Color(0x9E050506), RoundedCornerShape(10.dp))
                    .border(1.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                else Modifier,
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(21.dp)
                .background(if (focused) accent else JpColors.Paper.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (focused) {
                BasicText(
                    text = label.take(2).uppercase(),
                    style = TextStyle(color = JpColors.Ink, fontSize = 6.sp, fontFamily = JpFonts.Mono),
                )
            }
        }
        BasicText(label, style = TextStyle(color = if (focused) JpColors.Paper else JpColors.Muted, fontSize = 11.sp))
    }
}

@Composable
private fun BoxScope.PreviewClock(accent: Color, titleFont: FontFamily, hourFormat: HourFormat) {
    Column(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .glass(RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.End,
    ) {
        val time = ClockFormat.parts(PreviewTime, hourFormat)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BasicText(
                "${time.hours}:${time.minutes}",
                style = TextStyle(color = JpColors.Paper, fontSize = 48.sp, fontFamily = titleFont),
            )
            time.meridiem?.let { meridiem ->
                BasicText(
                    meridiem,
                    style = TextStyle(color = JpColors.Muted, fontSize = 12.sp, fontFamily = titleFont),
                    modifier = Modifier.padding(bottom = 7.dp),
                )
            }
        }
        BasicText(
            ClockFormat.date(PreviewTime),
            style = TextStyle(color = accent, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.2.em),
        )
    }
}
