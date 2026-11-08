package co.jp.tv.themes.shared

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import co.jp.tv.apps.domain.TvApp

enum class ThemeNavAxis { Vertical, Horizontal }

class ThemeOkPress internal constructor() {
    var pending = false
}

/** D-pad handling shared by fixed-palette home themes. */
@Composable
fun Modifier.themeKeyNavigation(
    apps: List<TvApp>,
    focusedIndex: Int,
    actions: HomeActions,
    axis: ThemeNavAxis,
    onBeforeLaunch: () -> Boolean = { false },
    onMenu: () -> Unit = { actions.openSettings() },
): Modifier {
    val currentApps by rememberUpdatedState(apps)
    val currentFocus by rememberUpdatedState(focusedIndex)
    val okPress = remember { ThemeOkPress() }
    return focusable()
        .onKeyEvent { event ->
            handleThemeKey(event, currentApps, currentFocus, actions, axis, onBeforeLaunch, onMenu, okPress)
        }
}

private fun handleThemeKey(
    event: KeyEvent,
    apps: List<TvApp>,
    focusedIndex: Int,
    actions: HomeActions,
    axis: ThemeNavAxis,
    onBeforeLaunch: () -> Boolean,
    onMenu: () -> Unit,
    ok: ThemeOkPress,
): Boolean {
    val key = event.key
    val isOk = key == Key.DirectionCenter || key == Key.Enter || key == Key.NumPadEnter
  return when {
        isOk && event.type == KeyEventType.KeyDown -> {
            val native = event.nativeKeyEvent
            if (native.repeatCount == 0) {
                ok.pending = true
            } else if (native.isLongPress && ok.pending) {
                ok.pending = false
                actions.openEditApps(apps.getOrNull(focusedIndex))
            }
            true
        }
        isOk && event.type == KeyEventType.KeyUp -> {
            if (ok.pending) {
                ok.pending = false
                if (!onBeforeLaunch()) {
                    apps.getOrNull(focusedIndex)?.let(actions::launch)
                }
            }
            true
        }
        event.type != KeyEventType.KeyDown -> false
        key == Key.Menu -> {
            actions.openEditApps(apps.getOrNull(focusedIndex))
            true
        }
        key == Key.Tab || key == Key.ButtonStart -> {
            onMenu()
            true
        }
        key == Key.DirectionUp && axis == ThemeNavAxis.Vertical -> {
            actions.focus(focusedIndex - 1)
            true
        }
        key == Key.DirectionDown && axis == ThemeNavAxis.Vertical -> {
            actions.focus(focusedIndex + 1)
            true
        }
        key == Key.DirectionLeft && axis == ThemeNavAxis.Horizontal -> {
            actions.focus(focusedIndex - 1)
            true
        }
        key == Key.DirectionRight && axis == ThemeNavAxis.Horizontal -> {
            actions.focus(focusedIndex + 1)
            true
        }
        else -> false
    }
}
