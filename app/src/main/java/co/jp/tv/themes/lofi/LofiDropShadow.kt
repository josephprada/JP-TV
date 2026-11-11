package co.jp.tv.themes.lofi

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp

/**
 * CSS `box-shadow: 0 <offsetY> <blur> <color>` under a rounded box. Uses the platform shadow
 * layer, drawn on hardware canvases from Android 9 (API 28); older devices draw no shadow.
 * The box itself is painted in [color] underneath, hidden by the opaque content drawn on top.
 */
internal fun Modifier.lofiDropShadow(color: Color, cornerRadius: Dp, offsetY: Dp, blur: Dp): Modifier =
    drawWithCache {
        // CSS blur radius = 2 sigma; Skia converts a shadow radius r to sigma = 0.57735 r + 0.5.
        val sigma = blur.toPx() / 2f
        val shadowRadius = ((sigma - 0.5f) / 0.57735f).coerceAtLeast(0f)
        val argb = color.toArgb()
        val paint = Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            this.color = argb
            setShadowLayer(shadowRadius, 0f, offsetY.toPx(), argb)
        }
        val r = cornerRadius.toPx()
        onDrawBehind {
            drawIntoCanvas { it.nativeCanvas.drawRoundRect(0f, 0f, size.width, size.height, r, r, paint) }
        }
    }
