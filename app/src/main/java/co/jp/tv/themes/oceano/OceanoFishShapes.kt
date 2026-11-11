package co.jp.tv.themes.oceano

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The artboard's fish symbols (`fAnth`, `fAnthFar`, `fSerg`, `fAngel`, `fSilv`, `fYel`, `fSnap`,
 * `fShark`, `fManta`) rebuilt as paths in their own viewBox, facing right. Built once.
 */
internal class OceanoFishShapes private constructor() {
    class Part(val path: Path, val color: Color, val brush: Brush?, val alpha: Float, val style: DrawStyle)

    class Shape(val viewWidth: Float, val viewHeight: Float, val parts: List<Part>) {
        /** Draws in view coordinates; [tint] replaces every color when given (silhouettes). */
        fun draw(scope: DrawScope, alpha: Float, tint: Color? = null) {
            for (i in parts.indices) {
                val part = parts[i]
                if (tint != null) {
                    scope.drawPath(part.path, tint, alpha = alpha * part.alpha, style = part.style)
                } else if (part.brush != null) {
                    scope.drawPath(part.path, part.brush, alpha = alpha * part.alpha, style = part.style)
                } else {
                    scope.drawPath(part.path, part.color, alpha = alpha * part.alpha, style = part.style)
                }
            }
        }
    }

    val anthias = Shape(
        60f, 26f,
        listOf(
            fill(path { moveTo(9f, 13f); lineTo(0f, 3f); quadraticBezierTo(5f, 13f, 0f, 23f); close() }, OceanoPalette.AnthTail, 0.9f),
            fill(path { moveTo(20f, 6f); quadraticBezierTo(32f, -3f, 46f, 7f); close() }, OceanoPalette.AnthFin, 0.85f),
            fill(path { moveTo(26f, 19f); quadraticBezierTo(30f, 26f, 36f, 20f); close() }, OceanoPalette.AnthFin, 0.8f),
            gradient(
                body(8f, 13f, 16f, 4f, 38f, 2f, 54f, 10f, 59f, 54f, 16f, 38f, 24f, 16f, 22f),
                Brush.verticalGradient(
                    0f to OceanoPalette.AnthTop,
                    0.55f to OceanoPalette.AnthMid,
                    1f to OceanoPalette.AnthBelly,
                    startY = 3f,
                    endY = 22f,
                ),
            ),
            stroke(path { moveTo(16f, 15f); cubicTo(26f, 21f, 42f, 20f, 54f, 15f) }, OceanoPalette.AnthLine, 1.2f),
            fill(circle(50f, 11f, 2f), OceanoPalette.AnthEye),
            fill(circle(50.6f, 10.4f, 0.6f), OceanoPalette.White),
        ),
    )

    val anthiasFar = Shape(
        60f, 26f,
        listOf(
            fill(path { moveTo(9f, 13f); lineTo(0f, 3f); quadraticBezierTo(5f, 13f, 0f, 23f); close() }, OceanoPalette.AnthFarTail),
            fill(body(8f, 13f, 16f, 4f, 38f, 2f, 54f, 10f, 59f, 54f, 16f, 38f, 24f, 16f, 22f), OceanoPalette.AnthFarBody),
        ),
    )

    val sergeant = Shape(
        60f, 30f,
        listOf(
            fill(path { moveTo(10f, 15f); lineTo(0f, 5f); quadraticBezierTo(5f, 15f, 0f, 25f); close() }, OceanoPalette.SergTail),
            fill(path { moveTo(18f, 7f); quadraticBezierTo(32f, -1f, 46f, 8f); close() }, OceanoPalette.SergFin),
            gradient(
                body(8f, 15f, 16f, 2f, 42f, 1f, 56f, 12f, 59f, 56f, 18f, 42f, 29f, 16f, 28f),
                Brush.verticalGradient(
                    0f to OceanoPalette.SergTop,
                    0.5f to OceanoPalette.SergMid,
                    1f to OceanoPalette.SergBelly,
                    startY = 2f,
                    endY = 28f,
                ),
            ),
            fill(
                path {
                    quad(20f, 6f, 24f, 5f, 24f, 25f, 20f, 24f)
                    quad(29f, 4f, 33f, 4f, 33f, 26f, 29f, 26f)
                    quad(38f, 4f, 42f, 5f, 42f, 25f, 38f, 26f)
                    quad(46f, 7f, 49f, 8f, 49f, 22f, 46f, 23f)
                },
                OceanoPalette.SergBar,
            ),
            fill(circle(52f, 13f, 2f), OceanoPalette.FishEye),
        ),
    )

