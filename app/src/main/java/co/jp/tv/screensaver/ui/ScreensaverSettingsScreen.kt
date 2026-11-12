package co.jp.tv.screensaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.preferences.ui.Chip
import co.jp.tv.preferences.ui.OptionsCard
import co.jp.tv.preferences.ui.TextEntryField
import co.jp.tv.screensaver.domain.IdleRules
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverSettings

/**
 * Screensaver settings: mode, idle time and the clock message, a preview button and a shortcut to Android's own
 * screensaver settings (where the user picks JP-TV; this launcher never changes that setting itself). Every
 * control is a D-pad stop; each change is saved right away through [onChange].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreensaverSettingsScreen(
    settings: ScreensaverSettings,
    accent: Color,
    titleFontId: String,
    onChange: (ScreensaverSettings) -> Unit,
    onPreview: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    modifier: Modifier = Modifier,
    /** A mode was picked: the host may ask for the storage permission that mode needs. */
    onModeChosen: (ScreensaverMode) -> Unit = {},
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstFocus.requestFocus() }
    var message by remember { mutableStateOf(settings.message) }
    val titleFont = rememberTitleFont(titleFontId)

    Column(
        modifier = modifier.fillMaxSize().background(JpColors.Ink).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BasicText(
                stringResource(R.string.screensaver_title),
                style = TextStyle(color = JpColors.Paper, fontSize = 20.sp, fontFamily = JpFonts.Body),
            )
            BasicText(
                stringResource(R.string.screensaver_breadcrumb),
                style = TextStyle(color = JpColors.Faint, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
            )
        }
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.width(400.dp).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OptionsCard(stringResource(R.string.screensaver_mode_label), delayMs = 0) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScreensaverMode.entries.forEachIndexed { i, mode ->
                            Chip(
                                label = stringResource(mode.labelRes()),
                                selected = settings.mode == mode,
                                accent = accent,
                                onClick = {
                                    onChange(settings.copy(mode = mode))
                                    onModeChosen(mode)
                                },
                                modifier = if (i == 0) Modifier.focusRequester(firstFocus) else Modifier,
                            )
                        }
                    }
                }
                OptionsCard(stringResource(R.string.screensaver_idle_label), delayMs = 40) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        IdleRules.CHOICES.forEach { minutes ->
                            Chip(
                                label = if (minutes == 0) stringResource(R.string.screensaver_idle_never) else stringResource(R.string.screensaver_idle_minutes, minutes),
                                selected = settings.idleMinutes == minutes,
                                accent = accent,
                                onClick = { onChange(settings.copy(idleMinutes = minutes)) },
                            )
                        }
                    }
                }
                OptionsCard(stringResource(R.string.screensaver_message_label), delayMs = 80) {
                    TextEntryField(
                        value = message,
                        onValueChange = {
                            message = it.take(ScreensaverSettings.MAX_MESSAGE)
                            onChange(settings.copy(message = ScreensaverSettings.sanitizeMessage(message)))
                        },
                        accent = accent,
                        label = stringResource(R.string.screensaver_message_label),
                        textStyle = TextStyle(color = JpColors.Paper, fontSize = 15.sp, fontFamily = titleFont),
                        placeholder = stringResource(R.string.screensaver_message_hint),
                        placeholderStyle = TextStyle(color = JpColors.Faint, fontSize = 15.sp, fontFamily = titleFont),
                    )
                }
            }
            val shape = RoundedCornerShape(20.dp)
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .glass(shape)
                    .border(1.dp, JpColors.Paper.copy(alpha = 0.08f), shape)
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BasicText(
                    stringResource(settings.mode.labelRes()).uppercase(),
                    style = TextStyle(color = accent, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.18.em),
                )
                BasicText(
                    stringResource(settings.mode.descriptionRes()),
                    style = TextStyle(color = JpColors.Paper, fontSize = 13.sp, fontFamily = JpFonts.Body),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(stringResource(R.string.screensaver_preview), accent, onPreview, primary = true)
                }
                BasicText(
                    stringResource(R.string.screensaver_system_hint),
                    style = TextStyle(color = JpColors.Paper, fontSize = 11.sp, fontFamily = JpFonts.Body),
                    modifier = Modifier.padding(top = 10.dp),
                )
                BasicText(
                    stringResource(R.string.screensaver_system_note),
                    style = TextStyle(color = JpColors.Muted, fontSize = 8.5.sp, fontFamily = JpFonts.Body),
                )
                GlassButton(stringResource(R.string.screensaver_open_system), accent, onOpenSystemSettings)
            }
        }
    }
}
