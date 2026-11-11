package co.jp.tv.themes.oceano

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Corals scattered along a reef skyline: brain corals, table corals, tube sponges and branching
 * corals, as on the artboard. The artboard places hundreds of hand-seeded pieces (with dot and
 * maze textures); this generator draws the same kinds of pieces with the artboard's colors from
 * a fixed seed, so the reef looks the same on every launch. Built once, in design px.
 */
internal class OceanoReefDressing private constructor(
    private val shadows: List<Oval>,
    private val brains: List<Brain>,
    private val tables: List<Table>,
    private val tubes: List<Tube>,
    private val branches: List<Branch>,
) {
    class Oval(val topLeft: Offset, val size: Size)
    class Brain(val topLeft: Offset, val size: Size, val brush: Brush)
    class Table(
        val stemTopLeft: Offset,
        val stemSize: Size,
        val stemColor: Color,
        val underTopLeft: Offset,
        val underSize: Size,
        val underColor: Color,
        val topTopLeft: Offset,
        val topSize: Size,
        val topBrush: Brush,
    )
    class Tube(
        val topLeft: Offset,
        val size: Size,
        val brush: Brush,
        val mouthTopLeft: Offset,
        val mouthSize: Size,
        val mouth: Color,
        val rim: Color,
    )
    class Branch(val levels: List<Path>, val colors: List<Color>, val strokes: List<Stroke>)

    /** Kinds and colors a reef band uses; the far bands are smaller and hazier. */
    class Style(
        val scale: Float,
        val brainColors: List<List<Color>>,
        val tableColors: List<TableColors>,
        val tubeColors: List<TubeColors>,
        val branchColors: List<List<Color>>,
    )
    class TableColors(val top: List<Color>, val under: Color, val stem: Color)
    class TubeColors(val gradient: List<Color>, val mouth: Color, val rim: Color)

    fun draw(scope: DrawScope) = with(scope) {
        shadows.forEach { drawOval(OceanoPalette.ReefShadow, it.topLeft, it.size) }
        branches.forEach { b ->
            b.levels.forEachIndexed { i, path ->
                drawPath(path, b.colors[i], style = b.strokes[i])
            }
        }
        tables.forEach {
            drawRect(it.stemColor, it.stemTopLeft, it.stemSize)
            drawOval(it.underColor, it.underTopLeft, it.underSize)
            drawOval(it.topBrush, it.topTopLeft, it.topSize)
        }
        brains.forEach { drawOval(it.brush, it.topLeft, it.size) }
        tubes.forEach {
            drawRoundRect(it.brush, it.topLeft, it.size, CornerRadius(it.size.width / 2f))
            drawOval(it.mouth, it.mouthTopLeft, it.mouthSize)
            drawOval(it.rim, it.mouthTopLeft, it.mouthSize, style = RimStroke)
        }
    }

    companion object {
        private val RimStroke = Stroke(width = 1.2f)

        /** Scatters pieces along [ridge] between [fromX] and [toX], resting on the skyline. */
        fun along(ridge: OceanoRidge, fromX: Float, toX: Float, seed: Int, style: Style): OceanoReefDressing {
            val random = Random(seed)
            val shadows = mutableListOf<Oval>()
            val brains = mutableListOf<Brain>()
            val tables = mutableListOf<Table>()
            val tubes = mutableListOf<Tube>()
            val branches = mutableListOf<Branch>()
            val s = style.scale
            var x = fromX + random.nextFloat() * 40f
            while (x < toX) {
                val ground = ridge.y(x) + 6f * s
                when (random.nextInt(4)) {
                    0 -> {
                        val rx = (18f + random.nextFloat() * 48f) * s
                        val ry = rx * 0.72f
                        val cy = ground - ry * 0.55f
                        shadows += Oval(Offset(x - rx * 1.07f, ground + ry * 0.35f - 3f), Size(rx * 2.14f, ry * 0.3f))
                        val colors = style.brainColors[random.nextInt(style.brainColors.size)]
                        val brush = Brush.radialGradient(
                            colors,
                            center = Offset(x - rx * 0.25f, cy - ry * 0.35f),
                            radius = rx * 1.25f,
                        )
                        brains += Brain(Offset(x - rx, cy - ry), Size(rx * 2f, ry * 2f), brush)
                    }
                    1 -> {
                        val rx = (34f + random.nextFloat() * 50f) * s
                        val ry = rx * 0.16f
                        val stemH = rx * 0.55f
                        val top = ground - stemH
                        val c = style.tableColors[random.nextInt(style.tableColors.size)]
                        val stemW = rx * 0.16f
                        tables += Table(
                            stemTopLeft = Offset(x - stemW / 2f, top),
                            stemSize = Size(stemW, stemH),
                            stemColor = c.stem,
                            underTopLeft = Offset(x - rx * 0.97f, top + ry * 0.3f - ry),
                            underSize = Size(rx * 1.94f, ry * 2.1f),
                            underColor = c.under,
                            topTopLeft = Offset(x - rx, top - ry * 1.1f),
                            topSize = Size(rx * 2f, ry * 1.9f),
                            topBrush = Brush.radialGradient(c.top, Offset(x - rx * 0.2f, top - ry), rx * 1.1f),
                        )
                    }
                    2 -> {
                        val c = style.tubeColors[random.nextInt(style.tubeColors.size)]
                        val n = 3 + random.nextInt(3)
                        repeat(n) {
                            val w = (5f + random.nextFloat() * 11f) * s
                            val h = (20f + random.nextFloat() * 80f) * s
                            val tx = x + (random.nextFloat() - 0.5f) * 44f * s
                            val ty = ground - h
                            val brush = Brush.horizontalGradient(
                                0f to c.gradient[0],
                                0.42f to c.gradient[1],
                                0.6f to c.gradient[2],
                                1f to c.gradient[3],
                                startX = tx,
                                endX = tx + w,
                            )
                            val mw = w * 0.84f
                            tubes += Tube(
                                topLeft = Offset(tx, ty),
                                size = Size(w, h),
                                brush = brush,
                                mouthTopLeft = Offset(tx + (w - mw) / 2f, ty + w * 0.04f),
                                mouthSize = Size(mw, mw * 0.4f),
                                mouth = c.mouth,
                                rim = c.rim,
                            )
                        }
                    }
                    else -> {
                        val colors = style.branchColors[random.nextInt(style.branchColors.size)]
                        branches += branch(x, ground, (20f + random.nextFloat() * 14f) * s, random, colors, s)
                    }
                }
                x += (70f + random.nextFloat() * 90f) * s
            }
            return OceanoReefDressing(shadows, brains, tables, tubes, branches)
        }

        /** A small staghorn: two trunks forking three times, one path per depth. */
        private fun branch(x: Float, ground: Float, length: Float, random: Random, colors: List<Color>, s: Float): Branch {
            val levels = List(colors.size) { Path() }
            fun grow(px: Float, py: Float, angle: Float, len: Float, depth: Int) {
                if (depth >= levels.size) return
                val nx = px + sin(angle) * len
                val ny = py - cos(angle) * len
                levels[depth].moveTo(px, py)
                levels[depth].lineTo(nx, ny)
                val spread = 0.35f + random.nextFloat() * 0.3f
                grow(nx, ny, angle - spread, len * 0.78f, depth + 1)
                grow(nx, ny, angle + spread, len * 0.78f, depth + 1)
            }
            grow(x - 6f * s, ground, -0.4f, length, 0)
            grow(x + 6f * s, ground, 0.4f, length, 0)
            val strokes = List(colors.size) { i -> Stroke(width = (6.4f - i * 1.3f) * s, cap = StrokeCap.Round) }
            return Branch(levels, colors, strokes)
        }
    }
}
