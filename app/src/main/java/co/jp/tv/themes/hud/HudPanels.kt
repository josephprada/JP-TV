package co.jp.tv.themes.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.network.domain.NetworkStatus
import co.jp.tv.themes.shared.HomeActions
import co.jp.tv.themes.shared.ThemeControlFocus
import co.jp.tv.themes.shared.themeControlTarget

/** polygon(0 0, 92% 0, 100% 14%, 100% 100%, 8% 100%, 0 86%) */
private val TelemetryShape: Shape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width * 0.92f, 0f)
    lineTo(size.width, size.height * 0.14f)
    lineTo(size.width, size.height)
    lineTo(size.width * 0.08f, size.height)
    lineTo(0f, size.height * 0.86f)
    close()
}

/** polygon(8% 0, 100% 0, 100% 86%, 92% 100%, 0 100%, 0 14%) */
private val UnitShape: Shape = GenericShape { size, _ ->
    moveTo(size.width * 0.08f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.86f)
    lineTo(size.width * 0.92f, size.height)
    lineTo(0f, size.height)
    lineTo(0f, size.height * 0.14f)
    close()
}

private val Title = TextStyle(fontSize = 7.5.sp, fontFamily = JpFonts.Orbitron, letterSpacing = 0.3.em)
private val Line = TextStyle(fontSize = 10.sp, fontFamily = JpFonts.Mono)

@Composable
private fun HudPanelFrame(
    shape: Shape,
    borderColor: Color,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Clip first: the cut corners also remove the border, as the design's clip-path does.
    Column(
        modifier = modifier
            .width(195.dp)
            .clip(shape)
            .background(HudPalette.PanelBg)
            .border(0.5.dp, borderColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = content,
    )
}

@Composable
private fun HudRow(label: String, value: String, color: Color = HudPalette.Text) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        BasicText(label, style = Line.copy(color = color))
        BasicText(value, style = Line.copy(color = color))
    }
}

/** Top-left panel: signal, ping and IP. Tapping it opens the network details. */
@Composable
internal fun HudTelemetryPanel(network: NetworkStatus, actions: HomeActions, modifier: Modifier = Modifier) {
    val signal = network.signalLevel.coerceIn(0, 4) * 25
    val ms = network.routerLatencyMs ?: network.internetLatencyMs
    HudPanelFrame(
        shape = TelemetryShape,
        borderColor = HudPalette.PanelBorder,
        modifier = modifier
            .focusProperties { canFocus = false }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = actions::openNetwork,
            ),
    ) {
        BasicText(stringResource(R.string.theme_hud_telemetry), style = Title.copy(color = HudPalette.Cyan))
        HudRow(stringResource(R.string.theme_hud_signal), "$signal%")
        Box(Modifier.fillMaxWidth().height(3.dp).background(HudPalette.BarTrack)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(signal / 100f)
                    .drawBehind {
                        val grow = 2.dp.toPx()
                        drawRect(
                            HudPalette.BarGlow,
                            Offset(-grow, -grow),
                            Size(size.width + 2 * grow, size.height + 2 * grow),
                        )
                    }
                    .background(HudPalette.Cyan),
            )
        }
        HudRow(stringResource(R.string.theme_hud_ping), ms?.let { "$it MS" } ?: "—")
        HudRow(stringResource(R.string.theme_hud_ip), network.ip ?: "—")
    }
}

/** Top-right panel and the theme's one D-pad control: it opens Settings. */
@Composable
internal fun HudUnitPanel(
    brand: String,
    appCount: Int,
    network: NetworkStatus,
    actions: HomeActions,
    focus: ThemeControlFocus,
    modifier: Modifier = Modifier,
) {
    val focused = focus.onControl
    HudPanelFrame(
        shape = UnitShape,
        borderColor = if (focused) HudPalette.Cyan else HudPalette.PanelBorder,
        modifier = modifier
            .themeControlTarget(focus)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = actions::openSettings,
            ),
    ) {
        BasicText(stringResource(R.string.theme_hud_unit, brand), style = Title.copy(color = HudPalette.Cyan))
        HudRow(stringResource(R.string.theme_hud_apps), appCount.toString())
        HudRow(stringResource(R.string.theme_hud_dns), network.dns.firstOrNull() ?: "—")
        HudRow(
            stringResource(R.string.theme_hud_settings),
            "▶",
            color = if (focused) HudPalette.White else HudPalette.Amber,
        )
    }
}
