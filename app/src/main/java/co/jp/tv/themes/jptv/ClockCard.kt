package co.jp.tv.themes.jptv

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpDimens
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.glass
import co.jp.tv.core.design.riseIn
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.core.util.LocalPaused
import java.time.LocalDateTime

@Composable
internal fun ClockCard(now: LocalDateTime, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(end = 20.dp, bottom = 20.dp)
            .riseIn(delayMs = 350, distanceDp = 12f)
            .width(IntrinsicSize.Max)
            .glass(JpDimens.RailShape)
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        BasicText(
            text = timeText(now),
            style = TextStyle(color = JpColors.Paper, fontSize = 74.sp, fontFamily = JpFonts.Display, letterSpacing = (-0.01).em),
        )
        BasicText(
            text = ClockFormat.date(now),
            style = TextStyle(color = accent, fontSize = 8.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.2.em),
        )
        MinuteLine(accent)
    }
}

/** Colon dims on odd seconds; driven by the per-second tick, so it stops when the activity stops. */
private fun timeText(now: LocalDateTime): AnnotatedString {
    val (hh, mm) = ClockFormat.time(now).split(":")
    val colon = if (ClockFormat.colonVisible(now)) 1f else 0.25f
    return buildAnnotatedString {
        append(hh)
        withStyle(SpanStyle(color = JpColors.Paper.copy(alpha = colon))) { append(":") }
        append(mm)
    }
}

/** Thin accent line that fills over each wall-clock minute. */
@Composable
private fun MinuteLine(accent: Color) {
    val paused = LocalPaused.current
    val fill = remember { Animatable(0f) }
    LaunchedEffect(paused) {
        if (paused) return@LaunchedEffect
        val now = LocalDateTime.now()
        val elapsed = (now.second * 1000L + now.nano / 1_000_000L)
        fill.snapTo(elapsed / 60_000f)
        fill.animateTo(1f, tween((60_000L - elapsed).toInt(), easing = LinearEasing))
        while (true) {
            fill.snapTo(0f)
            fill.animateTo(1f, tween(60_000, easing = LinearEasing))
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.5.dp)
            .drawBehind {
                val r = CornerRadius(size.height / 2f)
                drawRoundRect(JpColors.Line, cornerRadius = r)
                drawRoundRect(accent, size = Size(size.width * fill.value, size.height), cornerRadius = r)
            },
    )
}
