package co.jp.tv.themes.lofi

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.core.design.JpMotion
import kotlinx.coroutines.delay

private const val ENTER_MS = 600
private const val FOCUS_MARK = "✎"
private const val IDLE_MARK = "·"

/**
 * One handwritten app line: 64 px tall, Caveat 44 px. Focused: "✎", bold ink on a translucent
 * orange highlight. [enterDelayMs] plays lo-in (fade + slide from 30 px right); null shows it at once.
 */
@Composable
internal fun LofiNotebookRow(app: TvApp, focused: Boolean, enterDelayMs: Int?) {
    val enter = remember { Animatable(if (enterDelayMs == null) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (enterDelayMs != null) {
            delay(enterDelayMs.toLong())
            enter.animateTo(1f, tween(ENTER_MS, easing = JpMotion.Entrance))
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(32.dp)
            .graphicsLayer {
                alpha = enter.value
                translationX = (1f - enter.value) * 15.dp.toPx()
            }
            .background(if (focused) LofiPalette.Highlight else Color.Transparent, RoundedCornerShape(5.dp))
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(
            "${if (focused) FOCUS_MARK else IDLE_MARK} ${app.label}",
            style = TextStyle(
                color = if (focused) LofiPalette.Ink else LofiPalette.InkSoft,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontFamily = JpFonts.Caveat,
                fontWeight = if (focused) FontWeight.Bold else FontWeight.Medium,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
