package co.jp.tv.screensaver.ui

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
import co.jp.tv.screensaver.domain.ScreensaverSettings

/** "Protector de pantalla" row of the settings screen: what is chosen now and the way into its own screen. */
@Composable
fun ScreensaverRow(settings: ScreensaverSettings, accent: Color, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(13.dp)
    val idle = if (settings.idleMinutes == 0) stringResource(R.string.screensaver_idle_never) else stringResource(R.string.screensaver_idle_minutes, settings.idleMinutes)
    Row(
        modifier = modifier.fillMaxWidth().glass(shape).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    stringResource(R.string.screensaver_row_title),
                    style = TextStyle(JpColors.Paper, 11.5.sp, FontWeight.Medium, fontFamily = JpFonts.Body),
                )
                BasicText(
                    stringResource(R.string.screensaver_row_status, stringResource(settings.mode.labelRes()), idle).uppercase(),
                    style = TextStyle(accent, 7.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.14.em),
                )
            }
            BasicText(stringResource(R.string.screensaver_row_hint), style = TextStyle(JpColors.Muted, 8.sp, fontFamily = JpFonts.Body))
        }
        GlassButton(stringResource(R.string.screensaver_row_open), accent, onOpen)
    }
}
