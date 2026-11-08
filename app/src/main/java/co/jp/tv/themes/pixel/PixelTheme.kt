package co.jp.tv.themes.pixel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.network.ui.SignalBars
import co.jp.tv.themes.LauncherTheme
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.ThemeNavAxis
import co.jp.tv.themes.shared.rememberThemeControlFocus
import co.jp.tv.themes.shared.themeControlRouting
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themeKeyNavigation
import java.time.LocalDateTime

object PixelTheme : LauncherTheme {
    override val id = "pixel"
    override val displayName = "Pixel"
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
                .background(PixelPalette.Background)
                .pixelScanlines()
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionUp },
                    toList = { it == Key.DirectionDown },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            PixelGround()
            Column(Modifier.fillMaxSize()) {
                PixelTopBar(state.brand, state.network, state.now, actions, focus)
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    itemsIndexed(state.apps, key = { _, app -> app.packageName }) { index, app ->
                        PixelCartridge(app, focused = index == state.focusedIndex, index = index)
                    }
                }
            }
            val focused = state.apps.getOrNull(state.focusedIndex)
            if (focused != null) {
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BasicText(
                        "▶ ${focused.label.uppercase()}",
                        style = TextStyle(color = PixelPalette.Gold, fontSize = 22.sp, fontFamily = JpFonts.PressStart2P),
                    )
                    BasicText(
                        stringResource(R.string.theme_pixel_launch),
                        style = TextStyle(color = PixelPalette.Paper, fontSize = 9.sp, fontFamily = JpFonts.PressStart2P),
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelTopBar(
    brand: String,
    network: NetworkStatus,
    now: LocalDateTime,
    actions: HomeActions,
    focus: ThemeControlFocus,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            "P1 ★ $brand",
            style = TextStyle(color = PixelPalette.Gold, fontSize = 14.sp, fontFamily = JpFonts.PressStart2P),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    if (network.transport == NetworkStatus.Transport.ETHERNET) "ETH" else "WIFI",
                    style = TextStyle(color = PixelPalette.Green, fontSize = 9.sp, fontFamily = JpFonts.PressStart2P),
                )
                SignalBars(level = network.signalLevel.coerceIn(0, 4))
                val ms = network.routerLatencyMs ?: network.internetLatencyMs
                if (ms != null) {
                    BasicText(
                        "${ms}MS",
                        style = TextStyle(color = PixelPalette.Green, fontSize = 9.sp, fontFamily = JpFonts.PressStart2P),
                    )
                }
            }
            BasicText(
                ClockFormat.time(now),
                style = TextStyle(color = PixelPalette.Cyan, fontSize = 18.sp, fontFamily = JpFonts.PressStart2P),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .themeControlTarget(focus)
                    .background(PixelPalette.Panel)
                    .border(if (focus.onControl) 3.dp else 0.dp, PixelPalette.Gold)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = actions::openSettings,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                BasicText("⚙", style = TextStyle(color = PixelPalette.Paper, fontSize = 11.sp, fontFamily = JpFonts.PressStart2P))
            }
        }
    }
}

@Composable
private fun PixelCartridge(app: TvApp, focused: Boolean, index: Int) {
    val shell = PixelPalette.CartridgeColors[index % PixelPalette.CartridgeColors.size]
    val lift by animateFloatAsState(
        if (focused) -20f else 0f,
        tween(JpMotion.FocusMs, easing = JpMotion.Entrance),
        label = "pixelLift",
    )
    val width = if (focused) 150.dp else 120.dp
    val height = if (focused) 180.dp else 150.dp
    val darkText = shell == PixelPalette.CartridgeColors[2] || shell == PixelPalette.CartridgeColors[3]
    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .offset(y = lift.dp)
            .drawBehind {
                drawRect(PixelPalette.Shadow, topLeft = Offset(6.dp.toPx(), 6.dp.toPx()), size = size)
            }
            .background(shell)
            .then(if (focused) Modifier.border(4.dp, PixelPalette.Gold).padding(4.dp) else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.fillMaxWidth().height(if (focused) 75.dp else 55.dp).background(shell.copy(alpha = 0.55f)))
        BasicText(
            app.label.uppercase(),
            style = TextStyle(
                color = if (darkText) PixelPalette.Background else PixelPalette.Paper,
                fontSize = if (focused) 13.sp else 9.sp,
                fontFamily = JpFonts.PressStart2P,
            ),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BoxScope.PixelGround() {
    Column(Modifier.align(Alignment.BottomCenter)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color(0xFF29366F))
                .drawBehind {
                    drawRect(Color(0xFF3B5DC9), size = Size(size.width, 4.dp.toPx()))
                },
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF257179))
                .drawBehind {
                    drawRect(Color(0xFF38B764), size = Size(size.width, 4.dp.toPx()))
                },
        )
    }
}

private fun Modifier.pixelScanlines(): Modifier = drawBehind {
    var y = 0f
    while (y < size.height) {
        drawRect(PixelPalette.Shadow.copy(alpha = 0.12f), topLeft = Offset(0f, y), size = Size(size.width, 1f))
        y += 3f
    }
}
