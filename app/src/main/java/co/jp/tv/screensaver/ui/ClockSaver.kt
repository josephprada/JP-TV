package co.jp.tv.screensaver.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.screensaver.domain.BurnInDrift
import co.jp.tv.screensaver.domain.ScreensaverState
import co.jp.tv.weather.ui.WeatherBadge
import java.time.ZoneOffset

/** How far the clock may wander from the centre, as a share of the screen. */
private const val DRIFT_X = 0.07f
private const val DRIFT_Y = 0.09f
private const val DRIFT_MS = 6_000

/**
 * "Reloj y mensaje": a big clock in the user's title font and accent, the date, the weather and a short
 * message on black with a faint accent glow. Once a minute the whole group glides to another of nine places
 * ([BurnInDrift]) so no pixel stays lit; the glow is a single static gradient, nothing here loops.
 */
@Composable
internal fun ClockSaver(state: ScreensaverState) {
    val accent = Color(state.config.accent)
    val titleFont = rememberTitleFont(state.config.titleFontId)
    val drift = BurnInDrift.offset(BurnInDrift.stepOf(state.now.toEpochSecond(ZoneOffset.UTC)))
    val x by animateFloatAsState(drift.x, tween(DRIFT_MS, easing = LinearEasing), label = "driftX")
    val y by animateFloatAsState(drift.y, tween(DRIFT_MS, easing = LinearEasing), label = "driftY")
    val parts = ClockFormat.parts(state.now, state.config.hourFormat)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .drawWithCache {
                val glow = Brush.radialGradient(
                    0f to accent.copy(alpha = 0.2f),
                    1f to Color.Transparent,
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.minDimension * 0.95f,
                )
                onDrawBehind { drawRect(glow) }
            }
            .graphicsLayer {
                translationX = x * size.width * DRIFT_X
                translationY = y * size.height * DRIFT_Y
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicText(
                    "${parts.hours}:${parts.minutes}",
                    style = TextStyle(color = JpColors.Paper, fontSize = 120.sp, fontFamily = titleFont, letterSpacing = (-0.02).em),
                    maxLines = 1,
                )
                parts.meridiem?.let {
                    BasicText(
                        it,
                        style = TextStyle(color = accent, fontSize = 26.sp, fontFamily = titleFont),
                        modifier = Modifier.padding(bottom = 22.dp),
                    )
                }
            }
            BasicText(
                ClockFormat.date(state.now),
                style = TextStyle(color = accent, fontSize = 12.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.26.em),
            )
            WeatherBadge(
                weather = state.weather,
                color = JpColors.Muted,
                textStyle = TextStyle(fontSize = 20.sp, fontFamily = JpFonts.Body),
                iconSize = 26.dp,
                gap = 8.dp,
                modifier = Modifier.padding(top = 12.dp),
            )
            BasicText(
                state.config.screensaver.message,
                style = TextStyle(color = JpColors.Paper.copy(alpha = 0.85f), fontSize = 24.sp, fontFamily = titleFont),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
