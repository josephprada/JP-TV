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
import co.jp.tv.homeoverride.domain.DetectionMethod
import co.jp.tv.homeoverride.domain.HomeOverrideState
import co.jp.tv.homeoverride.domain.HomeOverrideStatus
import co.jp.tv.homeoverride.domain.MissingPermission

/** What the settings row shows and what its buttons do. */
data class HomeOverrideUi(
    val state: HomeOverrideState,
    val onToggle: () -> Unit,
    val onGrantUsageAccess: () -> Unit,
    val onGrantOverlay: () -> Unit,
    val onOpenStockLauncher: () -> Unit,
)

private val Amber = Color(0xFFFF8A1F)

/** "Usar JP-TV como inicio": status, the active method, what is missing, and the buttons to opt in or to reach the stock launcher. */
@Composable
fun HomeOverrideRow(ui: HomeOverrideUi, accent: Color, modifier: Modifier = Modifier) {
    val state = ui.state
    val (statusText, statusColor) = when (state.status) {
        HomeOverrideStatus.ACTIVE -> R.string.home_override_status_active to accent
        HomeOverrideStatus.DISABLED -> R.string.home_override_status_off to JpColors.Muted
        HomeOverrideStatus.NEEDS_PERMISSION -> R.string.home_override_status_needs_permission to Amber
    }
    val hint = when (state.status) {
        HomeOverrideStatus.ACTIVE -> stringResource(R.string.home_override_hint_active)
        HomeOverrideStatus.DISABLED -> stringResource(R.string.home_override_hint_off)
        HomeOverrideStatus.NEEDS_PERMISSION -> {
            val names = state.missing.map {
                stringResource(if (it == MissingPermission.USAGE_ACCESS) R.string.home_override_permission_usage else R.string.home_override_permission_overlay)
            }
            stringResource(R.string.home_override_hint_needs_permission, names.joinToString(stringResource(R.string.home_override_permission_separator)))
        }
    }
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier = modifier.fillMaxWidth().glass(shape).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    stringResource(R.string.home_override_title),
                    style = TextStyle(JpColors.Paper, 11.5.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
                )
                BasicText(
                    stringResource(statusText),
                    style = TextStyle(statusColor, 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
                state.method?.let { method ->
                    BasicText(
                        stringResource(if (method == DetectionMethod.ACCESSIBILITY) R.string.home_override_method_accessibility else R.string.home_override_method_usage),
                        style = TextStyle(JpColors.Muted, 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                    )
                }
            }
            BasicText(hint, style = TextStyle(JpColors.Muted, 8.sp, fontFamily = JpFonts.Body))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (state.status == HomeOverrideStatus.NEEDS_PERMISSION) {
                if (MissingPermission.USAGE_ACCESS in state.missing) {
                    GlassButton(stringResource(R.string.home_override_grant_usage), accent, ui.onGrantUsageAccess, primary = true)
                }
                if (MissingPermission.OVERLAY in state.missing) {
                    GlassButton(stringResource(R.string.home_override_grant_overlay), accent, ui.onGrantOverlay, primary = true)
                }
            }
            GlassButton(
                stringResource(if (state.status == HomeOverrideStatus.DISABLED) R.string.home_override_turn_on else R.string.home_override_turn_off),
                accent,
                ui.onToggle,
                primary = state.status == HomeOverrideStatus.DISABLED,
            )
            GlassButton(stringResource(R.string.home_override_open_stock), accent, ui.onOpenStockLauncher)
        }
    }
}
