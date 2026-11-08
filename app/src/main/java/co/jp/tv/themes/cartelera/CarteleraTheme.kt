package co.jp.tv.themes.cartelera

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
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

object CarteleraTheme : LauncherTheme {
    override val id = "cartelera"
    override val displayName = "Cartelera"
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
                .background(CarteleraPalette.Background)
                .focusRequester(focus.list)
                .themeControlRouting(
                    focus,
                    toControl = { it == Key.DirectionDown },
                    toList = { it == Key.DirectionUp },
                )
                .themeKeyNavigation(state.apps, state.focusedIndex, actions, ThemeNavAxis.Horizontal),
        ) {
            VelvetBackdrop()
            CarteleraSpotlight()
            Column(Modifier.fillMaxSize()) {
                CarteleraMarquee(state.brand, state.now)
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Bottom,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 80.dp),
                ) {
                    itemsIndexed(state.apps, key = { _, app -> app.packageName }) { index, app ->
                        CarteleraPoster(app, index, index == state.focusedIndex)
                    }
                }
                CarteleraFooter(state.network, state.now, actions, focus)
            }
        }
    }
}

@Composable
private fun VelvetBackdrop() {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(CarteleraPalette.VelvetA, CarteleraPalette.VelvetB, CarteleraPalette.VelvetA),
                ),
            ),
    )
}

@Composable
private fun CarteleraSpotlight() {
    val paused = LocalPaused.current
    val transition = rememberInfiniteTransition(label = "cartSpot")
    val sway by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(7000, easing = JpMotion.Entrance), RepeatMode.Reverse),
        label = "sway",
    )
    if (!paused) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(700.dp)
                .graphicsLayer { rotationZ = if (paused) 0f else sway }
                .background(
                    Brush.radialGradient(
                        listOf(CarteleraPalette.Spotlight, CarteleraPalette.Background.copy(alpha = 0f)),
                        radius = 600f,
                    ),
                ),
        )
    }
}

@Composable
private fun CarteleraMarquee(brand: String, now: java.time.LocalDateTime) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CarteleraPalette.MarqueeBg)
            .border(1.5.dp, CarteleraPalette.Gold, RoundedCornerShape(14.dp))
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BulbRow()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            BasicText(
                "$brand PRESENTA",
                style = TextStyle(color = CarteleraPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono),
            )
            BasicText(
                "ESTA NOCHE",
                style = TextStyle(color = CarteleraPalette.BulbOn, fontSize = 38.sp, fontFamily = JpFonts.Limelight),
            )
            BasicText(
                "FUNCIÓN ${ClockFormat.time(now)}",
                style = TextStyle(color = CarteleraPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono),
            )
        }
        BulbRow()
    }
}

@Composable
private fun BulbRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(24) { i ->
            val on = i % 3 == 0
            Box(
                Modifier
                    .size(6.dp)
                    .background(if (on) CarteleraPalette.BulbOn else CarteleraPalette.BulbOff, RoundedCornerShape(50)),
            )
        }
    }
}

@Composable
private fun CarteleraPoster(app: TvApp, index: Int, focused: Boolean) {
    val height = if (focused) 280.dp else 220.dp
    val width = if (focused) 156.dp else 122.dp
    val tilt = if (focused) 0f else if (index % 2 == 0) -4f else 4f
    Column(
        modifier = Modifier
            .width(width)
            .graphicsLayer { rotationZ = tilt; scaleX = if (focused) 1.06f else 1f; scaleY = if (focused) 1.06f else 1f }
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.verticalGradient(
                    listOf(CarteleraPalette.VelvetB, CarteleraPalette.MarqueeBg),
                ),
            )
            .border(if (focused) 2.dp else 1.dp, if (focused) CarteleraPalette.BulbOn else CarteleraPalette.Gold, RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        BasicText(
            "PÓSTER ${index + 1}",
            style = TextStyle(color = CarteleraPalette.Gold, fontSize = 6.5.sp, fontFamily = JpFonts.Mono),
        )
        BasicText(
            app.label.uppercase(),
            style = TextStyle(
                color = CarteleraPalette.Cream,
                fontSize = if (focused) 24.sp else 18.sp,
                fontFamily = JpFonts.Limelight,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CarteleraFooter(
    network: NetworkStatus,
    now: java.time.LocalDateTime,
    actions: HomeActions,
    focus: ThemeControlFocus,
) {
    val ms = network.routerLatencyMs ?: network.internetLatencyMs
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 80.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            "TAQUILLA · [RED WI-FI]${ms?.let { " · ${it} MS" }.orEmpty()}",
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                )
                .border(1.dp, CarteleraPalette.Gold, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = TextStyle(color = CarteleraPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono),
        )
        BasicText(
            ClockFormat.date(now),
            style = TextStyle(color = CarteleraPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono),
        )
        BasicText(
            stringResource(R.string.cd_settings).uppercase() + " →",
            modifier = Modifier
                .themeControlTarget(focus)
                .border(if (focus.onControl) 2.dp else 0.dp, CarteleraPalette.BulbOn, RoundedCornerShape(12.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openSettings,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = TextStyle(color = if (focus.onControl) CarteleraPalette.BulbOn else CarteleraPalette.Gold, fontSize = 8.sp, fontFamily = JpFonts.Mono),
        )
    }
}
