package co.jp.tv.themes.shared

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A thin rounded ring [outset] outside the element while [visible]: the focus mark of a header control. */
fun Modifier.controlOutline(
    visible: Boolean,
    color: Color,
    corner: Dp = 8.dp,
    outset: Dp = 3.dp,
    width: Dp = 1.5.dp,
): Modifier = drawBehind {
    if (visible) {
        val gap = outset.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(-gap, -gap),
            size = Size(size.width + gap * 2f, size.height + gap * 2f),
            cornerRadius = CornerRadius(corner.toPx() + gap),
            style = Stroke(width = width.toPx()),
        )
    }
}
