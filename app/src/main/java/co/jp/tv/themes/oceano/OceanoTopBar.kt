package co.jp.tv.themes.oceano

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.jptv.JpIcon
import co.jp.tv.themes.jptv.JpIconView
import co.jp.tv.themes.jptv.JpIcons
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.HomeState
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themesControlTarget
import co.jp.tv.weather.ui.WeatherBadge

/** A hard, blur-free text shadow [dy] design px down and right, scaled to the screen. */
@Composable
private fun rememberTextShadow(color: Color, dy: Float): Shadow {
    val toPx = with(LocalDensity.current) { OceanoDesign.dp(1f).toPx() }
    return remember(color, dy, toPx) { Shadow(color, Offset(dy * toPx, dy * toPx), 0f) }
}

/** "JP-TV" at the top left in the pixel font, rising in over 0.9 s. */
@Composable
internal fun OceanoBrand(brand: String, modifier: Modifier = Modifier) {
    val shadow = rememberTextShadow(OceanoPalette.TextShadow, dy = 3f)
    BasicText(
        brand,
        modifier = modifier.riseIn(0, OceanoMotion.EntranceMs, distanceDp = 7f),
        style = TextStyle(
            color = OceanoPalette.White,
            fontSize = 30.sp,
            lineHeight = 30.sp,
            fontFamily = OceanoType.Pixel,
            shadow = shadow,
        ),
    )
}

/**
 * Network link, settings, themes and the clock at the top right, rising in 0.12 s after the
 * brand. Settings is the theme's one extra focus target ([focus]).
 */
@Composable
internal fun OceanoTopRight(
    state: HomeState,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val shadow = rememberTextShadow(OceanoPalette.TextShadow, dy = 3f)
    Row(
        modifier.riseIn(OceanoMotion.EntranceStaggerMs, OceanoMotion.EntranceMs, distanceDp = 7f),
        horizontalArrangement = Arrangement.spacedBy(OceanoDesign.dp(32f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkLink(state.network, shadow, actions::openNetwork)
        IconButton(
            icon = JpIcons.Settings,
            description = stringResource(R.string.cd_settings),
            highlighted = focus.onControl,
            onClick = actions::openSettings,
            modifier = Modifier.themeControlTarget(focus),
        )
        IconButton(
            icon = JpIcons.Palette,
            description = stringResource(R.string.cd_themes),
            highlighted = focus.onThemes,
            onClick = actions::openThemes,
            modifier = Modifier.themesControlTarget(focus),
        )
        WeatherBadge(
            weather = state.weather,
            color = OceanoPalette.White,
            textStyle = TextStyle(fontSize = 17.sp, fontFamily = OceanoType.Pixel, shadow = shadow),
            iconSize = OceanoDesign.dp(28f),
            gap = OceanoDesign.dp(10f),
        )
        OceanoTime(state, shadow)
    }
}

@Composable
private fun NetworkLink(network: NetworkStatus, shadow: Shadow, onClick: () -> Unit) {
    val label = when {
        network.state == NetworkStatus.State.DISCONNECTED -> stringResource(R.string.network_offline)
        network.ssid != null -> network.ssid
        network.transport == NetworkStatus.Transport.WIFI -> stringResource(R.string.network_wifi)
        network.transport == NetworkStatus.Transport.ETHERNET -> stringResource(R.string.network_ethernet)
        else -> stringResource(R.string.network_generic)
    }
    val description = stringResource(R.string.cd_network)
    Row(
        Modifier
            .height(OceanoDesign.dp(48f))
            .semantics { contentDescription = description }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(OceanoDesign.dp(12f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JpIconView(JpIcons.Wifi, OceanoDesign.dp(24f), OceanoPalette.White)
        BasicText(
            label,
            style = TextStyle(
                color = OceanoPalette.White,
                fontSize = 17.sp,
                fontFamily = OceanoType.Pixel,
                shadow = shadow,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun IconButton(
    icon: JpIcon,
    description: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(OceanoDesign.dp(48f))
            .semantics { contentDescription = description }
            .background(if (highlighted) OceanoPalette.ControlFocus else Color.Transparent)
            .border(OceanoDesign.dp(3f), if (highlighted) OceanoPalette.White else Color.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        JpIconView(icon, OceanoDesign.dp(26f), OceanoPalette.White)
    }
}

/** The clock in the pixel font; a 12-hour clock adds a small AM/PM beside the digits. */
@Composable
private fun OceanoTime(state: HomeState, shadow: Shadow) {
    val parts = ClockFormat.parts(state.now, state.config.hourFormat)
    Row {
        BasicText(
            "${parts.hours}:${parts.minutes}",
            modifier = Modifier.alignByBaseline(),
            style = TextStyle(
                color = OceanoPalette.White,
                fontSize = 30.sp,
                lineHeight = 30.sp,
                fontFamily = OceanoType.Pixel,
                shadow = shadow,
            ),
        )
        parts.meridiem?.let {
            BasicText(
                " $it",
                modifier = Modifier.alignByBaseline(),
                style = TextStyle(
                    color = OceanoPalette.White,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    fontFamily = OceanoType.Pixel,
                    shadow = shadow,
                ),
            )
        }
    }
}
