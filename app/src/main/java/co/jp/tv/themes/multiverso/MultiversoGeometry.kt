package co.jp.tv.themes.multiverso

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Scene geometry of the artboard's `renderVals()`, in design pixels (1920x1080). Every
 * pseudo-random value uses the artboard's Park-Miller generator and call order, so positions match.
 * Build once (remember); never call from a draw lambda.
 */
internal object MultiversoGeometry {
    /** Branch box of one app: the artboard's 700x1080 SVG, node center at [NodeCenter]. */
    const val BranchBoxWidth = 700f
    val NodeCenter = Offset(350f, 600f)

    /** `(seed * 16807) % 2147483647`, exactly as the artboard computes it. */
    class Random(seed: Long) {
        private var state = seed
        fun next(): Double {
            state = (state * 16807L) % 2147483647L
            return state.toDouble() / 2147483647.0
        }
    }

    class Branch(val up: Path, val down: Path, val twigs: Path)

    class Star(
        val x: Float,
        val y: Float,
        val size: Float,
        val pink: Boolean,
        val glow: Boolean,
        val durationMs: Long,
        val delayMs: Long,
    )

    class Sky(val far: List<Star>, val near: List<Star>, val farLines: List<Path>)

    private class P(val x: Double, val y: Double)

    private fun bez(p0: P, p1: P, p2: P, p3: P, t: Double): P {
        val u = 1 - t
        fun axis(a: Double, b: Double, c: Double, d: Double) =
            u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d
        return P(axis(p0.x, p1.x, p2.x, p3.x), axis(p0.y, p1.y, p2.y, p3.y))
    }

    /** `v.toFixed(1)`. */
    private fun f1(v: Double): Float = ((v * 10).roundToInt() / 10.0).toFloat()

    /** `v.toFixed(0)`. */
    private fun f0(v: Double): Float = v.roundToInt().toFloat()

    /** Branch of the app at [index]: seed `97 + i * 131`, odd indices lean right. */
    fun branch(index: Int): Branch {
        val rnd = Random(97L + index * 131L)
        val sign = if (index % 2 != 0) 1.0 else -1.0
        val dx = sign * (120 + rnd.next() * 170)
        val p0 = P(350.0, 600.0)
        val p1 = P(350 + dx * 0.05, 470.0)
        val p2 = P(350 + dx * 0.6, 250.0)
        val p3 = P(350 + dx, -40.0)
        val q = bez(p0, p1, p2, p3, 0.42)
        val fx = dx * 0.55 + sign * 90
        val f1p = P(q.x + fx * 0.15, q.y - 90)
        val f2p = P(q.x + fx * 0.6, q.y - 190)
        val f3p = P(q.x + fx, q.y - 300)
        val up = Path().apply {
            moveTo(p0.x.toFloat(), p0.y.toFloat())
            cubicTo(p1.x.toFloat(), p1.y.toFloat(), p2.x.toFloat(), p2.y.toFloat(), p3.x.toFloat(), p3.y.toFloat())
            moveTo(f1(q.x), f1(q.y))
            cubicTo(f1(f1p.x), f1(f1p.y), f1(f2p.x), f1(f2p.y), f1(f3p.x), f1(f3p.y))
        }
        val dd = -dx * 0.55
        val d1 = P(350 + dd * 0.05, 700.0)
        val d2 = P(350 + dd * 0.6, 880.0)
        val d3 = P(350 + dd, 1120.0)
        val down = Path().apply {
            moveTo(350f, 600f)
            cubicTo(f1(d1.x), f1(d1.y), f1(d2.x), f1(d2.y), f1(d3.x), f1(d3.y))
        }
        val twigs = Path()
        fun twig(p: P, base: Double) {
            twigs.moveTo(f1(p.x), f1(p.y))
            var a = base
            for (k in 0 until 4) {
                a += (if (k % 2 != 0) 1 else -1) * (0.5 + rnd.next() * 0.4)
                val len = 14 + rnd.next() * 14
                twigs.relativeLineTo(f1(cos(a) * len), f1(sin(a) * len))
            }
        }
        // The artboard evaluates each twig's base angle (one draw) before the twig's own draws.
        for (t in doubleArrayOf(0.3, 0.58, 0.8)) {
            val start = bez(p0, p1, p2, p3, t)
            val base = -PI / 2 + (if (rnd.next() > 0.5) 0.9 else -0.9)
            twig(start, base)
        }
        twig(bez(q, f1p, f2p, f3p, 0.6), -PI / 2 + sign * 0.8)
        val downStart = bez(p0, d1, d2, d3, 0.5)
        val downBase = PI / 2 + (if (rnd.next() > 0.5) 0.8 else -0.8)
        twig(downStart, downBase)
        return Branch(up, down, twigs)
    }

    /** One braided strand: `y = 600 + off + amp sin(x f + ph) + 0.4 amp sin(2.3 x f + 1.7 ph)`. */
    private fun strand(amp: Double, freq: Double, ph: Double, off: Double): Path {
        val path = Path()
        var x = -60
        while (x <= 1980) {
            val y = 600 + off + amp * sin(x * freq + ph) + amp * 0.4 * sin(x * freq * 2.3 + ph * 1.7)
            if (x == -60) path.moveTo(x.toFloat(), f1(y)) else path.lineTo(x.toFloat(), f1(y))
            x += 30
        }
        return path
    }

    /** strandSpec: amp, freq, phase, offset. Drawn alphas and widths live in [StrandAlphas]/[StrandWidths]. */
    fun strands(): List<Path> = listOf(
        strand(3.0, 0.004, 0.0, 0.0),
        strand(7.0, 0.003, 1.2, -2.0),
        strand(10.0, 0.0026, 2.4, 3.0),
        strand(5.0, 0.005, 4.0, 1.0),
        strand(12.0, 0.002, 0.7, -1.0),
    )

    val StrandAlphas = floatArrayOf(0.95f, 0.6f, 0.5f, 0.7f, 0.42f)
    val StrandWidths = floatArrayOf(2.6f, 1.4f, 1.2f, 1f, 1.6f)

    /** Stars and distant timelines; seed 5, generated in the artboard's order. */
    fun sky(): Sky {
        val rnd = Random(5L)
        fun star(big: Int): Star {
            val left = floor(rnd.next() * 2000).toFloat()
            val top = floor(rnd.next() * 1080).toFloat()
            val pink = rnd.next() > 0.75
            val duration = 3 + rnd.next() * 5
            val delay = rnd.next() * 5
            return Star(
                x = left,
                y = top,
                size = big.toFloat(),
                pink = pink,
                glow = big > 2,
                durationMs = (f1(duration) * 1000).roundToInt().toLong(),
                delayMs = (f1(delay) * 1000).roundToInt().toLong(),
            )
        }
        val far = List(90) { star(if (rnd.next() > 0.8) 2 else 1) }
        val near = List(26) { star(3) }
        val lines = List(7) {
            val y0 = 80 + rnd.next() * 920
            val y1 = 80 + rnd.next() * 920
            val c1x = 400 + rnd.next() * 300
            val c1y = y0 + (rnd.next() - 0.5) * 500
            val c2x = 1300 + rnd.next() * 300
            val c2y = y1 + (rnd.next() - 0.5) * 500
            Path().apply {
                moveTo(-90f, f0(y0))
                cubicTo(f0(c1x), f0(c1y), f0(c2x), f0(c2y), 2010f, f0(y1))
            }
        }
        return Sky(far, near, lines)
    }
}
