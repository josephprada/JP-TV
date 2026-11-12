package co.jp.tv.weather.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.jp.tv.weather.domain.WeatherCondition

/**
 * One-colour line icon for a [WeatherCondition]. Static (nothing animates, so nothing to pause) and
 * allocation-free per draw: the geometry is parsed once in [WeatherGlyphs] and only transformed here.
 */
@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    isDay: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val parts = WeatherGlyphs.parts(condition, isDay)
    // The stroke is in each part's own scaled space; dividing by the part scale keeps it 2/24 of the icon.
    val strokes = remember(parts) { parts.map { Stroke(STROKE / it.scale, cap = StrokeCap.Round, join = StrokeJoin.Round) } }
    Spacer(
        modifier
            .size(size)
            .drawBehind {
                val unit = this.size.minDimension / GRID
                parts.forEachIndexed { i, part ->
                    withTransform({
                        translate(part.dx * unit, part.dy * unit)
                        scale(unit * part.scale, unit * part.scale, pivot = Offset.Zero)
                    }) {
                        drawPath(part.path, color, style = strokes[i])
                    }
                }
            },
    )
}

private const val GRID = 24f
private const val STROKE = 2f
