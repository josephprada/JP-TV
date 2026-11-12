package co.jp.tv.themes.pixel

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
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.networkName
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.themeControlTarget
import java.time.LocalDateTime

/** Tap target that the D-pad skips: the theme's list and its one control own D-pad focus. */
@Composable
internal fun Modifier.pixelTap(onClick: () -> Unit): Modifier = this
    .focusProperties { canFocus = false }
    .clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    )

/** Network pill, settings, themes and the clock, top right. Settings is the D-pad control. */
@Composable
internal fun PixelTopBar(
    network: NetworkStatus,
    now: LocalDateTime,
    hourFormat: ClockFormat.HourFormat,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val clockShadow = rememberPixelShadow(PixelPalette.ShadowClock, 2f)
    Row(
        modifier.pixelEntrance(100),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkPill(network, actions)
        PixelRoundButton(
            PixelIconKind.Gear,
            Modifier
                .themeControlTarget(focus)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = actions::openSettings,
                ),
            ringed = focus.onControl,
        )
        PixelRoundButton(PixelIconKind.Palette, Modifier.pixelTap(actions::openThemes), ringed = false)
        BasicText(
            ClockFormat.time(now, hourFormat),
            modifier = Modifier.padding(start = 6.dp),
            style = TextStyle(
                color = PixelPalette.White,
                fontSize = 22.sp,
                fontFamily = JpFonts.Fredoka,
                shadow = clockShadow,
            ),
        )
    }
}

@Composable
private fun NetworkPill(network: NetworkStatus, actions: HomeActions) {
    val name = networkName(network).uppercase()
    val ms = network.routerLatencyMs ?: network.internetLatencyMs
    val text = if (ms != null) "$name · $ms MS" else name
    Row(
        Modifier
            .height(32.dp)
            .background(PixelPalette.Chip, CircleShape)
            .border(1.dp, PixelPalette.ChipBorder, CircleShape)
            .pixelTap(actions::openNetwork)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PixelIcon(PixelIconKind.Wifi, PixelPalette.Green, 2f, 13.dp)
        BasicText(
            text,
            style = TextStyle(color = PixelPalette.White, fontSize = 10.sp, fontFamily = JpFonts.Fredoka),
        )
    }
}

@Composable
private fun PixelRoundButton(kind: PixelIconKind, modifier: Modifier, ringed: Boolean) {
    Box(
        modifier
            .size(32.dp)
            .drawWithCache {
                // The design's focus outline: 3px gold, 4px away from the button.
                val ring = Stroke(width = 1.5.dp.toPx())
                val radius = this.size.minDimension / 2f + 2.dp.toPx() + 0.75.dp.toPx()
                onDrawBehind {
                    if (ringed) drawCircle(PixelPalette.Gold, radius = radius, style = ring)
                }
            }
            .background(PixelPalette.Chip, CircleShape)
            .border(1.dp, PixelPalette.ChipBorder, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        PixelIcon(kind, PixelPalette.White, 1.9f, 14.dp)
    }
}
