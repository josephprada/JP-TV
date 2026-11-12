package co.jp.tv.themes.jptv

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.core.design.riseIn
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.RailWindow
import kotlinx.coroutines.delay

private const val VISIBLE_ROWS = 7

/** Takes plain values (not HomeState) so the per-second clock tick does not recompose the rail. */
@Composable
internal fun AppRail(
    brand: String,
    apps: List<TvApp>,
    focusedIndex: Int,
    accent: Color,
    titleFontId: String,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    val titleFont = rememberTitleFont(titleFontId)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Rows stagger in only on the first pass; rows revealed by scrolling appear immediately.
    var firstPass by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1500)
        firstPass = false
    }

    var previousTop by remember { mutableIntStateOf(0) }
    val count = apps.size
    val top = RailWindow.top(previousTop, focusedIndex, count, VISIBLE_ROWS)
    SideEffect { previousTop = top }

    // Latest values for the key handler, which is created once.
    val currentApps by rememberUpdatedState(apps)
    val currentFocus by rememberUpdatedState(focusedIndex)
    val okPress = remember { OkPress() }
    val backPress = remember { BackPress() }

    Column(
        modifier = modifier
            .padding(start = 20.dp, top = 20.dp, bottom = 20.dp)
            .fillMaxHeight()
            .width(270.dp)
            .riseIn(delayMs = 0, durationMs = JpMotion.FadeMs, distanceDp = 0f)
            .glass(JpDimens.RailShape)
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 20.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { handleKey(it, currentApps, currentFocus, actions, okPress, backPress) },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            BasicText(
                text = brand,
                style = TextStyle(color = accent, fontSize = 30.sp, fontFamily = titleFont, letterSpacing = (-0.01).em),
            )
            BasicText(text = stringResource(R.string.rail_total_apps, count), style = monoLabel(fontSize = 7.sp, letterSpacing = 0.2.em))
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
                val app = apps[index]
                key(app.packageName) {
                    val delayMs = if (firstPass) 150 + (index - top) * JpMotion.RowStaggerMs else null
                    AppRow(app, focused = index == focusedIndex, accent = accent, riseDelayMs = delayMs)
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
        Chevron(up = up, color = JpColors.Faint)
        BasicText(text = text, style = monoLabel())
    }
}

/** Design: header count 14px / 0.2em, pager labels 13px / 0.14em (dp = px / 2). */
private fun monoLabel(fontSize: TextUnit = 6.5.sp, letterSpacing: TextUnit = 0.14.em) = TextStyle(
    color = JpColors.Faint,
    fontSize = fontSize,
    fontFamily = JpFonts.Mono,
    letterSpacing = letterSpacing,
)

/** Tracks an OK press so a short press launches on release and a long press opens the editor instead. */
private class OkPress { var pending = false }

/** Same Back gesture as the other themes (see themeKeyNavigation): long press opens Settings. */
private class BackPress { var pending = false }

private fun handleKey(event: KeyEvent, apps: List<TvApp>, focusedIndex: Int, actions: HomeActions, ok: OkPress, back: BackPress): Boolean {
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
                apps.getOrNull(focusedIndex)?.let(actions::launch)
            }
            true
        }
        key == Key.Back && event.type == KeyEventType.KeyDown -> {
            val native = event.nativeKeyEvent
            if (native.repeatCount == 0) {
                back.pending = true
            } else if (native.isLongPress && back.pending) {
                back.pending = false
                actions.openSettings()
            }
            true
        }
        key == Key.Back && event.type == KeyEventType.KeyUp -> {
            back.pending = false
            true
        }
        event.type != KeyEventType.KeyDown -> false
        key == Key.Menu -> { actions.openEditApps(apps.getOrNull(focusedIndex)); true }
        key == Key.DirectionUp -> { actions.focus(focusedIndex - 1); true }
        key == Key.DirectionDown -> { actions.focus(focusedIndex + 1); true }
        else -> false
    }
}
