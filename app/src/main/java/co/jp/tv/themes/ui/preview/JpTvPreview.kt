package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.rememberLoop
import co.jp.tv.themes.ui.twinkleAlpha
import co.jp.tv.themes.ui.unit

private val SkyTop = Color(0xFF0A0A0C)
private val SkyMid = Color(0xFF110D10)
private val SkyBottom = Color(0xFF140A0B)
private val RailFill = Color(0xFF121214).copy(alpha = 0.5f)

/** Thumbnail of the default JP-TV home: galaxy backdrop, glass rail with a focused row, twinkling stars. */
@Composable
internal fun JpTvPreview(modifier: Modifier = Modifier) {
    // One 30 s loop drives the three stars: 2 s, 3 s and 2.5 s cycles fit 15, 10 and 12 times.
    val loop by rememberLoop(30_000)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            drawRect(cssLinear(155f, listOf(0f to SkyTop, 0.45f to SkyMid, 1f to SkyBottom)))
            val glowCenter = Offset(size.width * 0.30f + 110f * u, size.height * 0.10f + 110f * u)
            drawCircle(
                Brush.radialGradient(
                    0f to JpColors.Accent.copy(alpha = 0.35f),
                    0.65f to JpColors.Accent.copy(alpha = 0f),
                    center = glowCenter,
                    radius = 155.6f * u,
                ),
                radius = 110f * u,
                center = glowCenter,
            )
            drawCircle(
                Color.White.copy(alpha = twinkleAlpha(loop * 15f, 0f)),
                radius = 1.5f * u,
                center = Offset(size.width * 0.60f + 1.5f * u, size.height * 0.20f + 1.5f * u),
            )
            drawCircle(
                Color.White.copy(alpha = twinkleAlpha(loop * 10f, 1f / 3f)),
                radius = 1f * u,
                center = Offset(size.width * 0.80f + 1f * u, size.height * 0.50f + 1f * u),
            )
            drawCircle(
                JpColors.AccentLift.copy(alpha = twinkleAlpha(loop * 12f, 0.2f)),
                radius = 1.5f * u,
                center = Offset(size.width * 0.70f + 1.5f * u, size.height * 0.75f + 1.5f * u),
            )
        }
        val railShape = RoundedCornerShape(8.dp)
        val rowShape = RoundedCornerShape(4.dp)
        Column(
            modifier = Modifier
                .offset(x = 10.px)
                .padding(top = 10.px, bottom = 10.px)
                .fillMaxWidth(0.38f)
                .fillMaxHeight()
                .background(RailFill, railShape)
                .border(1.px, JpColors.Line, railShape)
                .padding(12.px),
            verticalArrangement = Arrangement.spacedBy(6.px),
        ) {
            BasicText(
                text = "JP-TV",
                style = TextStyle(color = JpColors.Accent, fontSize = 11.sp, fontFamily = JpFonts.Display),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.px)
                    .background(JpColors.FocusFill.copy(alpha = 0.7f), rowShape)
                    .border(1.px, JpColors.AccentLift.copy(alpha = 0.7f), rowShape),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(20.px)
                    .background(JpColors.Paper.copy(alpha = 0.06f), rowShape),
            )
        }
    }
}
