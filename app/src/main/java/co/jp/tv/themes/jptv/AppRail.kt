package co.jp.tv.themes.jptv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.RailWindow

private const val VISIBLE_ROWS = 7
private val RailShape = RoundedCornerShape(18.dp)

@Composable
internal fun AppRail(state: HomeState, actions: HomeActions, modifier: Modifier = Modifier) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var previousTop by remember { mutableIntStateOf(0) }
    val count = state.apps.size
    val top = RailWindow.top(previousTop, state.focusedIndex, count, VISIBLE_ROWS)
    SideEffect { previousTop = top }

    Column(
        modifier = modifier
            .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
            .fillMaxHeight()
            .width(270.dp)
            .background(JpTvPalette.Glass, RailShape)
            .border(0.5.dp, JpTvPalette.Line, RailShape)
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 20.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event -> handleKey(event, state, actions) },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            BasicText(
                text = state.brand,
                style = TextStyle(color = JpTvPalette.Accent, fontSize = 30.sp),
            )
            BasicText(
                text = stringResource(R.string.rail_total_apps, count),
                style = monoLabel(),
            )
        }

        PagerLabel(
            up = true,
            text = if (RailWindow.above(top) > 0) stringResource(R.string.rail_above, RailWindow.above(top))
            else stringResource(R.string.rail_start),
        )

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            for (index in top until minOf(count, top + VISIBLE_ROWS)) {
                val app = state.apps[index]
                androidx.compose.runtime.key(app.packageName) {
                    AppRow(app, focused = index == state.focusedIndex)
                }
            }
        }

        val below = RailWindow.below(top, count, VISIBLE_ROWS)
        PagerLabel(
            up = false,
            text = if (below > 0) stringResource(R.string.rail_below, below) else stringResource(R.string.rail_end),
        )
    }
}

@Composable
private fun PagerLabel(up: Boolean, text: String) {
    Row(
        modifier = Modifier.height(22.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Chevron(up = up, color = JpTvPalette.Faint)
        BasicText(text = text, style = monoLabel())
    }
}

private fun monoLabel() = TextStyle(
    color = JpTvPalette.Faint,
    fontSize = 8.sp,
    fontFamily = FontFamily.Monospace,
    letterSpacing = 0.14.em,
)

private fun handleKey(event: androidx.compose.ui.input.key.KeyEvent, state: HomeState, actions: HomeActions): Boolean {
    if (event.type != KeyEventType.KeyDown) return false
    return when (event.key) {
        Key.DirectionUp -> { actions.focus(state.focusedIndex - 1); true }
        Key.DirectionDown -> { actions.focus(state.focusedIndex + 1); true }
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
            if (event.nativeKeyEvent.repeatCount == 0) state.apps.getOrNull(state.focusedIndex)?.let(actions::launch)
            true
        }
        else -> false
    }
}
