package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import co.jp.tv.R
import co.jp.tv.core.design.JpMotion
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

/**
 * Top-right cluster: network pill, settings (the theme's D-pad control), themes, clock.
 * Enters with `cy-in` (0.8s, entrance curve): fade in while rising 14 px.
 */
@Composable
internal fun CyberpunkTopBar(
    state: HomeState,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(CyberpunkMotion.EnterMs, easing = JpMotion.Entrance)) }
    Row(
        modifier.graphicsLayer {
            alpha = enter.value
            translationY = (1f - enter.value) * 14f * 0.5.dp.toPx()
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dpx),
    ) {
        NetworkPill(state.network, actions::openNetwork)
        RoundControl(
            icon = JpIcons.Settings,
            description = stringResource(R.string.cd_settings),
            border = CyberpunkPalette.Cyan.copy(alpha = 0.45f),
            iconColor = CyberpunkPalette.Cyan,
            focused = focus.onControl,
            onClick = actions::openSettings,
            modifier = Modifier.themeControlTarget(focus),
        )
        RoundControl(
            icon = JpIcons.Palette,
            description = stringResource(R.string.cd_themes),
            border = CyberpunkPalette.Pink.copy(alpha = 0.55f),
            iconColor = CyberpunkPalette.PinkSoft,
            focused = focus.onThemes,
            onClick = actions::openThemes,
            modifier = Modifier.themesControlTarget(focus),
        )
        WeatherBadge(
            weather = state.weather,
            color = CyberpunkPalette.Cyan,
            textStyle = TextStyle(
                fontSize = 22.spx,
                fontWeight = FontWeight.Medium,
                fontFamily = CyberpunkFonts.Display,
                letterSpacing = 0.04.em,
            ),
            iconSize = 28.dpx,
            gap = 8.dpx,
        )
        CyberpunkGlowText(
            ClockFormat.time(state.now, state.config.hourFormat),
            TextStyle(
                color = CyberpunkPalette.Ice,
                fontSize = 44.spx,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CyberpunkFonts.Display,
                letterSpacing = 0.02.em,
                lineHeight = 1.em,
            ),
            glows = listOf(CyberpunkPalette.Cyan.copy(alpha = 0.6f) to 18f),
            modifier = Modifier.padding(start = 10.dpx),
        )
    }
}

@Composable
private fun NetworkPill(network: NetworkStatus, onClick: () -> Unit) {
    val title = when {
        network.state == NetworkStatus.State.DISCONNECTED -> stringResource(R.string.network_offline)
        network.ssid != null -> network.ssid
        network.transport == NetworkStatus.Transport.WIFI -> stringResource(R.string.network_wifi)
        network.transport == NetworkStatus.Transport.ETHERNET -> stringResource(R.string.network_ethernet)
        else -> stringResource(R.string.network_generic)
    }
    val description = stringResource(R.string.cd_network)
    val shape = RoundedCornerShape(26.dpx)
    Row(
        Modifier
            .height(52.dpx)
            .clip(shape)
            .background(CyberpunkPalette.PillFill)
            .border(1.dpx, CyberpunkPalette.Cyan.copy(alpha = 0.45f), shape)
            .semantics { contentDescription = description }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(horizontal = 20.dpx),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dpx),
    ) {
        JpIconView(WifiIcon, 22.dpx, CyberpunkPalette.Cyan)
        BasicText(
            stringResource(R.string.theme_cyberpunk_network, title.uppercase()),
            style = TextStyle(
                color = CyberpunkPalette.Ice,
                fontSize = 18.spx,
                fontWeight = FontWeight.Medium,
                fontFamily = CyberpunkFonts.Display,
                letterSpacing = 0.04.em,
            ),
            maxLines = 1,
        )
    }
}

/** The design's wifi glyph is stroked at 2 px instead of the shared 1.8. */
private val WifiIcon = JpIcon(JpIcons.Wifi.paths, strokeWidth = 2f)

/**
 * 52 px round button. When focused it follows the artboard's `:focus-visible` rule:
 * 14 px corner radius and a 2 px cyan outline 6 px outside.
 */
@Composable
private fun RoundControl(
    icon: JpIcon,
    description: String,
    border: Color,
    iconColor: Color,
    focused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = if (focused) RoundedCornerShape(14.dpx) else CircleShape
    val outline = with(LocalDensity.current) { remember { Stroke(width = 2.dpx.toPx()) } }
    Box(
        modifier
            .size(52.dpx)
            .drawBehind {
                if (focused) {
                    val gap = 6.dpx.toPx() + outline.width / 2f
                    val r = 14.dpx.toPx() + gap
                    drawRoundRect(
                        CyberpunkPalette.Cyan,
                        Offset(-gap, -gap),
                        Size(size.width + 2f * gap, size.height + 2f * gap),
                        CornerRadius(r, r),
                        style = outline,
                    )
                }
            }
            .clip(shape)
            .background(CyberpunkPalette.PillFill)
            .border(1.dpx, border, shape)
            .semantics { contentDescription = description }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        JpIconView(icon, 24.dpx, iconColor)
    }
}
