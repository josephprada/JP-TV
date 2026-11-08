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
 * Lets a remote without Tab/Menu-to-settings reach one extra control (the "ajustes" pill)
 * from a theme whose root owns D-pad focus, and come back to the app list afterwards.
 */
@Stable
class ThemeControlFocus internal constructor() {
    val list = FocusRequester()
    val control = FocusRequester()
    var onControl by mutableStateOf(false)
}

@Composable
fun rememberThemeControlFocus(): ThemeControlFocus = remember { ThemeControlFocus() }

private val DPad = setOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)

/**
 * Apply before [themeKeyNavigation]. [toControl] decides when a D-pad key on the list jumps
 * to the control; [toList] decides when a key on the control jumps back. Other D-pad keys
 * are swallowed while on the control so the list index does not move behind it.
 */
fun Modifier.themeControlRouting(
    focus: ThemeControlFocus,
    toControl: (Key) -> Boolean,
    toList: (Key) -> Boolean,
): Modifier = onPreviewKeyEvent { event ->
    val key = event.key
    when {
        event.type != KeyEventType.KeyDown -> false
        focus.onControl -> when {
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
