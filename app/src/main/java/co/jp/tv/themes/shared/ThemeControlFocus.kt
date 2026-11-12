package co.jp.tv.themes.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import co.jp.tv.R
import co.jp.tv.core.sound.LocalUiSounds
import co.jp.tv.core.sound.UiSound
import co.jp.tv.core.sound.UiSounds

/**
 * Lets a remote without Tab/Menu-to-settings reach the header controls from a theme whose root owns
 * D-pad focus, and come back to the app list afterwards. Path: list -> network -> settings -> themes.
 */
@Stable
class ThemeControlFocus internal constructor(
    /** Plays the control-focus sound; the theme's sound set decides how it sounds. */
    internal val sounds: UiSounds = UiSounds.None,
    /** Accessibility names of the three controls, in chain order (network, settings, themes). */
    internal val labels: List<String> = listOf("", "", ""),
) {
    val list = FocusRequester()
    val network = FocusRequester()
    val control = FocusRequester()
    val themes = FocusRequester()
    var onNetwork by mutableStateOf(false)
    var onControl by mutableStateOf(false)
    var onThemes by mutableStateOf(false)
}

@Composable
fun rememberThemeControlFocus(): ThemeControlFocus {
    val sounds = LocalUiSounds.current
    val labels = listOf(
        stringResource(R.string.cd_network),
        stringResource(R.string.cd_settings),
        stringResource(R.string.cd_themes),
    )
    return remember(sounds, labels) { ThemeControlFocus(sounds, labels) }
}

private val DPad = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

/**
 * Apply before [themeKeyNavigation]. The controls form a chain: network, settings, themes.
 * [toControl] decides when a D-pad key on the list jumps to the first control (network); [forward] moves
 * one control along the chain (network to settings to themes) and [backward] one step back; [toList]
 * decides when a key on any control jumps back to the list (also from the first control, [backward]).
 * Other D-pad keys are swallowed while on a control so the list index does not move behind it.
 */
fun Modifier.themeControlRouting(
    focus: ThemeControlFocus,
    toControl: (Key) -> Boolean,
    toList: (Key) -> Boolean,
    forward: (Key) -> Boolean = { false },
    backward: (Key) -> Boolean = { false },
): Modifier = onPreviewKeyEvent { event ->
    val key = event.key
    when {
        event.type != KeyEventType.KeyDown -> false
        focus.onThemes -> when {
            backward(key) -> { focus.control.requestFocus(); true }
            toList(key) -> { focus.list.requestFocus(); true }
            key in DPad -> true
            else -> false
        }
        focus.onControl -> when {
            forward(key) -> { focus.themes.requestFocus(); true }
            backward(key) -> { focus.network.requestFocus(); true }
            toList(key) -> { focus.list.requestFocus(); true }
            key in DPad -> true
            else -> false
        }
        focus.onNetwork -> when {
            forward(key) -> { focus.control.requestFocus(); true }
            backward(key) || toList(key) -> { focus.list.requestFocus(); true }
            key in DPad -> true
            else -> false
        }
        toControl(key) -> { focus.network.requestFocus(); true }
        else -> false
    }
}

/** Marks a composable as the theme's network control (opens the network panel), wired to [focus]. Follow with clickable. */
fun Modifier.networkControlTarget(focus: ThemeControlFocus): Modifier = this
    .semantics { contentDescription = focus.labels[0] }
    .focusRequester(focus.network)
    .onFocusChanged {
        if (it.isFocused && !focus.onNetwork) focus.sounds.play(UiSound.ControlFocus)
        focus.onNetwork = it.isFocused
    }

/** Marks a composable as the theme's settings control, wired to [focus]. Follow with clickable. */
fun Modifier.themeControlTarget(focus: ThemeControlFocus): Modifier = this
    .semantics { contentDescription = focus.labels[1] }
    .focusRequester(focus.control)
    .onFocusChanged {
        if (it.isFocused && !focus.onControl) focus.sounds.play(UiSound.ControlFocus)
        focus.onControl = it.isFocused
    }

/** Marks a composable as the theme's themes control (opens the Themes screen), wired to [focus]. Follow with clickable. */
fun Modifier.themesControlTarget(focus: ThemeControlFocus): Modifier = this
    .semantics { contentDescription = focus.labels[2] }
    .focusRequester(focus.themes)
    .onFocusChanged {
        if (it.isFocused && !focus.onThemes) focus.sounds.play(UiSound.ControlFocus)
        focus.onThemes = it.isFocused
    }
