package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import co.jp.tv.core.design.JpColors
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.cssLinear
import co.jp.tv.themes.ui.drawGlowRect
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.rememberLoop
import co.jp.tv.themes.ui.twinkleAlpha
import co.jp.tv.themes.ui.unit

private val SkyTop = Color(0xFF0A0A0C)
private val SkyMid = Color(0xFF110D10)
private val SkyBottom = Color(0xFF140A0B)
private val Label = Color(0xFFF5F5F7)
private val YouTube = Color(0xFFFF1F3D)
private val Netflix = Color(0xFFD81F26)
private val Spotify = Color(0xFF1ED760)

/** Thumbnail of the Zen home: one large focused app with the rest collapsed above and below. */
@Composable
internal fun ZenPreview(modifier: Modifier = Modifier) {
    // One 6 s loop drives both stars: 2 s and 3 s cycles fit 3 and 2 times.
    val loop by rememberLoop(6_000)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val u = unit()
            drawRect(cssLinear(155f, listOf(0f to SkyTop, 0.45f to SkyMid, 1f to SkyBottom)))
            val center = Offset(size.width * 0.40f + 110f * u, -size.height * 0.20f + 110f * u)
            drawCircle(
                Brush.radialGradient(
                    0f to JpColors.Accent.copy(alpha = 0.30f),
                    0.65f to JpColors.Accent.copy(alpha = 0f),
                    center = center,
                    radius = 155.6f * u,
                ),
                radius = 110f * u,
                center = center,
            )
            drawCircle(
                Color.White.copy(alpha = twinkleAlpha(loop * 3f, 0f)),
                radius = 1.5f * u,
                center = Offset(size.width * 0.70f + 1.5f * u, size.height * 0.18f + 1.5f * u),
            )
            drawCircle(
                Color.White.copy(alpha = twinkleAlpha(loop * 2f, 1f / 3f)),
                radius = 1f * u,
                center = Offset(size.width * 0.88f + 1f * u, size.height * 0.60f + 1f * u),
            )
        }
        Column(
            modifier = Modifier.padding(start = 22.px, top = 36.px),
            verticalArrangement = Arrangement.spacedBy(10.px),
        ) {
            SmallItem("YouTube", YouTube)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.px)) {
                Box(
                    Modifier
                        .size(60.px)
                        .drawBehind {
                            val u = unit()
                            drawGlowRect(Offset(0f, 10f * u), Size(60f * u, 60f * u), 13.8f * u, 26f * u, Netflix.copy(alpha = 0.45f))
                        }
                        .background(Netflix, RoundedCornerShape(23)),
                )
                BasicText(
                    "Netflix",
                    style = TextStyle(
                        color = Label,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = JpFonts.Body,
                        letterSpacing = (-0.03).em,
                    ),
                )
            }
            SmallItem("Spotify", Spotify)
        }
    }
}

@Composable
private fun SmallItem(name: String, color: Color) {
    Row(
        modifier = Modifier.alpha(0.5f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.px),
    ) {
        Box(Modifier.size(20.px).background(color, RoundedCornerShape(23)))
        BasicText(name, style = TextStyle(color = Label, fontSize = 7.5.sp, fontWeight = FontWeight.Medium, fontFamily = JpFonts.Body))
    }
}
