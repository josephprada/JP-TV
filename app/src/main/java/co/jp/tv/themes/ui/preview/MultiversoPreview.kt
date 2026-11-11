package co.jp.tv.themes.ui.preview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import co.jp.tv.themes.ui.drawEllipseGradient
import kotlin.math.max

private val SpaceInner = Color(0xFF14206B)
private val SpaceMid = Color(0xFF0A1040)
private val SpaceOuter = Color(0xFF040624)
private val Violet = Color(0xFF6A5CFF)
private val Ice = Color(0xFF7FDCFF)
private val IceCore = Color(0xFFDFF0FF)
private val Pink = Color(0xFFFF9DE6)
private val Node = Color(0xFF1B2370)
private val NodeGlow = Color(0xFF9D7BFF)

private const val MAIN_LINE = "M-10 118 C100 112 200 116 430 106"
private const val BRANCHES =
    "M150 116 C190 80 230 50 270 -10 M230 112 C260 150 290 190 320 240 M110 118 C90 150 70 180 50 240 M300 108 C340 70 380 50 430 30"
private const val SPARKS = "M205 64 l-12 -8 l5 -10 M262 160 l12 6 l-4 10"

/** Thumbnail of the Multiverso home: a glowing timeline with branches and three nodes. */
@Composable
internal fun MultiversoPreview(modifier: Modifier = Modifier) {
    val paths = remember {
        Triple(
            PathParser().parsePathString(MAIN_LINE).toPath(),
            PathParser().parsePathString(BRANCHES).toPath(),
            PathParser().parsePathString(SPARKS).toPath(),
        )
    }
    Canvas(modifier.fillMaxSize()) {
        drawEllipseGradient(
            Offset(size.width / 2f, size.height / 2f),
            size.width * 0.8f,
            size.height * 0.7f,
            listOf(0f to SpaceInner, 0.5f to SpaceMid, 1f to SpaceOuter),
        )
        // The artboard SVG uses viewBox 0 0 420 230 with "xMidYMid slice".
        val s = max(size.width / 420f, size.height / 230f)
        val dx = (size.width - 420f * s) / 2f
        val dy = (size.height - 230f * s) / 2f
        val (main, branches, sparks) = paths
        translate(dx, dy) {
            scale(s, pivot = Offset.Zero) {
                // Blur is not available in a draw scope: the glow is three widening, fainter strokes.
                for ((width, alpha) in listOf(18f to 0.12f, 12f to 0.16f, 7f to 0.22f)) {
                    drawPath(main, Violet.copy(alpha = alpha), style = Stroke(width, cap = StrokeCap.Round))
                }
                for ((width, alpha) in listOf(8f to 0.18f, 5f to 0.3f)) {
                    drawPath(branches, Ice.copy(alpha = alpha), style = Stroke(width, cap = StrokeCap.Round))
                }
                drawPath(main, Color.White, style = Stroke(3f, cap = StrokeCap.Round))
                drawPath(branches, IceCore, style = Stroke(1.6f, cap = StrokeCap.Round))
                drawPath(sparks, Pink.copy(alpha = 0.8f), style = Stroke(1.2f, cap = StrokeCap.Round))
                node(120f, 116f, 12f, 0.7f, 1.5f)
                drawCircle(NodeGlow.copy(alpha = 0.18f), radius = 32f, center = Offset(210f, 115f))
                drawCircle(NodeGlow.copy(alpha = 0.25f), radius = 27f, center = Offset(210f, 115f))
                node(210f, 115f, 22f, 0.9f, 2f)
                node(300f, 111f, 12f, 0.7f, 1.5f)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.node(cx: Float, cy: Float, r: Float, strokeAlpha: Float, strokeWidth: Float) {
    drawCircle(Node, radius = r, center = Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = strokeAlpha), radius = r, center = Offset(cx, cy), style = Stroke(strokeWidth))
}
