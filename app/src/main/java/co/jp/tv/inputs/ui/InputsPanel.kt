package co.jp.tv.inputs.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.onFocusChanged
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
import co.jp.tv.inputs.domain.InputRules
import co.jp.tv.inputs.domain.InputSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val PanelFill = Color(0xFF121214).copy(alpha = 0.93f)
private const val REFRESH_MS = 3_000L
private const val TOGGLE = "toggle"
private const val ANDROID = "android"
private fun rowKey(id: String) = "in:$id"

/**
 * Glass sheet that slides in from the right with the device's video inputs (HDMI and friends).
 * Choosing one opens the system TV app on it. The list is read when the panel opens and again every
 * few seconds so a cable plugged in while it is open shows up. Back is handled by the host.
 *
 * Some firmware lists ports the device does not have. Holding OK (or Menu) on an input opens a small
 * inline action to hide it; hidden inputs stay behind "MOSTRAR OCULTAS" so they can be brought back.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputsPanel(
    load: suspend () -> List<InputSource>,
    /** Ids of the inputs the user hid. */
    hidden: Set<String>,
    onHide: (InputSource) -> Unit,
    onShow: (InputSource) -> Unit,
    accent: Color,
    /** Switches to the input; returns false when nothing on the device can show it. */
    onOpen: (InputSource) -> Boolean,
    onAndroidSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slide = remember { Animatable(0f) }
    LaunchedEffect(Unit) { slide.animateTo(1f, tween(600, easing = JpMotion.Entrance)) }
    val slidePx = with(LocalDensity.current) { 40.dp.toPx() }
    var openFailed by remember { mutableStateOf(false) }
    var showHidden by remember { mutableStateOf(false) }
    /** Id of the input whose inline action is open. */
    var actionFor by remember { mutableStateOf<String?>(null) }
    val requesters = remember { mutableMapOf<String, FocusRequester>() }
    fun requester(key: String) = requesters.getOrPut(key) { FocusRequester() }
    var pendingFocus by remember { mutableStateOf<String?>(null) }
    val sources by produceState<List<InputSource>?>(initialValue = null, load) {
        while (true) {
            value = withContext(Dispatchers.IO) { load() }
            delay(REFRESH_MS)
        }
    }
    val list = sources
    val (shown, hiddenSources) = if (list == null) emptyList<InputSource>() to emptyList() else InputRules.partition(list, hidden)
    // Hidden rows only show while the toggle is on; with nothing hidden the toggle itself is gone.
    val revealed = showHidden && hiddenSources.isNotEmpty()
    val rows = if (revealed) shown + hiddenSources else shown

    // Focus the first row once the list arrives, or the control below it when every input is hidden.
    LaunchedEffect(list != null) {
        if (list != null) pendingFocus = rows.firstOrNull()?.let { rowKey(it.id) } ?: if (hiddenSources.isNotEmpty()) TOGGLE else ANDROID
    }
    LaunchedEffect(pendingFocus) {
        val target = pendingFocus ?: return@LaunchedEffect
        // The target may only exist after the hidden set round-trips through the config store.
        var tries = 0
        while (tries < 20 && runCatching { requester(target).requestFocus() }.isFailure) {
            delay(50)
            tries++
        }
        pendingFocus = null
    }
    // Back closes the inline action first; the panel itself closes on the next Back.
    BackHandler(enabled = actionFor != null) {
        pendingFocus = actionFor?.let(::rowKey)
        actionFor = null
    }

    /** Hides or shows [source], then keeps focus where the user is: on the same row if it stays, else a neighbour. */
    fun apply(source: InputSource, hide: Boolean) {
        val index = rows.indexOfFirst { it.id == source.id }
        val stays = !hide || showHidden
        val neighbour = rows.getOrNull(index + 1) ?: rows.getOrNull(index - 1)
        val lastOneGone = hide && !showHidden && rows.size == 1
        pendingFocus = when {
            stays -> rowKey(source.id)
            neighbour != null && neighbour.id != source.id -> rowKey(neighbour.id)
            lastOneGone -> TOGGLE
            else -> ANDROID
        }
        actionFor = null
        if (hide) onHide(source) else onShow(source)
    }

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
            val hint = when {
                list != null && list.isEmpty() -> R.string.panel_inputs_empty
                list != null && shown.isEmpty() -> R.string.panel_inputs_all_hidden
                else -> R.string.panel_inputs_hint
            }
            BasicText(stringResource(hint), style = TextStyle(JpColors.Muted, 9.sp, fontFamily = JpFonts.Body))
            if (!list.isNullOrEmpty()) {
                BasicText(
                    stringResource(R.string.panel_inputs_hide_hint),
                    style = TextStyle(JpColors.Muted, 9.sp, fontFamily = JpFonts.Body),
                )
            }
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
                rows.forEachIndexed { i, source ->
                    val isHidden = source.id in hidden
                    // The row and its inline action are one focus unit: leaving both closes the action.
                    // The short delay rides out the moment focus is in transit from the row to the action.
                    var unitFocused by remember(source.id) { mutableStateOf(true) }
                    LaunchedEffect(unitFocused, actionFor) {
                        if (!unitFocused && actionFor == source.id) {
                            delay(150)
                            actionFor = null
                        }
                    }
                    Column(
                        Modifier.fillMaxWidth().onFocusChanged { unitFocused = it.hasFocus },
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        GlassButton(
                            label = rowText(source, list.orEmpty(), isHidden),
                            accent = accent,
                            onClick = { openFailed = !onOpen(source) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = if (isHidden) 0.5f else 1f }
                                .focusRequester(requester(rowKey(source.id))),
                            primary = i == 0 && !isHidden,
                            onLongPress = { actionFor = source.id },
                        )
                        if (actionFor == source.id) {
                            val actionFocus = requester("action")
                            LaunchedEffect(Unit) { runCatching { actionFocus.requestFocus() } }
                            GlassButton(
                                label = stringResource(if (isHidden) R.string.panel_inputs_unhide else R.string.panel_inputs_hide),
                                accent = accent,
                                onClick = { apply(source, hide = !isHidden) },
                                modifier = Modifier.padding(start = 18.dp).focusRequester(actionFocus),
                            )
                        }
                    }
                }
            }
            if (hiddenSources.isNotEmpty()) {
                GlassButton(
                    label = stringResource(
                        if (revealed) R.string.panel_inputs_hide_hidden else R.string.panel_inputs_show_hidden,
                        hiddenSources.size,
                    ),
                    accent = accent,
                    onClick = {
                        showHidden = !revealed
                        actionFor = null
                        pendingFocus = TOGGLE
                    },
                    modifier = Modifier.fillMaxWidth().focusRequester(requester(TOGGLE)),
                )
            }
            GlassButton(
                label = stringResource(R.string.panel_inputs_android),
                accent = accent,
                onClick = onAndroidSettings,
                modifier = Modifier.focusRequester(requester(ANDROID)),
            )
        }
    }
}

