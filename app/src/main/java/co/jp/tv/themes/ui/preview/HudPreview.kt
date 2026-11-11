package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.drawEllipseGradient
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.rememberLoop
import co.jp.tv.themes.ui.unit

private val CoreInner = Color(0xFF0A1824)
private val CoreOuter = Color(0xFF03070C)
private val Cyan = Color(0xFF4DE8FF)
private val ClockText = Color(0xFFE9FDFF)

/** Hexagonal tag: polygon(12% 0, 88% 0, 100% 50%, 88% 100%, 12% 100%, 0 50%). */
private val TagShape = GenericShape { size, _ ->
    moveTo(size.width * 0.12f, 0f)
    lineTo(size.width * 0.88f, 0f)
    lineTo(size.width, size.height * 0.5f)
    lineTo(size.width * 0.88f, size.height)
    lineTo(size.width * 0.12f, size.height)
    lineTo(0f, size.height * 0.5f)
    close()
}

/** Thumbnail of the HUD home: sci-fi ring around the clock with the focused app tag above it. */
@Composable
internal fun HudPreview(modifier: Modifier = Modifier) {
    val spin by rememberLoop(20_000, rest = 0f)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            val center = Offset(size.width / 2f, size.height / 2f)
            drawEllipseGradient(center, size.width * 0.8f, size.height * 0.7f, listOf(0f to CoreInner, 0.75f to CoreOuter))
            rotate(spin * 360f, pivot = center) {
                drawCircle(
                    Cyan.copy(alpha = 0.5f),
                    radius = 84f * u,
                    center = center,
                    style = Stroke(2f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f * u, 6f * u))),
                )
            }
        }
        // Static sample time, as in the artboard (the Themes screen has no clock or hour-format input).
        BasicText(
            text = "21:40",
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                color = ClockText,
                fontSize = 17.sp,
                fontFamily = JpFonts.Orbitron,
                textAlign = TextAlign.Center,
                shadow = Shadow(Cyan.copy(alpha = 0.7f), blurRadius = 14f),
            ),
        )
        BasicText(
            text = "YOUTUBE",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.px)
                .background(Cyan, TagShape)
                .padding(horizontal = 14.px, vertical = 6.px),
            style = TextStyle(color = CoreOuter, fontSize = 5.5.sp, fontFamily = JpFonts.Orbitron),
        )
    }
}
