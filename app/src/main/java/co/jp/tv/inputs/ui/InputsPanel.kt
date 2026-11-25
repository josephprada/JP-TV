package co.jp.tv.inputs.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.inputs.domain.InputSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val PanelFill = Color(0xFF121214).copy(alpha = 0.93f)
private const val REFRESH_MS = 3_000L

/**
 * Glass sheet that slides in from the right with the device's video inputs (HDMI and friends).
 * Choosing one opens the system TV app on it. The list is read when the panel opens and again every
 * few seconds so a cable plugged in while it is open shows up. Back is handled by the host.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputsPanel(
    load: () -> List<InputSource>,
    accent: Color,
    /** Switches to the input; returns false when nothing on the device can show it. */
    onOpen: (InputSource) -> Boolean,
    onAndroidSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slide = remember { Animatable(0f) }
    LaunchedEffect(Unit) { slide.animateTo(1f, tween(600, easing = JpMotion.Entrance)) }
    val slidePx = with(LocalDensity.current) { 40.dp.toPx() }
    val firstFocus = remember { FocusRequester() }
    var openFailed by remember { mutableStateOf(false) }
    val sources by produceState<List<InputSource>?>(initialValue = null, load) {
        while (true) {
            value = withContext(Dispatchers.IO) { load() }
            delay(REFRESH_MS)
        }
    }
    // Focus the first input once the list arrives, or the Android settings button when there are none.
    LaunchedEffect(sources != null) { if (sources != null) runCatching { firstFocus.requestFocus() } }

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BasicText(
                stringResource(R.string.panel_inputs_title),
                style = TextStyle(JpColors.Paper, 20.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
            )
            val list = sources
            BasicText(
                stringResource(if (list != null && list.isEmpty()) R.string.panel_inputs_empty else R.string.panel_inputs_hint),
                style = TextStyle(JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
            )
            if (openFailed) {
                BasicText(
                    stringResource(R.string.panel_inputs_open_failed),
                    style = TextStyle(JpColors.AccentLift, 9.sp, fontFamily = JpFonts.Body),
                )
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                list?.forEachIndexed { i, source ->
                    GlassButton(
                        label = rowText(source, list),
                        accent = accent,
                        onClick = { openFailed = !onOpen(source) },
                        modifier = Modifier.fillMaxWidth().then(if (i == 0) Modifier.focusRequester(firstFocus) else Modifier),
                        primary = i == 0,
                    )
                }
            }
            GlassButton(
                label = stringResource(R.string.panel_inputs_android),
                accent = accent,
                onClick = onAndroidSettings,
                modifier = Modifier.then(if (list != null && list.isEmpty()) Modifier.focusRequester(firstFocus) else Modifier),
            )
        }
    }
}

/** "HDMI 2 · CON SEÑAL"; unnamed inputs are numbered among their own kind ("HDMI 1", "HDMI 2"). */
@Composable
private fun rowText(source: InputSource, all: List<InputSource>): String {
    val kind = stringResource(kindName(source.kind))
    val sameKind = all.filter { it.kind == source.kind }
    val name = source.label ?: if (sameKind.size > 1) "$kind ${sameKind.indexOf(source) + 1}" else kind
    val signal = when (source.signal) {
        InputSource.Signal.CONNECTED -> stringResource(R.string.input_signal_connected)
        InputSource.Signal.STANDBY -> stringResource(R.string.input_signal_standby)
        InputSource.Signal.DISCONNECTED -> stringResource(R.string.input_signal_disconnected)
        InputSource.Signal.UNKNOWN -> null
    }
    return listOfNotNull(name.uppercase(), signal).joinToString(" · ")
}

private fun kindName(kind: InputSource.Kind): Int = when (kind) {
    InputSource.Kind.HDMI -> R.string.input_kind_hdmi
    InputSource.Kind.COMPOSITE -> R.string.input_kind_composite
    InputSource.Kind.COMPONENT -> R.string.input_kind_component
    InputSource.Kind.VGA -> R.string.input_kind_vga
    InputSource.Kind.OTHER -> R.string.input_kind_other
}
