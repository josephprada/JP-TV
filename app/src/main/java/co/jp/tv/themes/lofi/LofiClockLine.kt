package co.jp.tv.themes.lofi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LongState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.ui.WeatherBadge

/** VHS stamp at (120, 48) px: blinking "▶ PLAY", then "[clock] · [date]" and the weather; 22 px mono, 0.14 em. */
@Composable
internal fun LofiClockLine(clockLine: String, millis: LongState, weather: Weather? = null) {
    val style = TextStyle(
        color = LofiPalette.Paper,
        fontSize = 11.sp,
        fontFamily = JpFonts.DmMono,
        letterSpacing = 0.14.em,
    )
    Row(
        Modifier.offset(x = 60.dp, y = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row {
            BasicText(
                "▶",
                // lo-blink, 1 s steps(1): visible for the first half of every second.
                modifier = Modifier.graphicsLayer { alpha = if (millis.longValue % 1000L < 500L) 1f else 0f },
                style = style.copy(color = LofiPalette.PlayMark),
            )
            BasicText(" " + stringResource(R.string.theme_lofi_play), style = style)
        }
        BasicText(clockLine, style = style)
        WeatherBadge(weather, color = LofiPalette.Paper, textStyle = style, iconSize = 13.dp, gap = 4.dp)
    }
}