    val angel = Shape(
        64f, 44f,
        listOf(
            fill(path { moveTo(12f, 22f); lineTo(0f, 10f); quadraticBezierTo(6f, 22f, 0f, 34f); close() }, OceanoPalette.AngelTail),
            gradient(
                body(10f, 22f, 14f, 4f, 40f, 0f, 54f, 12f, 62f, 54f, 32f, 40f, 44f, 14f, 40f),
                Brush.linearGradient(
                    *angelStripes(),
                    start = Offset(10f, 3f),
                    end = Offset(25f, 41f),
                ),
            ),
            fill(
                path {
                    moveTo(44f, 8f)
                    cubicTo(48f, 14f, 48f, 30f, 44f, 36f)
                    lineTo(50f, 34f)
                    cubicTo(53f, 28f, 53f, 16f, 50f, 10f)
                    close()
                },
                OceanoPalette.AngelFace,
            ),
            stroke(path { moveTo(54f, 14f); quadraticBezierTo(60f, 22f, 54f, 30f) }, OceanoPalette.AngelLine, 2f),
            fill(circle(52f, 19f, 2.2f), OceanoPalette.FishEye),
        ),
    )

    val silverside = Shape(
        60f, 22f,
        listOf(
            fill(path { moveTo(9f, 11f); lineTo(0f, 2f); quadraticBezierTo(5f, 11f, 0f, 20f); close() }, OceanoPalette.SilvGold),
            gradient(
                body(8f, 11f, 18f, 2f, 40f, 1f, 55f, 9f, 59f, 55f, 13f, 40f, 21f, 18f, 20f),
                Brush.verticalGradient(
                    0f to OceanoPalette.SilvTop,
                    0.5f to OceanoPalette.SilvMid,
                    1f to OceanoPalette.SilvBelly,
                    startY = 2f,
                    endY = 20f,
                ),
            ),
            stroke(path { moveTo(12f, 9f); cubicTo(24f, 5f, 42f, 5f, 54f, 9f) }, OceanoPalette.SilvGold, 1.8f),
            fill(circle(50f, 10f, 1.8f), OceanoPalette.SilvEye),
        ),
    )

    val yellowtail = Shape(
        60f, 28f,
        listOf(
            fill(path { moveTo(10f, 14f); lineTo(0f, 4f); quadraticBezierTo(5f, 14f, 0f, 24f); close() }, OceanoPalette.YelTop),
            fill(path { moveTo(20f, 6f); quadraticBezierTo(32f, -2f, 46f, 7f); close() }, OceanoPalette.YelFin),
            gradient(
                body(8f, 14f, 16f, 2f, 42f, 1f, 55f, 11f, 59f, 55f, 17f, 42f, 27f, 16f, 26f),
                Brush.verticalGradient(
                    0f to OceanoPalette.YelTop,
                    0.5f to OceanoPalette.YelMid,
                    1f to OceanoPalette.YelBelly,
                    startY = 2f,
                    endY = 26f,
                ),
            ),
            stroke(path { moveTo(14f, 16f); cubicTo(26f, 20f, 42f, 19f, 54f, 15f) }, OceanoPalette.YelLine, 1.4f),
            fill(circle(50f, 12f, 2f), OceanoPalette.YelEye),
        ),
    )

    /** Silhouette symbols: drawn with a tint. */
    val snapper = Shape(
        80f, 30f,
        listOf(
            fill(
                path {
                    moveTo(12f, 15f); lineTo(0f, 3f); quadraticBezierTo(6f, 15f, 0f, 27f); close()
                    moveTo(10f, 15f); cubicTo(20f, 2f, 54f, 0f, 74f, 11f); quadraticBezierTo(80f, 15f, 74f, 19f)
                    cubicTo(54f, 30f, 20f, 28f, 10f, 15f); close()
                    moveTo(28f, 6f); quadraticBezierTo(44f, -3f, 60f, 6f); close()
                },
                OceanoPalette.FarSnapper,
            ),
        ),
    )

