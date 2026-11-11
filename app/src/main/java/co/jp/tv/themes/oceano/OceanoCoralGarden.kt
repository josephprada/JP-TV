package co.jp.tv.themes.oceano

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The swaying life on the foreground reef (`sw-sway`, ±2.4° around each base): five gorgonian
 * fans, four anemones and three seagrass clumps, at the artboard's positions and periods. Their
 * branching is generated from a fixed seed with the artboard's depth, stroke widths and spread.
 */
@Composable
internal fun OceanoCoralGarden(clock: OceanoClock, modifier: Modifier = Modifier) {
    val pieces = remember { buildGarden() }
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = clock.seconds
        inDesignSpace {
            for (i in pieces.indices) {
                val piece = pieces[i]
                val sway = OceanoMotion.lerp(-2.4f, 2.4f, OceanoMotion.alternate(t, piece.periodS, piece.delayS))
                withTransform({
                    translate(piece.left, piece.top)
                    rotate(sway, piece.pivot)
                }) {
                    if (piece.under != null) drawPath(piece.under, piece.underColor)
                    for (s in piece.strokes.indices) {
                        drawPath(piece.strokes[s], piece.strokeColors[s], alpha = piece.strokeAlphas[s], style = piece.strokeStyles[s])
                    }
                    if (piece.over != null) drawPath(piece.over, piece.overColor)
                }
            }
        }
    }
}

private class GardenPiece(
    val left: Float,
    val top: Float,
    val pivot: Offset,
    val periodS: Float,
    val delayS: Float,
    val strokes: List<Path>,
    val strokeColors: List<Color>,
    val strokeAlphas: FloatArray,
    val strokeStyles: List<Stroke>,
    /** Filled before the strokes (anemone base). */
    val under: Path? = null,
    val underColor: Color = Color.Transparent,
    /** Filled after the strokes (anemone tips). */
    val over: Path? = null,
    val overColor: Color = Color.Transparent,
)

private fun buildGarden(): List<GardenPiece> {
    val random = Random(57)
    return listOf(
        fan(random, -59f, 633f, 418f, 247f, 4.3f, 3.3f, OceanoPalette.FanViolet, OceanoPalette.FanVioletMesh),
        fan(random, 395f, 765f, 330f, 195f, 4.0f, 3.4f, OceanoPalette.FanRed, OceanoPalette.FanRedMesh),
        fan(random, 1263f, 539f, 374f, 221f, 4.3f, 2.6f, OceanoPalette.FanPurple, OceanoPalette.FanPurpleMesh),
        fan(random, 1610f, 540f, 440f, 260f, 4.1f, 3.8f, OceanoPalette.FanCrimson, OceanoPalette.FanCrimsonMesh),
        fan(random, 1486f, 518f, 308f, 182f, 6.4f, 1.4f, OceanoPalette.FanViolet, OceanoPalette.FanVioletMesh),
        anemone(random, 308f, 910f, 144f, 90f, 3.2f, 0.8f, OceanoPalette.AnemoneGreen, OceanoPalette.AnemoneGreenTip, OceanoPalette.AnemoneGreenBase),
        anemone(random, 1256f, 930f, 128f, 80f, 3.1f, 1.2f, OceanoPalette.AnemonePink, OceanoPalette.AnemonePinkTip, OceanoPalette.AnemonePinkBase),
        anemone(random, 1820f, 940f, 160f, 100f, 3.5f, 3.6f, OceanoPalette.AnemoneGreen, OceanoPalette.AnemoneGreenTip, OceanoPalette.AnemoneGreenBase),
        anemone(random, 6f, 970f, 128f, 80f, 4.1f, 0.1f, OceanoPalette.AnemonePink, OceanoPalette.AnemonePinkTip, OceanoPalette.AnemonePinkBase),
        seagrass(random, 760f, 970f, 3.4f, 3.4f),
        seagrass(random, 1020f, 970f, 3.8f, 0.3f),
        seagrass(random, 640f, 970f, 3.6f, 2.9f),
    )
}

