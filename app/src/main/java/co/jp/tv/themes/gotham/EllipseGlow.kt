package co.jp.tv.themes.gotham

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.sqrt

/**
 * CSS `radial-gradient(ellipse, stops)` painted on a `border-radius: 50%` box at [left], [top]
 * of [width] x [height] design px. CSS sizes the ellipse to the farthest corner, so a stop at
 * 100% lies at sqrt(2) times the half-axes; the box clips it to the inscribed ellipse.
 * Built once; [draw] allocates nothing.
 */
internal class EllipseGlow(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    vararg stops: Pair<Float, Color>,
) {
    private val glowCenter = Offset(left + width / 2f, top + height / 2f)
    private val radius = width / 2f
    private val squash = height / width
    private val brush = Brush.radialGradient(
        *stops,
        center = glowCenter,
        radius = radius * sqrt(2f),
    )

    fun draw(scope: DrawScope, alpha: Float = 1f) {
        scope.scale(scaleX = 1f, scaleY = squash, pivot = glowCenter) {
            drawCircle(brush = brush, radius = radius, center = glowCenter, alpha = alpha)
        }
    }
}
