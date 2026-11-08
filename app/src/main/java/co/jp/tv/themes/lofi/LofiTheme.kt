package co.jp.tv.themes.lofi

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeKeyNavigation

object LofiTheme : LauncherTheme {
    override val id = "lofi"
    override val displayName = "Lo-fi"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val listState = rememberLazyListState()
        LaunchedEffect(state.focusedIndex) {
            if (state.apps.isNotEmpty()) listState.animateScrollToItem(state.focusedIndex)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(LofiPalette.BackgroundTop, LofiPalette.BackgroundMid, LofiPalette.BackgroundBottom),
                    ),
                )
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionRight || (it == Key.DirectionDown && state.focusedIndex >= state.apps.lastIndex) },
                    toList = { it == Key.DirectionUp || it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            LofiDesk()
            LofiWindow()
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(start = 60.dp, end = 40.dp, top = 60.dp, bottom = 96.dp),
            ) {
                Box(Modifier.weight(1.2f))
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BasicText(
                        ClockFormat.time(state.now),
                        style = TextStyle(color = LofiPalette.Paper, fontSize = 36.sp, fontFamily = JpFonts.Caveat),
                    )
                    BasicText(
                        ClockFormat.date(state.now),
                        style = TextStyle(color = LofiPalette.Accent, fontSize = 14.sp, fontFamily = JpFonts.Caveat),
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(state.apps, key = { _, app -> app.packageName }) { index, app ->
                            LofiListItem(app, index == state.focusedIndex, index)
                        }
                    }
                }
            }
            LofiBottomBar(state.network, actions, focus, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun LofiBottomBar(network: NetworkStatus, actions: HomeActions, focus: ThemeControlFocus, modifier: Modifier = Modifier) {
    val ms = NetworkRules.formatLatency(network.routerLatencyMs ?: network.internetLatencyMs)
    val net = network.ssid ?: stringResource(R.string.network_wifi)
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 40.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            stringResource(R.string.theme_lofi_status, net, ms),
            style = TextStyle(color = LofiPalette.Paper.copy(alpha = 0.8f), fontSize = 12.sp, fontFamily = JpFonts.Caveat, letterSpacing = 0.12.em),
        )
        val shape = RoundedCornerShape(26.dp)
        BasicText(
            stringResource(R.string.cd_settings).lowercase(),
            modifier = Modifier
                .focusRequester(focus.control)
                .onFocusChanged { focus.onControl = it.isFocused }
                .clip(shape)
                .background(LofiPalette.Paper.copy(alpha = if (focus.onControl) 0.25f else 0.1f))
                .border(1.5.dp, if (focus.onControl) LofiPalette.Accent else Color.Transparent, shape)
                .clickable(onClick = actions::openSettings)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            style = TextStyle(color = LofiPalette.Paper, fontSize = 15.sp, fontFamily = JpFonts.Caveat, letterSpacing = 0.12.em),
        )
    }
}

@Composable
private fun BoxScope.LofiDesk() {
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(160.dp)
            .background(Brush.verticalGradient(listOf(LofiPalette.Desk, LofiPalette.BackgroundBottom))),
    )
}

@Composable
private fun LofiWindow() {
    val paused = LocalPaused.current
    val transition = rememberInfiniteTransition(label = "lofiRain")
    val drift by transition.animateFloat(
        0f,
        120f,
        infiniteRepeatable(tween(4200, easing = JpMotion.Entrance), RepeatMode.Restart),
        label = "rain",
    )
    Box(
        Modifier
            .padding(start = 60.dp, top = 60.dp)
            .width(380.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(LofiPalette.Card, LofiPalette.Accent.copy(alpha = 0.65f), LofiPalette.Accent),
                ),
            )
            .border(9.dp, LofiPalette.WindowFrame, RoundedCornerShape(12.dp)),
    ) {
        if (!paused) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .graphicsLayer { translationY = drift }
                    .background(LofiPalette.Paper.copy(alpha = 0.15f)),
            )
        }
        Box(
            Modifier
                .align(Alignment.Center)
                .width(8.dp)
                .fillMaxHeight()
                .background(LofiPalette.WindowFrame),
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(7.dp)
                .background(LofiPalette.WindowFrame),
        )
    }
}

@Composable
private fun LofiListItem(app: TvApp, focused: Boolean, index: Int) {
    val tilt = if (focused) 0f else if (index % 2 == 0) -2f else 2f
    BasicText(
        app.label,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { rotationZ = tilt }
            .clip(RoundedCornerShape(10.dp))
            .background(if (focused) LofiPalette.Accent.copy(alpha = 0.35f) else LofiPalette.Card.copy(alpha = 0.7f))
            .border(
                width = if (focused) 1.5.dp else 1.dp,
                color = if (focused) LofiPalette.Accent else LofiPalette.Paper.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        style = TextStyle(
            color = LofiPalette.Paper,
            fontSize = if (focused) 22.sp else 18.sp,
            fontFamily = JpFonts.Caveat,
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
