package co.jp.tv.themes.gotham

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.floor
import kotlin.math.roundToInt

/** Artboard height; the skyline is anchored to its bottom edge. */
private const val ArtboardHeight = 1080f

/** One static piece of a skyline layer, in design px with a top-left origin. */
internal sealed interface CityShape {
    /** A building block: vertical body gradient, optional window grid, lit left edge. */
    class Block(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val body: Brush,
        val edge: Color,
        val grid: Boolean,
    ) : CityShape

    class Fill(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val color: Color,
        val corner: Float = 0f,
        /** Alpha of a 1px lit left edge (inset box-shadow); 0 for none. */
        val edgeAlpha: Float = 0f,
        /** Alpha of a 1px lit top edge; 0 for none. */
        val topEdgeAlpha: Float = 0f,
    ) : CityShape

    /** A filled polygon (spire, water-tower roof). */
    class Spike(val path: Path, val color: Color) : CityShape

    /** A lit window, 7x11 at 0.8 opacity; near windows also glow. */
    class Window(val left: Float, val top: Float, val color: Color, val glow: Boolean) : CityShape
}

/** Blinking red aircraft light on an antenna; [delayMs] offsets its `ng-blink`. */
internal class CityBeacon(val centerX: Float, val centerY: Float, val delayMs: Float)

/** Floating dust in the searchlight beam, in the rig's local design px. */
internal class BeamMote(
    val left: Float,
    val top: Float,
    val size: Float,
    val durationMs: Float,
    val delayMs: Float,
)

/** One rain streak inside the rotated rain sheet. */
internal class RainDrop(
    val left: Float,
    val size: Size,
    val brush: Brush,
    val durationMs: Float,
    val delayMs: Float,
)

/**
 * The procedural night city of the artboard, generated once. It replays the artboard's
 * Park-Miller generator (seed 11) in the same call order so every building, window,
 * antenna, mote and raindrop lands where the design puts it.
 */
internal class GothamCity {
    val far = ArrayList<CityShape>()
    val mid = ArrayList<CityShape>()
    val near = ArrayList<CityShape>()
    val midBeacons = ArrayList<CityBeacon>()
    val nearBeacons = ArrayList<CityBeacon>()
    val motes: List<BeamMote>
    val rain: List<RainDrop>

    private val random = DesignRandom(11L)

    init {
        var bx = -30
        while (bx < 1940) {
            val w = random.int(60, 120)
            build(far, null, bx, w, random.int(220, 440), FarStyle)
            bx += w
        }
        bx = -20
        while (bx < 1940) {
            if (bx in 440 until 650) {
                bx = 650
                continue
            }
            var w = random.int(70, 130)
            if (bx < 440 && bx + w > 440) w = 440 - bx
            if (w < 30) {
                bx += w
                continue
            }
            build(mid, midBeacons, bx, w, random.int(170, 380), MidStyle)
            bx += w
        }
        // Rooftop that carries the searchlight.
        build(mid, midBeacons, 440, 170, 300, RoofStyle)
        mid += fill(436, 300, 178, 8, GothamPalette.Ledge, topEdgeAlpha = 0.25f)
        for (x in 440 until 610 step 14) mid += fill(x, 308, 2, 16, GothamPalette.Railing)
        mid += fill(440, 322, 170, 2, GothamPalette.Railing)
        mid += fill(560, 308, 38, 26, GothamPalette.RoofHut, edgeAlpha = 0.2f)
        mid += fill(452, 308, 22, 12, GothamPalette.RoofVent)
        bx = -10
        while (bx < 1940) {
            val w = random.int(90, 170)
            build(near, nearBeacons, bx, w, random.int(100, 250), NearStyle)
            bx += w
        }
        motes = List(22) {
            val y = random.int(80, 640)
            val half = 28.0 + ((700 - y) / 700.0) * 250.0
            val mx = 280.0 + (random.next() * 2 - 1) * half * 0.85
            val size = random.int(1, 3)
            val duration = tenths(5 + random.next() * 6)
            val delay = tenths(random.next() * 5)
            BeamMote(mx.roundToInt().toFloat(), y.toFloat(), size.toFloat(), duration * 1000f, delay * 1000f)
        }
        rain = List(110) {
            val h = 40 + floor(random.next() * 60).toInt()
            val left = floor(random.next() * 2320).toInt()
            val duration = hundredths(0.7 + random.next() * 0.6)
            val delay = hundredths(random.next() * 1.5)
            RainDrop(
                left = left.toFloat(),
                size = Size(1f, h.toFloat()),
                brush = Brush.verticalGradient(
                    listOf(GothamPalette.Rain.copy(alpha = 0f), GothamPalette.Rain.copy(alpha = 0.35f)),
                    startY = 0f,
                    endY = h.toFloat(),
                ),
                durationMs = duration * 1000f,
                delayMs = delay * 1000f,
            )
        }
    }