/** A sea fan: a trunk forking six times (strokes 3.3 -> 0.6 at 418 px wide) over five mesh arcs. */
private fun fan(random: Random, left: Float, top: Float, w: Float, h: Float, period: Float, delay: Float, color: Color, mesh: Color): GardenPiece {
    val k = w / 418f
    val widths = floatArrayOf(3.3f, 2.9f, 2.4f, 2.0f, 1.5f, 1.1f, 0.6f)
    val levels = List(widths.size) { Path() }
    fun grow(x: Float, y: Float, angle: Float, len: Float, depth: Int) {
        if (depth >= levels.size) return
        val nx = x + sin(angle) * len
        val ny = y - cos(angle) * len
        levels[depth].moveTo(x, y)
        levels[depth].lineTo(nx, ny)
        val spread = 0.2f + random.nextFloat() * 0.06f
        grow(nx, ny, angle - spread, len * 0.83f, depth + 1)
        grow(nx, ny, angle + spread, len * 0.83f, depth + 1)
    }
    grow(w / 2f, h, 0f, h * 0.155f, 0)
    val meshPath = Path()
    val radii = floatArrayOf(57f, 81.7f, 106.4f, 131.1f, 155.8f)
    for (r in radii) {
        val radius = r * k
        val sweep = 109f
        meshPath.arcTo(
            Rect(w / 2f - radius, h - radius, w / 2f + radius, h + radius),
            startAngleDegrees = -90f - sweep / 2f,
            sweepAngleDegrees = sweep,
            forceMoveTo = true,
        )
    }
    val strokes = levels + meshPath
    val colors = List(levels.size) { color } + mesh
    val alphas = FloatArray(strokes.size) { if (it < levels.size) 1f else 0.45f }
    val styles = widths.map { Stroke(width = it * k, cap = StrokeCap.Round) } + Stroke(width = 0.8f * k)
    return GardenPiece(left, top, Offset(w / 2f, h), period, delay, strokes, colors, alphas, styles)
}

/** An anemone: 36 curled tentacles with bright tips over a round base. */
private fun anemone(
    random: Random,
    left: Float,
    top: Float,
    w: Float,
    h: Float,
    period: Float,
    delay: Float,
    color: Color,
    tip: Color,
    base: Color,
): GardenPiece {
    val k = w / 144f
    val tentacles = Path()
    val tips = Path()
    val baseY = h * 0.86f
    repeat(36) {
        val angle = (random.nextFloat() - 0.5f) * 2.6f
        val len = (34f + random.nextFloat() * 24f) * k
        val sx = w / 2f + (random.nextFloat() - 0.5f) * 16f * k
        val ex = sx + sin(angle) * len
        val ey = baseY - cos(angle) * len
        val bend = (random.nextFloat() - 0.5f) * 0.9f
        val mx = sx + sin(angle + bend) * len * 0.55f
        val my = baseY - cos(angle + bend) * len * 0.55f
        tentacles.moveTo(sx, baseY)
        tentacles.quadraticBezierTo(mx, my, ex, ey)
        val r = 2.7f * k
        tips.addOval(Rect(ex - r, ey - r, ex + r, ey + r))
    }
    val basePath = Path().apply {
        addOval(Rect(w / 2f - w / 8f, h * 0.92f - h * 0.12f, w / 2f + w / 8f, h * 0.92f + h * 0.12f))
    }
    return GardenPiece(
        left, top, Offset(w / 2f, h), period, delay,
        strokes = listOf(tentacles),
        strokeColors = listOf(color),
        strokeAlphas = floatArrayOf(1f),
        strokeStyles = listOf(Stroke(width = 4f * k, cap = StrokeCap.Round)),
        under = basePath,
        underColor = base,
        over = tips,
        overColor = tip,
    )
}

/** Seagrass: nine blades leaning out from a 120x110 clump. */
private fun seagrass(random: Random, left: Float, top: Float, period: Float, delay: Float): GardenPiece {
    val blades = List(9) { Path() }
    val styles = ArrayList<Stroke>()
    repeat(9) { i ->
        val x = 40f + random.nextFloat() * 42f
        val lean = (random.nextFloat() - 0.5f) * 50f
        val tipY = 14f + random.nextFloat() * 38f
        blades[i].moveTo(x, 110f)
        blades[i].quadraticBezierTo(x + lean * 0.2f, 110f - (110f - tipY) * 0.5f, x + lean, tipY)
        styles += Stroke(width = 2.3f + random.nextFloat(), cap = StrokeCap.Round)
    }
    return GardenPiece(
        left, top, Offset(60f, 110f), period, delay,
        strokes = blades,
        strokeColors = List(9) { OceanoPalette.Seagrass },
        strokeAlphas = FloatArray(9) { 1f },
        strokeStyles = styles,
    )
}
