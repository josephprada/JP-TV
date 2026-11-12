package co.jp.tv.themes.multiverso

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.themes.shared.networkName
import co.jp.tv.themes.shared.controlOutline
import co.jp.tv.themes.shared.networkControlTarget
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
import co.jp.tv.weather.ui.WeatherBadge
import co.jp.tv.themes.shared.themesControlTarget

/**
 * Brand at the top left; network link, Settings (the D-pad control), Themes and the clock at the
 * top right. Both groups rise in with `mv-in` (0.9 s, the second one 0.12 s later).
 */
@Composable
internal fun MultiversoHeader(
    state: HomeState,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val designPx = LocalDensity.current.density / 2f
    Box(modifier.fillMaxWidth()) {
        BasicText(
            state.brand,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 48.dp, top = 30.dp)
                .riseIn(delayMs = 0, durationMs = MultiversoMotion.EntranceMs, distanceDp = 7f),
            style = TextStyle(
                color = MultiversoPalette.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MultiversoFonts.Geist,
                letterSpacing = (-0.01).em,
                shadow = Shadow(MultiversoPalette.BrandGlow, Offset.Zero, 28f * designPx),
            ),
            maxLines = 1,
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 48.dp, top = 24.dp)
                .riseIn(delayMs = 120, durationMs = MultiversoMotion.EntranceMs, distanceDp = 7f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NetworkLink(state.network, actions, focus)
            HeaderIcon(
                icon = SettingsIcon,
                description = stringResource(R.string.cd_settings),
                focused = focus.onControl,
                onClick = actions::openSettings,
                modifier = Modifier.themeControlTarget(focus),
            )
            HeaderIcon(
                icon = ThemesIcon,
                description = stringResource(R.string.cd_themes),
                focused = focus.onThemes,
                onClick = actions::openThemes,
                modifier = Modifier.themesControlTarget(focus),
            )
            WeatherBadge(
                weather = state.weather,
                color = MultiversoPalette.TextSoft,
                textStyle = TextStyle(fontSize = 12.sp, fontFamily = MultiversoFonts.Geist),
                iconSize = 14.dp,
            )
            BasicText(
                ClockFormat.time(state.now, state.config.hourFormat),
                style = TextStyle(
                    color = MultiversoPalette.Text,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = MultiversoFonts.Geist,
                    letterSpacing = (-0.02).em,
                    lineHeight = 1.em,
                ),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/** Wi-Fi glyph and the network name; the artboard's "[RED WI-FI]" placeholder shows the real name. */
@Composable
private fun NetworkLink(network: NetworkStatus, actions: HomeActions, focus: ThemeControlFocus) {
    val name = networkName(network)
    val description = stringResource(R.string.cd_network)
    Row(
        modifier = Modifier
            .heightIn(min = 24.dp)
            .semantics { contentDescription = description }
            .networkControlTarget(focus)
            .controlOutline(focus.onNetwork, MultiversoPalette.White, corner = 6.dp, outset = 4.dp, width = 1.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = actions::openNetwork,
            ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JpIconView(WifiIcon, 12.dp, MultiversoPalette.TextSoft)
        BasicText(
            name,
            style = TextStyle(
                color = MultiversoPalette.TextSoft,
                fontSize = 9.5.sp,
                fontFamily = MultiversoFonts.Geist,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * 48 px icon button. While [focused], draws the artboard's `:focus-visible` ring: 2 px white,
 * 6 px outside the button, round.
 */
@Composable
private fun HeaderIcon(
    icon: JpIcon,
    description: String,
    focused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .semantics { contentDescription = description }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .drawWithCache {
                val ring = Stroke(1.dp.toPx())
                val radius = size.minDimension / 2f + 3.dp.toPx() + ring.width / 2f
                onDrawWithContent {
                    drawContent()
                    if (focused) drawCircle(MultiversoPalette.White, radius = radius, style = ring)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        JpIconView(icon, 13.dp, MultiversoPalette.TextSoft)
    }
}

// The artboard's icons are JP-TV's own glyphs with thinner strokes (1.7 and 1.6).
private val WifiIcon = JpIcon(JpIcons.Wifi.paths, strokeWidth = 1.7f)
private val SettingsIcon = JpIcon(JpIcons.Settings.paths, strokeWidth = 1.6f)
private val ThemesIcon = JpIcon(JpIcons.Palette.paths, strokeWidth = 1.6f)