    /** Port of the artboard's `build(out, x, w, h, o)`; bottoms are measured from the artboard foot. */
    private fun build(
        out: MutableList<CityShape>,
        beacons: MutableList<CityBeacon>?,
        x: Int,
        w: Int,
        h: Int,
        style: LayerStyle,
    ) {
        val edge = GothamPalette.Edge.copy(alpha = style.edge)
        out += block(x, 0, w, h, style, edge, grid = style.grid)
        var cx = x
        var cw = w
        var ch = h
        if (style.details) {
            val steps = random.int(0, 2)
            repeat(steps) {
                val nw = floor(cw * (0.62 + random.next() * 0.2)).toInt()
                val nx = cx + floorDiv2(cw - nw)
                val nh = random.int(24, 54)
                out += block(nx, ch, nw, nh, style, edge, grid = false)
                cx = nx
                cw = nw
                ch += nh
            }
            val roll = random.next()
            val mid = cx + floorDiv2(cw)
            if (roll < 0.3 && cw > 40) {
                val sh = random.int(50, 90)
                out += CityShape.Spike(triangle(mid - 9, ch, 18, sh), style.top)
            } else if (roll < 0.62) {
                val ah = random.int(40, 90)
                out += fill(mid, ch, 2, ah, style.top)
                val delay = tenths(random.next() * 2) * 1000f
                // 6x6 light whose bottom sits at ch + ah.
                beacons?.add(CityBeacon(mid - 2 + 3f, ArtboardHeight - (ch + ah) - 3f, delay))
            } else if (roll < 0.85 && cw > 56) {
                val tx = cx + random.int(8, cw - 34)
                out += fill(tx, ch, 2, 14, style.foot)
                out += fill(tx + 20, ch, 2, 14, style.foot)
                out += fill(tx - 2, ch + 14, 26, 24, style.top, corner = 3f)
                out += CityShape.Spike(triangle(tx - 4, ch + 38, 30, 10), style.foot)
            }
            if (cw > 30) {
                out += fill(cx + random.int(4, cw - 22), ch, 16, 9, style.foot)
            }
        }
        val cols = floorDiv(w - 9, 16)
        val rows = floorDiv(h - 24, 22)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (random.next() < style.lit) {
                    val color = if (random.next() < 0.72) {
                        GothamPalette.WarmWindows[random.int(0, 2)]
                    } else {
                        GothamPalette.CoolWindows[random.int(0, 1)]
                    }
                    val bottom = 11 + r * 22
                    out += CityShape.Window(
                        left = (x + 9 + c * 16).toFloat(),
                        top = ArtboardHeight - bottom - 11f,
                        color = color,
                        glow = style.glow,
                    )
                }
            }
        }
    }

    private fun block(x: Int, bottom: Int, w: Int, h: Int, style: LayerStyle, edge: Color, grid: Boolean): CityShape {
        val top = ArtboardHeight - bottom - h
        return CityShape.Block(
            left = x.toFloat(),
            top = top,
            width = w.toFloat(),
            height = h.toFloat(),
            body = Brush.verticalGradient(listOf(style.top, style.foot), startY = top, endY = top + h),
            edge = edge,
            grid = grid,
        )
    }

    private fun fill(
        x: Int,
        bottom: Int,
        w: Int,
        h: Int,
        color: Color,
        corner: Float = 0f,
        edgeAlpha: Float = 0f,
        topEdgeAlpha: Float = 0f,
    ): CityShape = CityShape.Fill(
        left = x.toFloat(),
        top = ArtboardHeight - bottom - h,
        width = w.toFloat(),
        height = h.toFloat(),
        color = color,
        corner = corner,
        edgeAlpha = edgeAlpha,
        topEdgeAlpha = topEdgeAlpha,
    )

    /** `clip-path: polygon(50% 0, 100% 100%, 0 100%)` on a box whose bottom is [bottom]. */
    private fun triangle(x: Int, bottom: Int, w: Int, h: Int): Path {
        val foot = ArtboardHeight - bottom
        return Path().apply {
            moveTo(x + w / 2f, foot - h)
            lineTo((x + w).toFloat(), foot)
            lineTo(x.toFloat(), foot)
            close()
        }
    }

    private class LayerStyle(
        val top: Color,
        val foot: Color,
        val edge: Float,
        val grid: Boolean,
        val details: Boolean,
        val lit: Double,
        val glow: Boolean,
    )

    private companion object {
        val FarStyle = LayerStyle(GothamPalette.FarTop, GothamPalette.FarFoot, 0.1f, grid = true, details = false, lit = 0.05, glow = false)
        val MidStyle = LayerStyle(GothamPalette.MidTop, GothamPalette.MidFoot, 0.16f, grid = true, details = true, lit = 0.18, glow = false)
        val RoofStyle = LayerStyle(GothamPalette.RoofTop, GothamPalette.MidFoot, 0.2f, grid = true, details = false, lit = 0.2, glow = false)
        val NearStyle = LayerStyle(GothamPalette.NearTop, GothamPalette.NearFoot, 0.12f, grid = false, details = true, lit = 0.1, glow = true)

        /** JS `Math.floor(a / b)`, also for negative numerators. */
        fun floorDiv(a: Int, b: Int): Int = Math.floorDiv(a, b)

        fun floorDiv2(a: Int): Int = Math.floorDiv(a, 2)

        /** JS `toFixed(1)` / `toFixed(2)` back to a number. */
        fun tenths(v: Double): Float = ((v * 10).roundToInt() / 10.0).toFloat()

        fun hundredths(v: Double): Float = ((v * 100).roundToInt() / 100.0).toFloat()
    }
}

/** The artboard's `rnd()` and `ri(a, b)`: Park-Miller minimal standard generator. */
private class DesignRandom(private var seed: Long) {
    fun next(): Double {
        seed = (seed * 16807L) % 2147483647L
        return seed / 2147483647.0
    }

    fun int(a: Int, b: Int): Int = a + floor(next() * (b - a + 1)).toInt()
}
