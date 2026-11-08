package co.jp.tv.core.design

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Glass surface: translucent fill, 1dp border, and a faint highlight along the top edge.
 *
 * On API 31+ the fill is lighter because blur is expected behind it; backdrop blur through
 * RenderEffect needs the wallpaper captured in a layer and is not wired yet, so every API
 * currently renders the "fake glass" (no blur) and only the fill density differs.
 */
fun Modifier.glass(shape: Shape, fill: Color? = null): Modifier {
    val resolvedFill = fill ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) JpColors.Glass else JpColors.GlassFake
    return this
        .background(resolvedFill, shape)
        .topHighlight(shape)
        .border(JpDimens.Hairline, JpColors.Line, shape)
}

/** Inner top highlight: a bright hairline at the top edge that fades out within [fadeDp]. */
private fun Modifier.topHighlight(shape: Shape, fadeDp: Float = 28f): Modifier = drawWithCache {
    val outline = shape.createOutline(size, layoutDirection, this)
    val fade = fadeDp.dp.toPx().coerceAtMost(size.height)
    val brush = Brush.verticalGradient(
        0f to JpColors.GlassHighlight,
        1f to Color.Transparent,
        startY = 0f,
        endY = fade,
    )
    val stroke = Stroke(width = 1.dp.toPx())
    val inset = stroke.width / 2f
    val radius = (outline as? Outline.Rounded)?.roundRect?.topLeftCornerRadius?.x?.minus(inset)?.coerceAtLeast(0f) ?: 0f
    onDrawBehind {
        drawRoundRect(
            brush = brush,
            topLeft = Offset(inset, inset),
            size = Size(size.width - stroke.width, size.height - stroke.width),
            cornerRadius = CornerRadius(radius, radius),
            style = stroke,
        )
    }
}
