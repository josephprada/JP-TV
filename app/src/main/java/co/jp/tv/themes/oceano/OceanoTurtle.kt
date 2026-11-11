package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer

/**
 * The green sea turtle crossing near the surface (`sw-cross-r` 95 s, delay 28 s, `sw-tbob` 18 px
 * over 6 s), with its rear flippers (`sw-rflip`) and front flippers (`sw-fflip` / `sw-fflip2`)
 * stroking every 3.4 s. Shapes follow the artboard's inline SVG, in its viewBox (-170, -150).
 */
@Composable
internal fun OceanoTurtle(clock: OceanoClock, modifier: Modifier = Modifier) {
    val parts = remember { TurtleParts() }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        inDesignSpace {
            val travel = OceanoMotion.lerp(-420f, 2340f, OceanoMotion.loop(t, 95f, 28f))
            val bob = -18f * OceanoMotion.pulse(t, 6f)
            withTransform({
                translate(travel + 170f, 120f + 150f + bob)
                rotate(-6f, Offset.Zero)
            }) {
                parts.draw(this, t)
            }
        }
    }
}

private class TurtleParts {
    private val skin = Brush.linearGradient(
        listOf(OceanoPalette.TurtleSkinLight, OceanoPalette.TurtleSkinDark),
        start = Offset(-150f, -142f),
        end = Offset(152f, 142f),
    )
    private val shell = Brush.linearGradient(
        listOf(OceanoPalette.TurtleShellLight, OceanoPalette.TurtleShellMid, OceanoPalette.TurtleShellDark),
        start = Offset(-116f, -72f),
        end = Offset(96f, 72f),
    )
    private val rearTop = Path().apply {
        moveTo(-100f, -38f); cubicTo(-128f, -52f, -146f, -46f, -150f, -34f); cubicTo(-136f, -30f, -118f, -28f, -98f, -26f); close()
    }
    private val rearBottom = Path().apply {
        moveTo(-100f, 38f); cubicTo(-128f, 52f, -146f, 46f, -150f, 34f); cubicTo(-136f, 30f, -118f, 28f, -98f, 26f); close()
    }
    private val frontTop = Path().apply {
        moveTo(38f, -44f); cubicTo(18f, -96f, -38f, -138f, -92f, -142f); cubicTo(-62f, -112f, -18f, -78f, 10f, -36f); close()
    }
    private val frontTopLines = Path().apply {
        moveTo(30f, -50f); lineTo(12f, -70f); moveTo(14f, -40f); lineTo(-10f, -80f)
        moveTo(-6f, -60f); lineTo(-40f, -100f); moveTo(-30f, -96f); lineTo(-64f, -122f)
    }
    private val frontBottom = Path().apply {
        moveTo(38f, 44f); cubicTo(18f, 96f, -38f, 138f, -92f, 142f); cubicTo(-62f, 112f, -18f, 78f, 10f, 36f); close()
    }
    private val frontBottomLines = Path().apply {
        moveTo(30f, 50f); lineTo(12f, 70f); moveTo(14f, 40f); lineTo(-10f, 80f)
        moveTo(-6f, 60f); lineTo(-40f, 100f); moveTo(-30f, 96f); lineTo(-64f, 122f)
    }
    private val head = Path().apply {
        moveTo(78f, -12f); cubicTo(92f, -16f, 104f, -18f, 120f, -16f); cubicTo(144f, -14f, 152f, -4f, 152f, 0f)
        cubicTo(152f, 4f, 144f, 14f, 120f, 16f); cubicTo(104f, 18f, 92f, 16f, 78f, 12f); close()
    }
    private val headLines = Path().apply {
        moveTo(122f, -10f); lineTo(132f, -12f); lineTo(140f, -6f); lineTo(132f, 0f); lineTo(140f, 6f); lineTo(132f, 12f); lineTo(122f, 10f)
        moveTo(108f, -12f); lineTo(114f, 0f); lineTo(108f, 12f)
    }
    private val scutes = Path().apply {
        poly(52f, -18f, 72f, 0f, 52f, 18f, 30f, 14f, 30f, -14f)
        poly(30f, -14f, 30f, 14f, 4f, 18f, -6f, 0f, 4f, -18f)
        poly(4f, -18f, -6f, 0f, 4f, 18f, -24f, 16f, -34f, 0f, -24f, -16f)
        poly(-24f, -16f, -34f, 0f, -24f, 16f, -52f, 14f, -62f, 0f, -52f, -14f)
        poly(-52f, -14f, -62f, 0f, -52f, 14f, -84f, 10f, -96f, 0f, -84f, -10f)
        poly(52f, -18f, 30f, -14f, 4f, -18f, 18f, -52f, 48f, -46f)
        poly(4f, -18f, -24f, -16f, -40f, -58f, -8f, -62f, 18f, -52f)
        poly(-24f, -16f, -52f, -14f, -76f, -50f, -40f, -58f)
        poly(-52f, -14f, -84f, -10f, -100f, -36f, -76f, -50f)
        poly(52f, 18f, 30f, 14f, 4f, 18f, 18f, 52f, 48f, 46f)
        poly(4f, 18f, -24f, 16f, -40f, 58f, -8f, 62f, 18f, 52f)
        poly(-24f, 16f, -52f, 14f, -76f, 50f, -40f, 58f)
        poly(-52f, 14f, -84f, 10f, -100f, 36f, -76f, 50f)
    }
    private val lineStroke = Stroke(width = 2f)
    private val headStroke = Stroke(width = 1.6f, join = StrokeJoin.Round)
    private val scuteStroke = Stroke(width = 2.4f, join = StrokeJoin.Round, cap = StrokeCap.Round)
    private val rimStroke = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
    private val outline = Rect(-122f, -78f, 102f, 78f)
    private val shellOval = Rect(-116f, -72f, 96f, 72f)
    private val sheen = Rect(-88f, -50f, 28f, -2f)

