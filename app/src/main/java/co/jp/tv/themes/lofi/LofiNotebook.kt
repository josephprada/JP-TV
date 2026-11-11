package co.jp.tv.themes.lofi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.apps.domain.TvApp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.shared.RailWindow
import kotlinx.coroutines.delay

/** Rows that fit under the heading: (700 - 56 - 56 - 18) px / 64 px. */
private const val VISIBLE_ROWS = 8
private const val ENTER_FIRST_MS = 100
private const val ENTER_STAGGER_MS = 70

/**
 * Ruled notebook at right 120 px, top 110 px, 640x700 px, tilted 1.5 deg: pink margin, Caveat
 * heading "[brand] — esta noche / hoy" and the app list, scrolled with [RailWindow].
 */
@Composable
internal fun LofiNotebook(
    brand: String,
    night: Boolean,
    apps: List<TvApp>,
    focusedIndex: Int,
    modifier: Modifier = Modifier,
) {
    // Rows slide in only on the first pass; rows revealed by scrolling appear at once.
    var firstPass by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1500)
        firstPass = false
    }
    var previousTop by remember { mutableIntStateOf(0) }
    val count = apps.size
    val top = RailWindow.top(previousTop, focusedIndex, count, VISIBLE_ROWS)
    SideEffect { previousTop = top }

    val whenLabel = stringResource(if (night) R.string.theme_lofi_tonight else R.string.theme_lofi_today)
    Box(
        modifier
            .padding(top = 55.dp, end = 60.dp)
            .size(width = 320.dp, height = 350.dp)
            .graphicsLayer { rotationZ = 1.5f }
            // box-shadow: 0 30px 70px rgba(0,0,0,0.5).
            .lofiDropShadow(LofiPalette.Shadow, cornerRadius = 9.dp, offsetY = 15.dp, blur = 35.dp)
            .drawWithCache {
                // repeating-linear-gradient: paper 0-62 px, line at 63 px, paper at 64 px.
                val period = 32.dp.toPx()
                val lines = Brush.verticalGradient(
                    0f to LofiPalette.Paper,
                    (31f / 32f) to LofiPalette.Paper,
                    (31.5f / 32f) to LofiPalette.NotebookLine,
                    1f to LofiPalette.Paper,
                    startY = 0f,
                    endY = period,
                    tileMode = TileMode.Repeated,
                )
                val radius = CornerRadius(9.dp.toPx())
                val marginX = 32.dp.toPx()
                val marginWidth = 1.dp.toPx()
                onDrawBehind {
                    drawRoundRect(lines, cornerRadius = radius)
                    drawRect(
                        LofiPalette.NotebookMargin,
                        topLeft = Offset(marginX, 0f),
                        size = Size(marginWidth, size.height),
                    )
                }
            }
            .padding(start = 46.dp, top = 28.dp, end = 28.dp),
    ) {
        Column {
            BasicText(
                stringResource(R.string.theme_lofi_title, brand, whenLabel),
                style = TextStyle(
                    color = LofiPalette.NotebookTitle,
                    fontSize = 28.sp,
                    lineHeight = 28.sp,
                    fontFamily = JpFonts.Caveat,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(Modifier.padding(top = 9.dp)) {
                for (index in top until minOf(count, top + VISIBLE_ROWS)) {
                    val app = apps[index]
                    key(app.packageName) {
                        val delayMs = if (firstPass) ENTER_FIRST_MS + (index - top) * ENTER_STAGGER_MS else null
                        LofiNotebookRow(app, focused = index == focusedIndex, enterDelayMs = delayMs)
                    }
                }
            }
        }
    }
}
