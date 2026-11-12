package co.jp.tv.themes.gotham

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.themes.shared.networkControlTarget
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.networkName
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.themeControlTarget
import co.jp.tv.themes.shared.themesControlTarget
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.ui.WeatherBadge

private const val ChromeAlpha = 0.85f

/**
 * Top-right cluster: network link, Settings (the theme's D-pad control), Themes and the clock.
 * Settings and Themes are D-pad controls; Network is pointer-only, as in the other fixed themes.
 */
@Composable
internal fun GothamTopBar(
    network: NetworkStatus,
    weather: Weather?,
    clock: String,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val chrome = GothamPalette.Text.copy(alpha = ChromeAlpha)
    val networkDescription = stringResource(R.string.cd_network)
    val settingsDescription = stringResource(R.string.cd_settings)
    val themesDescription = stringResource(R.string.cd_themes)
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            Modifier
                .heightIn(min = 24.dp)
                .semantics { contentDescription = networkDescription }
                .focusOutline(visible = focus.onNetwork)
                .networkControlTarget(focus)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openNetwork,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            GothamIcon(GothamGlyph.Wifi, size = 13.dp, color = chrome)
            BasicText(
                networkName(network).uppercase(),
                style = TextStyle(color = chrome, fontSize = 9.sp, fontFamily = JpFonts.SpecialElite, letterSpacing = 0.08.em),
                maxLines = 1,
            )
        }
        Box(
            Modifier
                .size(24.dp)
                .semantics { contentDescription = settingsDescription }
                .focusOutline(visible = focus.onControl)
                .themeControlTarget(focus)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openSettings,
                ),
            contentAlignment = Alignment.Center,
        ) {
            GothamIcon(GothamGlyph.Settings, size = 14.dp, color = chrome)
        }
        Box(
            Modifier
                .size(24.dp)
                .semantics { contentDescription = themesDescription }
                .focusOutline(visible = focus.onThemes)
                .themesControlTarget(focus)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openThemes,
                ),
            contentAlignment = Alignment.Center,
        ) {
            GothamIcon(GothamGlyph.Palette, size = 14.dp, color = chrome)
        }
        WeatherBadge(
            weather = weather,
            color = chrome,
            textStyle = TextStyle(fontSize = 11.sp, fontFamily = JpFonts.SpecialElite, letterSpacing = 0.08.em),
            iconSize = 15.dp,
            gap = 5.dp,
        )
        BasicText(
            clock,
            style = TextStyle(
                color = GothamPalette.Text,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                fontFamily = JpFonts.BebasNeue,
                letterSpacing = 0.06.em,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun Modifier.pointerOnlyClick(onClick: () -> Unit): Modifier = this
    .focusProperties { canFocus = false }
    .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    )

/** `:focus-visible { outline: 2px solid #fff; outline-offset: 4px; border-radius: 10px }`. */
private fun Modifier.focusOutline(visible: Boolean): Modifier = drawWithCache {
    val width = 1.dp.toPx()
    val inset = 2.dp.toPx() + width / 2f
    val stroke = Stroke(width)
    val radius = CornerRadius(5.dp.toPx() + inset)
    onDrawWithContent {
        drawContent()
        if (visible) {
            drawRoundRect(
                GothamPalette.FocusOutline,
                topLeft = Offset(-inset, -inset),
                size = Size(size.width + 2 * inset, size.height + 2 * inset),
                cornerRadius = radius,
                style = stroke,
            )
        }
    }
}