    fun draw(scope: DrawScope, t: Float) = with(scope) {
        val rear = OceanoMotion.lerp(-12f, 12f, OceanoMotion.alternate(t, 3.4f))
        val rearB = OceanoMotion.lerp(-12f, 12f, OceanoMotion.alternate(t, 3.4f, 1.7f))
        val front = OceanoMotion.lerp(14f, -20f, OceanoMotion.alternate(t, 3.4f))
        val frontB = OceanoMotion.lerp(-14f, 20f, OceanoMotion.alternate(t, 3.4f))
        rotate(rear, Offset(-98f, -39f)) { drawPath(rearTop, skin) }
        rotate(rearB, Offset(-98f, 39f)) { drawPath(rearBottom, skin) }
        rotate(front, Offset(31.5f, -41.3f)) {
            drawPath(frontTop, skin)
            drawPath(frontTopLines, OceanoPalette.TurtleLines, style = lineStroke)
        }
        rotate(frontB, Offset(31.5f, 41.3f)) {
            drawPath(frontBottom, skin)
            drawPath(frontBottomLines, OceanoPalette.TurtleLines, style = lineStroke)
        }
        drawPath(head, skin)
        drawPath(headLines, OceanoPalette.TurtleLines, style = headStroke)
        drawCircle(OceanoPalette.TurtleEye, radius = 2.6f, center = Offset(138f, -9f))
        drawCircle(OceanoPalette.TurtleEye, radius = 2.6f, center = Offset(138f, 9f))
        drawOval(OceanoPalette.TurtleOutline, outline.topLeft, outline.size)
        drawOval(shell, shellOval.topLeft, shellOval.size)
        drawPath(scutes, OceanoPalette.TurtleScute)
        drawPath(scutes, OceanoPalette.TurtleOutline, style = scuteStroke)
        drawOval(OceanoPalette.TurtleRim, shellOval.topLeft, shellOval.size, style = rimStroke)
        drawOval(OceanoPalette.TurtleSheen, sheen.topLeft, sheen.size)
    }
}

private fun Path.poly(vararg xy: Float) {
    moveTo(xy[0], xy[1])
    var i = 2
    while (i + 1 < xy.size) {
        lineTo(xy[i], xy[i + 1])
        i += 2
    }
    close()
}