    val shark = Shape(
        200f, 70f,
        listOf(
            fill(
                path {
                    moveTo(16f, 36f); lineTo(0f, 6f); lineTo(26f, 30f); lineTo(8f, 66f); lineTo(22f, 40f); close()
                    moveTo(92f, 20f); lineTo(114f, 0f); lineTo(120f, 22f); close()
                    moveTo(122f, 44f); lineTo(104f, 68f); lineTo(140f, 46f); close()
                    moveTo(10f, 36f); cubicTo(44f, 20f, 112f, 16f, 172f, 29f); quadraticBezierTo(196f, 33f, 198f, 37f)
                    quadraticBezierTo(182f, 44f, 152f, 46f); cubicTo(102f, 53f, 44f, 48f, 10f, 36f); close()
                },
                OceanoPalette.FarShark,
            ),
        ),
    )

    val manta = Shape(
        160f, 90f,
        listOf(
            fill(
                path {
                    moveTo(4f, 40f); quadraticBezierTo(58f, -6f, 80f, 28f); quadraticBezierTo(102f, -6f, 156f, 40f)
                    quadraticBezierTo(104f, 52f, 80f, 48f); quadraticBezierTo(56f, 52f, 4f, 40f); close()
                    moveTo(79f, 46f); lineTo(81f, 46f); lineTo(80f, 88f); close()
                },
                OceanoPalette.FarManta,
            ),
            stroke(
                path {
                    moveTo(70f, 28f); quadraticBezierTo(66f, 16f, 72f, 14f)
                    moveTo(90f, 28f); quadraticBezierTo(94f, 16f, 88f, 14f)
                },
                OceanoPalette.FarManta,
                4f,
            ),
        ),
    )

    companion object {
        fun build(): OceanoFishShapes = OceanoFishShapes()

        private fun path(block: Path.() -> Unit): Path = Path().apply(block)

        private fun circle(cx: Float, cy: Float, r: Float): Path = Path().apply {
            addOval(Rect(cx - r, cy - r, cx + r, cy + r))
        }

        /** The symbols' body: `M x0 y0 C a b, c d, e f Q tip y0 e g C h i, j k, x0 y0 Z`. */
        private fun body(
            x0: Float, y0: Float,
            c1x: Float, c1y: Float, c2x: Float, c2y: Float, ex: Float, ey: Float,
            tip: Float, bx: Float, by: Float,
            d1x: Float, d1y: Float, d2x: Float, d2y: Float,
        ): Path = Path().apply {
            moveTo(x0, y0)
            cubicTo(c1x, c1y, c2x, c2y, ex, ey)
            quadraticBezierTo(tip, y0, bx, by)
            cubicTo(d1x, d1y, d2x, d2y, x0, y0)
            close()
        }

        private fun Path.quad(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float) {
            moveTo(x1, y1); lineTo(x2, y2); lineTo(x3, y3); lineTo(x4, y4); close()
        }

        private fun fill(path: Path, color: Color, alpha: Float = 1f) = Part(path, color, null, alpha, Fill)

        private fun gradient(path: Path, brush: Brush) = Part(path, Color.Unspecified, brush, 1f, Fill)

        private fun stroke(path: Path, color: Color, width: Float) =
            Part(path, color, null, 1f, Stroke(width = width, cap = StrokeCap.Round))

        /** `gAngel`: hard blue and yellow bands. */
        private fun angelStripes(): Array<Pair<Float, Color>> {
            val edges = floatArrayOf(0.08f, 0.14f, 0.24f, 0.3f, 0.4f, 0.46f, 0.56f, 0.62f, 0.72f, 0.78f)
            val stops = ArrayList<Pair<Float, Color>>()
            stops += 0f to OceanoPalette.AngelBlue
            var yellow = false
            for (edge in edges) {
                stops += edge to if (yellow) OceanoPalette.AngelYellow else OceanoPalette.AngelBlue
                yellow = !yellow
                stops += edge to if (yellow) OceanoPalette.AngelYellow else OceanoPalette.AngelBlue
            }
            stops += 1f to OceanoPalette.AngelBlue
            return stops.toTypedArray()
        }
    }
}
