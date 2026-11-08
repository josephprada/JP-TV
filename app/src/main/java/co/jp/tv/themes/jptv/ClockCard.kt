package co.jp.tv.themes.jptv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.themes.shared.HomeState

private val CardShape = RoundedCornerShape(18.dp)

@Composable
internal fun ClockCard(state: HomeState, modifier: Modifier = Modifier) {
    val now = state.now
    Column(
        modifier = modifier
            .padding(end = 20.dp, bottom = 20.dp)
            .background(JpTvPalette.Glass, CardShape)
            .border(0.5.dp, JpTvPalette.Line, CardShape)
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        BasicText(
            text = timeText(now, JpTvPalette.Paper),
            style = TextStyle(color = JpTvPalette.Paper, fontSize = 74.sp, letterSpacing = (-0.01).em),
        )
        BasicText(
            text = ClockFormat.date(now),
            style = TextStyle(
                color = JpTvPalette.Accent,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.2.em,
            ),
        )
    }
}

private fun timeText(now: java.time.LocalDateTime, paper: androidx.compose.ui.graphics.Color): AnnotatedString {
    val (hh, mm) = ClockFormat.time(now).split(":")
    return buildAnnotatedString {
        append(hh)
        withStyle(SpanStyle(color = paper.copy(alpha = if (ClockFormat.colonVisible(now)) 1f else 0.25f))) { append(":") }
        append(mm)
    }
}
