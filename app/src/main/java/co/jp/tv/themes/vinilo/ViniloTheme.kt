package co.jp.tv.themes.vinilo

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation

object ViniloTheme : LauncherTheme {
    override val id = "vinilo"
    override val displayName = "Vinilo"
    override val supportsAccent = false

    @Composable
    override fun Home(state: HomeState, actions: HomeActions) {
        val focus = rememberThemeControlFocus()
        LaunchedEffect(Unit) { focus.list.requestFocus() }
        val listState = rememberLazyListState()
        LaunchedEffect(state.focusedIndex) {
            if (state.apps.isNotEmpty()) listState.animateScrollToItem(state.focusedIndex)
        }
        val selected = state.apps.getOrNull(state.focusedIndex)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(ViniloPalette.BackgroundGlow, ViniloPalette.Background)))
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionRight || (it == Key.DirectionUp && state.focusedIndex <= 0) },
                    toList = { it == Key.DirectionDown || it == Key.DirectionLeft },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Vertical),
        ) {
            Column(Modifier.fillMaxSize()) {
                ViniloHeader(state.brand, state.network, state.now, state.config.hourFormat, actions, focus)
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        itemsIndexed(state.apps, key = { _, app -> app.packageName }) { index, app ->
                            ViniloRecordCard(app, index == state.focusedIndex, index)
                        }
                    }
                    ViniloTurntable(selected?.label.orEmpty(), state.focusedIndex)
                }
            }
        }
    }
}

@Composable
private fun ViniloHeader(
    brand: String,
    network: NetworkStatus,
    now: java.time.LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    actions: HomeActions,
    focus: ThemeControlFocus,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            "$brand Records",
            style = TextStyle(color = ViniloPalette.Orange, fontSize = 28.sp, fontFamily = JpFonts.Righteous),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val ms = network.routerLatencyMs ?: network.internetLatencyMs
            BasicText(
                "[RED WI-FI]${ms?.let { " · ${it} MS" }.orEmpty()}",
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
                style = TextStyle(color = ViniloPalette.Cream, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.16.em),
            )
            BasicText(
                ClockFormat.time(now, hourFormat),
                style = TextStyle(color = ViniloPalette.Cream, fontSize = 22.sp, fontFamily = JpFonts.Righteous),
            )
            BasicText(
                "AJUSTES",
                modifier = Modifier
                    .themeControlTarget(focus)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (focus.onControl) ViniloPalette.Gold.copy(alpha = 0.25f) else Color.Transparent)
                    .border(if (focus.onControl) 2.dp else 1.dp, ViniloPalette.Gold, RoundedCornerShape(12.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openSettings,
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                style = TextStyle(color = ViniloPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.16.em),
            )
        }
    }
}

@Composable
private fun ViniloRecordCard(app: TvApp, focused: Boolean, index: Int) {
    val labelColor = ViniloPalette.LabelColors[index % ViniloPalette.LabelColors.size]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ViniloPalette.Deck.copy(alpha = if (focused) 0.95f else 0.65f))
            .border(if (focused) 1.5.dp else 1.dp, if (focused) ViniloPalette.Gold else ViniloPalette.Orange.copy(0.35f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(ViniloPalette.Vinyl).border(2.dp, labelColor, CircleShape))
        Column {
            BasicText(
                app.label,
                style = TextStyle(color = ViniloPalette.Cream, fontSize = 11.sp, fontFamily = JpFonts.Body),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                "SIDE ${('A' + index % 26)}",
                style = TextStyle(color = ViniloPalette.Gold, fontSize = 6.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.16.em),
            )
        }
    }
}

@Composable
private fun ViniloTurntable(selected: String, focusedIndex: Int) {
    val paused = LocalPaused.current
    val labelColor = ViniloPalette.LabelColors[focusedIndex % ViniloPalette.LabelColors.size]
    val transition = rememberInfiniteTransition(label = "vinSpin")
    val spin by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    Column(
        Modifier
            .width(350.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(ViniloPalette.Deck, ViniloPalette.BackgroundGlow))),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(240.dp)
                    .graphicsLayer { rotationZ = if (paused) 0f else spin }
                    .clip(CircleShape)
                    .background(ViniloPalette.Vinyl)
                    .border(8.dp, ViniloPalette.Vinyl.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(labelColor),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        selected.take(8).uppercase(),
                        style = TextStyle(color = ViniloPalette.Background, fontSize = 8.sp, fontFamily = JpFonts.Righteous),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        BasicText(
            "AHORA SUENA · ${selected.uppercase()}",
            modifier = Modifier.padding(top = 12.dp),
            style = TextStyle(color = ViniloPalette.Gold, fontSize = 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.2.em),
        )
    }
}