/** "HDMI 2 · CONECTADO"; hidden ones end with "· OCULTA"; unnamed inputs are numbered among their own kind ("HDMI 1", "HDMI 2"). */
@Composable
private fun rowText(source: InputSource, all: List<InputSource>, hidden: Boolean): String {
    val kind = stringResource(kindName(source.kind))
    val sameKind = all.filter { it.kind == source.kind }
    val name = source.label ?: if (sameKind.size > 1) "$kind ${sameKind.indexOf(source) + 1}" else kind
    val signal = when (source.signal) {
        InputSource.Signal.CONNECTED -> stringResource(R.string.input_signal_connected)
        InputSource.Signal.STANDBY -> stringResource(R.string.input_signal_standby)
        InputSource.Signal.DISCONNECTED -> stringResource(R.string.input_signal_disconnected)
        InputSource.Signal.UNKNOWN -> null
    }
    val tag = if (hidden) stringResource(R.string.input_hidden_tag) else null
    return listOfNotNull(name.uppercase(), signal, tag).joinToString(" · ")
}

private fun kindName(kind: InputSource.Kind): Int = when (kind) {
    InputSource.Kind.HDMI -> R.string.input_kind_hdmi
    InputSource.Kind.COMPOSITE -> R.string.input_kind_composite
    InputSource.Kind.COMPONENT -> R.string.input_kind_component
    InputSource.Kind.VGA -> R.string.input_kind_vga
    InputSource.Kind.OTHER -> R.string.input_kind_other
}
