package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.jp.tv.R
import co.jp.tv.core.design.JpFonts
import co.jp.tv.themes.ui.px
import co.jp.tv.themes.ui.unit

private val Wall = Color(0xFF1C0A0C)
private val Gold = Color(0xFFF6C453)
private val Poster = Color(0xFF3A1418)

/** Thumbnail of the Cartelera home: a cinema marquee over three posters. */
@Composable
internal fun CarteleraPreview(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(Wall).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.px, Alignment.CenterVertically),
    ) {
        BasicText(
            text = stringResource(R.string.theme_preview_cartelera_marquee),
            modifier = Modifier.dottedEdges(Gold).padding(horizontal = 16.px, vertical = 6.px),
            style = TextStyle(color = Gold, fontSize = 16.sp, fontFamily = JpFonts.Limelight),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.px)) {
            repeat(3) {
                val shape = RoundedCornerShape(4.dp)
                Box(Modifier.size(width = 70.px, height = 96.px).background(Poster, shape).border(2.px, Gold, shape))
            }
        }
    }
}

/** 3 px dotted lines above and below the marquee text. */
private fun Modifier.dottedEdges(color: Color): Modifier = drawBehind {
    val width = 3f * unit()
    val dots = PathEffect.dashPathEffect(floatArrayOf(0f, width * 2f))
    val inset = width / 2f
    drawLine(color, Offset(0f, inset), Offset(size.width, inset), strokeWidth = width, cap = StrokeCap.Round, pathEffect = dots)
    drawLine(color, Offset(0f, size.height - inset), Offset(size.width, size.height - inset), strokeWidth = width, cap = StrokeCap.Round, pathEffect = dots)
}
