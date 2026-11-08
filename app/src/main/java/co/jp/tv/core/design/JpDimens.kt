package co.jp.tv.core.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** JP design tokens: radii and lines. The design is 1920x1080; dp values are half of its px. */
object JpDimens {
    val RailRadius = 18.dp
    val RowRadius = 12.dp
    val IconRadius = 8.dp
    val Hairline = 1.dp

    val RailShape = RoundedCornerShape(RailRadius)
    val RowShape = RoundedCornerShape(RowRadius)
    val IconShape = RoundedCornerShape(IconRadius)
    val PillShape = CircleShape
}
