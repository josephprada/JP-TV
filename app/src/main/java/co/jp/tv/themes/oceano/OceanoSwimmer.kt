package co.jp.tv.themes.oceano

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * One fish of the scene with its artboard motion, all in design px:
 * - [Motion.Hover]: `sw-hov-*`, eases to ([a], [b]) and back.
 * - [Motion.Dart]: `sw-dart-*`, holds, darts to ([a], [b]), settles at ([c], [d]), and back.
 * - [Motion.Orbit]: `sw-path` on an ellipse centred ([a], [b]) with radii ([c], [d]), starting at its
 *   left end and going over the top (under it when [reverse]); the fish turns at each end.
 * - [Motion.Cross]: `sw-cross-r` / `sw-cross-l`, crossing from -420 to 2340 px (or back) at height [y].
 */
internal class OceanoSwimmer(
    val shape: OceanoFishShapes.Shape,
    val width: Float,
    val height: Float,
    val alpha: Float,
    val motion: Motion,
    /** Top-left of the fish for Hover, Dart and Cross; offset from the path for Orbit. */
    val x: Float,
    val y: Float,
    /** 1 faces right, -1 faces left (Orbit ignores it and faces its direction of travel). */
    val facing: Float,
    val periodS: Float,
    val delayS: Float,
    val a: Float = 0f,
    val b: Float = 0f,
    val c: Float = 0f,
    val d: Float = 0f,
    val reverse: Boolean = false,
    val tint: Color? = null,
    /** `sw-wag` / `sw-swag` tail beat, as a small rocking angle; 0 disables it. */
    val wagS: Float = 0f,
    val wagDeg: Float = 4f,
    /** `sw-fbob` / `sw-tbob`: vertical bob of [bobPx] over [bobS]; 0 disables it. */
    val bobS: Float = 0f,
    val bobPx: Float = 0f,
    /** `sw-flap`: wings squash to 78 % height (manta). */
    val flapS: Float = 0f,
) {
    enum class Motion { Hover, Dart, Orbit, Cross }

    fun draw(scope: DrawScope, t: Float, layerAlpha: Float) {
        var cx = 0f
        var cy = 0f
        var face = facing
        when (motion) {
            Motion.Hover -> {
                val p = OceanoMotion.alternate(t, periodS, delayS)
                cx = x + width / 2f + a * p
                cy = y + height / 2f + b * p
            }
            Motion.Dart -> {
                val raw = (t + delayS) / periodS
                val u = raw - 2f * floor(raw / 2f)
                val f = if (u < 1f) u else 2f - u
                var dx = 0f
                var dy = 0f
                if (f >= 0.76f) {
                    val k = OceanoMotion.smooth((f - 0.76f) / 0.24f)
                    dx = OceanoMotion.lerp(a, c, k)
                    dy = OceanoMotion.lerp(b, d, k)
                } else if (f >= 0.68f) {
                    val k = OceanoMotion.smooth((f - 0.68f) / 0.08f)
                    dx = a * k
                    dy = b * k
                }
                cx = x + width / 2f + dx
                cy = y + height / 2f + dy
            }
            Motion.Orbit -> {
                val p = OceanoMotion.loop(t, periodS, delayS)
                val theta = if (reverse) PI - 2.0 * PI * p else PI + 2.0 * PI * p
                cx = a + c * cos(theta).toFloat() + x
                cy = b + d * sin(theta).toFloat() + y
                val dxdt = -sin(theta) * (if (reverse) -1.0 else 1.0)
                face = if (dxdt >= 0.0) 1f else -1f
            }
            Motion.Cross -> {
                val p = OceanoMotion.loop(t, periodS, delayS)
                val travel = if (facing > 0f) OceanoMotion.lerp(-420f, 2340f, p) else OceanoMotion.lerp(2340f, -420f, p)
                cx = x + travel + width / 2f
                cy = y + height / 2f
            }
        }
        if (bobS > 0f) cy -= bobPx * OceanoMotion.pulse(t, bobS, delayS)
        val wag = if (wagS > 0f) OceanoMotion.lerp(-wagDeg, wagDeg, OceanoMotion.alternate(t, wagS, delayS)) else 0f
        val squash = if (flapS > 0f) OceanoMotion.lerp(1f, 0.78f, OceanoMotion.alternate(t, flapS)) else 1f
        val sx = face * width / shape.viewWidth
        val sy = squash * height / shape.viewHeight
        val vw = shape.viewWidth
        val vh = shape.viewHeight
        val drawAlpha = alpha * layerAlpha
        scope.withTransform({
            translate(cx, cy)
            rotate(wag, Offset.Zero)
            scale(sx, sy, Offset.Zero)
            translate(-vw / 2f, -vh / 2f)
        }) {
            shape.draw(this, drawAlpha, tint)
        }
    }
}
