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

/**
 * Lets a remote without Tab/Menu-to-settings reach the extra controls (the "ajustes" pill and, in
 * themes that have one, the "temas" button) from a theme whose root owns D-pad focus, and come
 * back to the app list afterwards. Path: list -> settings control -> themes control.
 */
@Stable
class ThemeControlFocus internal constructor() {
    val list = FocusRequester()
    val control = FocusRequester()
    val themes = FocusRequester()
    var onControl by mutableStateOf(false)
    var onThemes by mutableStateOf(false)
}

@Composable
fun rememberThemeControlFocus(): ThemeControlFocus = remember { ThemeControlFocus() }

private val DPad = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

/**
 * Apply before [themeKeyNavigation]. [toControl] decides when a D-pad key on the list jumps
 * to the settings control; [toList] decides when a key on either control jumps back. A theme with
 * a themes control also passes [toThemes] (a key on the settings control that moves to it) and
 * [fromThemes] (a key on the themes control that moves back to settings). Other D-pad keys are
 * swallowed while on a control so the list index does not move behind it.
 */
fun Modifier.themeControlRouting(
    focus: ThemeControlFocus,
    toControl: (Key) -> Boolean,
    toList: (Key) -> Boolean,
    toThemes: (Key) -> Boolean = { false },
    fromThemes: (Key) -> Boolean = { false },
): Modifier = onPreviewKeyEvent { event ->
    val key = event.key
    when {
        event.type != KeyEventType.KeyDown -> false
        focus.onThemes -> when {
            fromThemes(key) -> { focus.control.requestFocus(); true }
            toList(key) -> { focus.list.requestFocus(); true }
            key in DPad -> true
            else -> false
        }
        focus.onControl -> when {
            toThemes(key) -> { focus.themes.requestFocus(); true }
            toList(key) -> { focus.list.requestFocus(); true }
            key in DPad -> true
            else -> false
        }
        toControl(key) -> { focus.control.requestFocus(); true }
        else -> false
    }
}

/** Marks a composable as the theme's focusable control, wired to [focus]. Follow with clickable. */
fun Modifier.themeControlTarget(focus: ThemeControlFocus): Modifier = this
    .focusRequester(focus.control)
    .onFocusChanged { focus.onControl = it.isFocused }

/** Marks a composable as the theme's themes control (opens the Themes screen), wired to [focus]. Follow with clickable. */
fun Modifier.themesControlTarget(focus: ThemeControlFocus): Modifier = this
    .focusRequester(focus.themes)
    .onFocusChanged { focus.onThemes = it.isFocused }
