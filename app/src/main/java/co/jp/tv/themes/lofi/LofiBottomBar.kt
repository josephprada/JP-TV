package co.jp.tv.themes.lofi

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.themeControlTarget

/** Bottom bar at 120 px sides, 56 px bottom: status line and the "ajustes" pill; 20 px mono, 0.12 em. */
@Composable
internal fun LofiBottomBar(
    network: NetworkStatus,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val ms = NetworkRules.formatLatency(network.routerLatencyMs ?: network.internetLatencyMs)
    val net = (network.ssid ?: stringResource(R.string.network_wifi)).uppercase()
    val style = TextStyle(
        color = LofiPalette.Paper,
        fontSize = 10.sp,
        fontFamily = JpFonts.Mono,
        letterSpacing = 0.12.em,
    )
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 60.dp, bottom = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(stringResource(R.string.theme_lofi_status_line, net, ms), style = style)
        val shape = RoundedCornerShape(13.dp)
        BasicText(
            stringResource(R.string.cd_settings).lowercase(),
            modifier = Modifier
                .themeControlTarget(focus)
                .clip(shape)
                .background(if (focus.onControl) LofiPalette.PillFocused else LofiPalette.PillIdle)
                .border(1.5.dp, if (focus.onControl) LofiPalette.Accent else Color.Transparent, shape)
                .clickable(onClick = actions::openSettings)
                .padding(horizontal = 13.dp, vertical = 7.dp),
            style = style,
        )
    }
}
