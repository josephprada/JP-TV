package co.jp.tv.screensaver.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.rememberTitleFont
import co.jp.tv.core.util.ClockFormat
import co.jp.tv.screensaver.domain.ScreensaverState

private val Soft = Shadow(Color.Black.copy(alpha = 0.7f), Offset(0f, 2f), 10f)

/** The small clock and date every mode except the big clock keeps in the bottom corner, readable over any picture. */
@Composable
internal fun BoxScope.CornerClock(state: ScreensaverState, modifier: Modifier = Modifier) {
    val accent = Color(state.config.accent)
    val titleFont = rememberTitleFont(state.config.titleFontId)
    Column(
        modifier.align(Alignment.BottomEnd).padding(horizontal = 28.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.End,
    ) {
        BasicText(
            ClockFormat.time(state.now, state.config.hourFormat),
            style = TextStyle(color = JpColors.Paper, fontSize = 30.sp, fontFamily = titleFont, shadow = Soft),
        )
        BasicText(
            ClockFormat.date(state.now),
            style = TextStyle(color = accent, fontSize = 7.5.sp, fontFamily = JpFonts.Mono, letterSpacing = 0.2.em, shadow = Soft),
        )
    }
}
