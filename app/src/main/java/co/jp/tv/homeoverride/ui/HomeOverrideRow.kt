package co.jp.tv.homeoverride.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.GlassButton
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.glass
import co.jp.tv.homeoverride.domain.HomeOverrideStatus

/** What the settings row shows and what its buttons do. */
data class HomeOverrideUi(
    val status: HomeOverrideStatus,
    val onToggle: () -> Unit,
    val onGrantPermission: () -> Unit,
    val onOpenStockLauncher: () -> Unit,
)

private val Amber = Color(0xFFFF8A1F)

/** "Usar JP-TV como inicio": state, a one-line explanation and the buttons to opt in or to reach the stock launcher. */
@Composable
fun HomeOverrideRow(ui: HomeOverrideUi, accent: Color, modifier: Modifier = Modifier) {
    val (statusText, statusColor, hint) = when (ui.status) {
        HomeOverrideStatus.ACTIVE -> Triple(R.string.home_override_status_active, accent, R.string.home_override_hint_active)
        HomeOverrideStatus.DISABLED -> Triple(R.string.home_override_status_off, JpColors.Muted, R.string.home_override_hint_off)
        HomeOverrideStatus.NEEDS_PERMISSION -> Triple(R.string.home_override_status_needs_permission, Amber, R.string.home_override_hint_needs_permission)
    }
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = modifier.fillMaxWidth().glass(shape).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    stringResource(R.string.home_override_title),
                    style = TextStyle(JpColors.Paper, 11.5.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
                )
                BasicText(
                    stringResource(statusText),
                    style = TextStyle(statusColor, 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
            }
            BasicText(stringResource(hint), style = TextStyle(JpColors.Muted, 8.sp, fontFamily = JpFonts.Body))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (ui.status == HomeOverrideStatus.NEEDS_PERMISSION) {
                GlassButton(stringResource(R.string.home_override_grant), accent, ui.onGrantPermission, primary = true)
            }
            GlassButton(
                stringResource(if (ui.status == HomeOverrideStatus.DISABLED) R.string.home_override_turn_on else R.string.home_override_turn_off),
                accent,
                ui.onToggle,
                primary = ui.status == HomeOverrideStatus.DISABLED,
            )
            GlassButton(stringResource(R.string.home_override_open_stock), accent, ui.onOpenStockLauncher)
        }
    }
}
