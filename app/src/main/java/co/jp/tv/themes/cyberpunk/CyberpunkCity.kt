package co.jp.tv.themes.cyberpunk

import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * Every particle of the artboard, generated once with the design's seeded generator
 * (`seed = 23`, Park-Miller 16807) and in the same call order, so positions match the design.
 * Geometry is in design px. "Container" x values are relative to the far/mid layers, which sit
 * at left -40 px. Colors are indices into [CyberpunkPalette] lists; phase alphas apply at draw time.
 */
internal class CyberpunkCity private constructor(
    val towers: List<Tower>,
    val farLights: List<FarLight>,
    val billboards: List<Billboard>,
    val blocks: List<Block>,
    val windows: List<Window>,
    val roofLines: List<RoofLine>,
    val streaks: List<Streak>,
    val splashes: List<Splash>,
    val drops: List<Drop>,
) {
    class Tower(val x: Float, val w: Float, val h: Float)

    /** Far-layer animated light, kept in DOM order so later towers paint over earlier glows. */
    sealed class FarLight {
        /** Index of the tower drawn just before this light. */
        abstract val tower: Int
    }

    class Dot(override val tower: Int, val x: Float, val bottom: Float, val color: Int, val dur: Float, val delay: Float) : FarLight()

    class Strip(override val tower: Int, val x: Float, val bottom: Float, val h: Float, val color: Int, val dur: Float, val delay: Float) : FarLight()

    class BeaconLight(override val tower: Int, val x: Float, val bottom: Float, val delay: Float) : FarLight()

    class Billboard(val x: Float, val y: Float, val w: Float, val h: Float, val color: Int, val dur: Float, val delay: Float)

    class Block(val x: Float, val w: Float, val h: Float)

    class Window(val x: Float, val bottom: Float, val color: Int)

    class RoofLine(val x: Float, val bottom: Float, val w: Float, val color: Int)

    class Streak(val color: Int, val x: Float, val w: Float, val h: Float)

    class Splash(val x: Float, val y: Float, val dur: Float, val delay: Float)

    class Drop(val x: Float, val h: Float, val dur: Float, val delay: Float)

    companion object {
        /** Design billboards: x, y, w, h and color as a [CyberpunkPalette.Neon] index (0 cyan, 1 pink, 2 violet, 3 yellow). */
        private val BILLBOARDS = listOf(
            floatArrayOf(150f, 300f, 120f, 180f, 1f),
            floatArrayOf(720f, 240f, 96f, 150f, 0f),
            floatArrayOf(1230f, 280f, 150f, 96f, 2f),
            floatArrayOf(1600f, 220f, 104f, 170f, 3f),
            floatArrayOf(460f, 380f, 84f, 120f, 0f),
        )

        fun generate(): CyberpunkCity {
            val rng = Rng(23L)
            val towers = ArrayList<Tower>()
            val lights = ArrayList<FarLight>()
            var x = -40
            while (x < 2000) {
                val w = rng.int(60, 150)
                val h = rng.int(380, 760)
                val t = towers.size
                towers += Tower(x.toFloat(), w.toFloat(), h.toFloat())
                var k = 0
                while (k < rng.int(4, 12)) {
                    val c = rng.int(0, 3)
                    val left = x + rng.int(4, w - 8)
                    val bottom = 300 + rng.int(20, h - 30)
                    val dur = fixed(2 + rng.next() * 5, 1)
                    val delay = fixed(rng.next() * 4, 1)
                    lights += Dot(t, left.toFloat(), bottom.toFloat(), c, dur, delay)
                    k++
                }
                if (rng.next() > 0.6) {
                    val c = rng.int(0, 1)
                    val bottom = 300 + rng.int(60, h - 200)
                    val sh = rng.int(80, 180)
                    val dur = fixed(5 + rng.next() * 6, 1)
                    val delay = fixed(rng.next() * 3, 1)
                    lights += Strip(t, (x + w - 4).toFloat(), bottom.toFloat(), sh.toFloat(), c, dur, delay)
                }
                if (h > 600) {
                    val delay = fixed(rng.next() * 2, 1)
                    lights += BeaconLight(t, (x + w / 2 - 3).toFloat(), (300 + h).toFloat(), delay)
                }
                x += w
            }
            val billboards = BILLBOARDS.map { b ->
                val dur = fixed(6 + rng.next() * 5, 1)
                val delay = fixed(rng.next() * 4, 1)
                Billboard(b[0], b[1], b[2], b[3], b[4].toInt(), dur, delay)
            }
            val blocks = ArrayList<Block>()
            val windows = ArrayList<Window>()
            val roofs = ArrayList<RoofLine>()
            x = -30
            while (x < 2000) {
                val w = rng.int(110, 200)
                val h = rng.int(240, 440)
                blocks += Block(x.toFloat(), w.toFloat(), h.toFloat())
                val cols = (w - 10) / 14
                val rows = (h - 20) / 18
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if (rng.next() < 0.1) {
                            val col = rng.int(0, 3)
                            windows += Window((x + 8 + c * 14).toFloat(), (310 + r * 18).toFloat(), col)
                        }
                    }
                }
                if (rng.next() > 0.55) {
                    val c = rng.int(0, 2)
                    roofs += RoofLine(x.toFloat(), (300 + h - 6).toFloat(), w.toFloat(), c)
                }
                x += w
            }
            val streaks = List(26) {
                val c = rng.int(0, 3)
                val left = rng.int(0, 1900)
                val w = rng.int(6, 16)
                val h = rng.int(110, 240)
                Streak(c, left.toFloat(), w.toFloat(), h.toFloat())
            }
            val splashes = List(26) {
                val left = rng.int(0, 1900)
                val top = rng.int(800, 1060)
                val dur = fixed(1 + rng.next(), 2)
                val delay = fixed(rng.next() * 2, 2)
                Splash(left.toFloat(), top.toFloat(), dur, delay)
            }
            val drops = List(110) {
                val left = rng.int(0, 2320)
                val h = rng.int(40, 100)
                val dur = fixed(0.55 + rng.next() * 0.6, 2)
                val delay = fixed(rng.next() * 1.5, 2)
                Drop(left.toFloat(), h.toFloat(), dur, delay)
            }
            return CyberpunkCity(towers, lights, billboards, blocks, windows, roofs, streaks, splashes, drops)
        }

        /** JS `toFixed(digits)` as a float. */
        private fun fixed(value: Double, digits: Int): Float {
            val scale = if (digits == 1) 10.0 else 100.0
            return ((value * scale).roundToLong() / scale).toFloat()
        }
    }

    /** The design's `rnd()` and `ri(a, b)`. */
    private class Rng(private var seed: Long) {
        fun next(): Double {
            seed = (seed * 16807L) % 2147483647L
            return seed / 2147483647.0
        }

        fun int(a: Int, b: Int): Int = a + floor(next() * (b - a + 1)).toInt()
    }
}
