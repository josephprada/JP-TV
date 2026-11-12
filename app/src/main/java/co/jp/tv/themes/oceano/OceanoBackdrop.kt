package co.jp.tv.themes.oceano

import java.util.Random
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The still layers of the scene for one phase and weather, baked once into plain arrays.
 *
 * [back] is the full frame of water, light shafts, far reef and sand. [foreAt]/[foreColor] are the
 * sparse pixels of the near rocks and coral, drawn after the fish so they swim behind them.
 * [causticAt]/[causticPhase]/[causticLit] are the pixels that shimmer by palette cycling, and
 * [glowAt]/[glowColor] the coral tips that twinkle at night (empty by day).
 */
internal class OceanoBackdrop(
    val back: IntArray,
    val foreAt: IntArray,
    val foreColor: IntArray,
    val causticAt: IntArray,
    val causticPhase: ByteArray,
    val causticLit: IntArray,
    val glowAt: IntArray,
    val glowColor: IntArray,
) {
    companion object {
        private const val W = OceanoPixels.W
        private const val H = OceanoPixels.H

        private val STOPS = floatArrayOf(0f, 0.14f, 0.36f, 0.62f, 1f)

        fun bake(night: Boolean, weather: OceanoWeather): OceanoBackdrop {
            val palette = if (night) OceanoPalette.night() else OceanoPalette.day()
            val back = PixelCanvas(W, H)
            val fore = PixelCanvas(W, H)
            val tips = ArrayList<IntArray>()
            paintWater(back, weather.darken)
            paintSun(back, if (night) 0.28f else 1f, weather.rays)
            paintShafts(back, if (night) 0.28f else 1f, weather.rays)
            paintFarReef(back, weather)
            paintMidRocks(back)
            paintSand(back)
            paintNearReef(fore, tips)

            val backArgb = IntArray(W * H) { palette[back.px[it].toInt()] }
            var count = 0
            for (v in fore.px) if (v.toInt() != 0) count++
            val foreAt = IntArray(count)
            val foreColor = IntArray(count)
            var n = 0
            for (i in 0 until W * H) {
                val v = fore.px[i].toInt()
                if (v != 0) {
                    foreAt[n] = i
                    foreColor[n] = palette[v]
                    n++
                }
            }
            val (cAt, cPhase, cLit) = caustics(back, fore, palette, night)
            val glowAt = if (night) IntArray(tips.size) { tips[it][1] * W + tips[it][0] } else IntArray(0)
            val glowColor = if (night) IntArray(tips.size) { palette[tips[it][2]] } else IntArray(0)
            return OceanoBackdrop(backArgb, foreAt, foreColor, cAt, cPhase, cLit, glowAt, glowColor)
        }

        private fun waterIndex(x: Int, y: Int, darken: Int): Int {
            val v = y / (H - 1f)
            var s = 0
            while (s < 3 && v > STOPS[s + 1]) s++
            val frac = (v - STOPS[s]) / (STOPS[s + 1] - STOPS[s])
            val band = if (frac * 16f > OceanoPixels.bayer(x, y)) s + 1 else s
            return minOf(Px.W4, Px.W0 + band + darken)
        }

        private fun paintWater(c: PixelCanvas, darken: Int) {
            for (y in 0 until H) for (x in 0 until W) c.set(x, y, waterIndex(x, y, darken))
        }

        private fun lightenAt(c: PixelCanvas, x: Int, y: Int, steps: Int = 1) {
            var i = c.get(x, y)
            if (i !in Px.WATER_FIRST..Px.WATER_LAST) return
            repeat(steps) { i = Px.lighten(i) }
            c.set(x, y, i)
        }

        /** The bright window of the surface seen from below. */
        private fun paintSun(c: PixelCanvas, strength: Float, rays: Float) {
            val s = strength * (0.35f + 0.65f * rays)
            val cx = 196f
            val cy = -4f
            for (y in 0 until 34) for (x in 0 until W) {
                val dx = (x - cx) / 78f
                val dy = (y - cy) / 30f
                val d = dx * dx + dy * dy
                if (d >= 1f) continue
                val level = (1f - d) * s
                if (level > 0.62f) {
                    c.set(x, y, Px.HI)
                } else if (level * 16f * 1.4f > OceanoPixels.bayer(x, y)) {
                    lightenAt(c, x, y)
                }
            }
        }

        private class Shaft(val x0: Float, val width: Float, val slant: Float, val power: Float)

        private val SHAFTS = arrayOf(
            Shaft(34f, 9f, 0.30f, 0.8f),
            Shaft(98f, 14f, 0.22f, 1f),
            Shaft(170f, 18f, 0.28f, 1f),
            Shaft(226f, 12f, 0.18f, 0.9f),
            Shaft(286f, 10f, 0.26f, 0.7f),
        )

        /** Slanted god rays, dithered, thinning with depth. */
        private fun paintShafts(c: PixelCanvas, strength: Float, rays: Float) {
            val depth = 120
            for (sh in SHAFTS) for (y in 0 until depth) {
                val fall = 1f - y / depth.toFloat()
                val w = sh.width + y * 0.2f
                val cx = sh.x0 + sh.slant * y
                val x0 = (cx - w / 2f).toInt()
                val x1 = (cx + w / 2f).toInt()
                for (x in x0..x1) {
                    val edge = 1f - abs(x + 0.5f - cx) / (w / 2f)
                    if (edge <= 0f) continue
                    val level = edge * fall * sh.power * strength * rays
                    val t = level * 16f * 0.9f
                    if (t > OceanoPixels.bayer(x, y)) lightenAt(c, x, y, if (level > 0.5f) 2 else 1)
                }
            }
        }

        private fun ridge(x: Int): Int {
            val rise = ((x - 150f) / 170f).coerceIn(0f, 1f)
            return (118 + 6 * sin(x / 19f) + 3 * sin(x / 7f + 1.3f) - 12 * rise * rise).roundToInt()
        }

        private fun paintFarReef(c: PixelCanvas, weather: OceanoWeather) {
            for (x in 0 until W) for (y in ridge(x) until H) c.set(x, y, Px.FAR)
            for (cx in intArrayOf(58, 112, 203, 262, 300)) c.ellipse(cx.toFloat(), ridge(cx) + 3f, 10f, 9f, Px.FAR)
            if (weather.haze <= 0f) return
            val t = weather.haze * 16f
            for (y in 0 until H) for (x in 0 until W) {
                if (c.get(x, y) == Px.FAR && t > OceanoPixels.bayer(x, y)) c.set(x, y, waterIndex(x, y, weather.darken))
            }
        }

        private fun paintMidRocks(c: PixelCanvas) {
            mound(c, 6f, 181f, 38f, 62f, Px.ROCK_DARK, Px.ROCK_MID, Px.ROCK_DARK)
            mound(c, 312f, 181f, 42f, 70f, Px.ROCK_DARK, Px.ROCK_MID, Px.ROCK_DARK)
            mound(c, 262f, 181f, 26f, 38f, Px.ROCK_DARK, Px.ROCK_MID, Px.ROCK_DARK)
        }

        private fun paintSand(c: PixelCanvas) {
            for (x in 0 until W) {
                val top = (152 + 3 * sin(x / 27f + 1f)).roundToInt()
                for (y in top until H) {
                    val t = (y - top) / (H - top).toFloat()
                    val (a, b, f) = if (t < 0.5f) Triple(Px.SAND_DARK, Px.SAND_MID, t * 2f) else Triple(Px.SAND_MID, Px.SAND_LIGHT, (t - 0.5f) * 2f)
                    c.set(x, y, if (f * 16f > OceanoPixels.bayer(x, y)) b else a)
                }
            }
            // ripples in the sand
            for (row in intArrayOf(158, 164, 171, 177)) {
                for (x in 0 until W) {
                    val v = sin(x * 0.21f + row * 1.7f) + 0.5f * sin(x * 0.07f + row)
                    if (v > 1.0f) c.set(x, row + ((x / 3) and 1), Px.SAND_LIGHT)
                    else if (v < -1.05f) c.set(x, row, Px.SAND_DARK)
                }
            }
        }

        /** Dome with dither shading: light from the top left, [dark] on the lower right. */
        private fun mound(c: PixelCanvas, cx: Float, baseY: Float, rx: Float, ry: Float, main: Int, light: Int, dark: Int) {
            val x0 = (cx - rx).toInt() - 1
            val x1 = (cx + rx).toInt() + 1
            val y0 = (baseY - ry).toInt() - 1
            for (y in y0..baseY.toInt()) for (x in x0..x1) {
                val u = (x + 0.5f - cx) / rx
                val v = (baseY - y - 0.5f) / ry
                if (u * u + v * v > 1f) continue
                val s = -0.55f * u + 0.65f * v
                val pick = when {
                    s > 0.62f -> light
                    s > 0.4f -> if ((s - 0.4f) / 0.22f * 16f > OceanoPixels.bayer(x, y)) light else main
                    s < -0.45f -> dark
                    s < -0.2f -> if ((-0.2f - s) / 0.25f * 16f > OceanoPixels.bayer(x, y)) dark else main
                    else -> main
                }
                c.set(x, y, pick)
            }
        }

        private fun top(c: PixelCanvas, x: Int): Int {
            for (y in 0 until H) if (c.get(x, y) != 0) return y
            return H - 1
        }

        private fun tube(c: PixelCanvas, tips: MutableList<IntArray>, x: Int, baseY: Int, w: Int, h: Int, body: Int, rim: Int, glow: Int) {
            for (y in baseY - h..baseY) for (xx in x until x + w) c.set(xx, y, if (xx == x + w - 1) Px.ROCK_DARK else body)
            c.rect(x, baseY - h - 1, x + w - 1, baseY - h - 1, rim)
            if (w > 2) c.rect(x + 1, baseY - h, x + w - 2, baseY - h, Px.INK)
            tips.add(intArrayOf(x + w / 2, baseY - h - 2, glow))
        }

        private fun fan(c: PixelCanvas, tips: MutableList<IntArray>, x: Int, baseY: Int, h: Int, color: Int, glow: Int) {
            val forkY = baseY - h * 35 / 100
            c.line(x, baseY, x, forkY, color)
            val px = IntArray(5)
            val py = IntArray(5)
            for (k in -2..2) {
                val tx = x + k * h * 18 / 100
                val ty = baseY - h + abs(k) * h * 12 / 100
                c.line(x, forkY, tx, ty, color)
                px[k + 2] = tx
                py[k + 2] = ty
            }
            for (k in 0 until 4) {
                val mx0 = x + (px[k] - x) * 6 / 10
                val my0 = forkY + (py[k] - forkY) * 6 / 10
                val mx1 = x + (px[k + 1] - x) * 6 / 10
                val my1 = forkY + (py[k + 1] - forkY) * 6 / 10
                c.line(mx0, my0, mx1, my1, color)
            }
            for (k in 0 until 5 step 2) tips.add(intArrayOf(px[k], py[k] - 1, glow))
        }

        private fun anemone(c: PixelCanvas, tips: MutableList<IntArray>, x: Int, baseY: Int, h: Int, tip: Int) {
            for (k in -3..3) {
                c.line(x + k, baseY - 2, x + k * 2, baseY - 2 - h + abs(k), Px.GREEN_LIGHT)
                c.set(x + k * 2, baseY - 2 - h + abs(k), tip)
            }
            c.ellipse(x.toFloat(), baseY - 1.5f, 4.5f, 2.5f, Px.GREEN)
            tips.add(intArrayOf(x, baseY - 3 - h, Px.GLOW_B))
        }

        private fun grass(c: PixelCanvas, x: Int, baseY: Int, h: Int) {
            for (b in -1..1) for (r in 0 until h) {
                val dx = (sin(r * 0.3f + x * 0.7f + b) * 2f * r / h).roundToInt()
                c.set(x + b * 2 + dx, baseY - r, if (r % 5 == 4) Px.GREEN_LIGHT else Px.GREEN)
            }
        }

        private fun brain(c: PixelCanvas, tips: MutableList<IntArray>, x: Int, baseY: Int, r: Int, main: Int, light: Int, glow: Int) {
            mound(c, x.toFloat(), baseY.toFloat(), r.toFloat(), r * 0.72f, main, light, Px.ROCK_DARK)
            // brain-coral ridges
            for (k in 1..2) c.set(x - r / 2 + k * r / 2, baseY - (r * 0.72f).toInt() + 2 + k, Px.ROCK_DARK)
            tips.add(intArrayOf(x - r / 3, baseY - (r * 0.72f).toInt() - 1, glow))
        }

        private fun paintNearReef(c: PixelCanvas, tips: MutableList<IntArray>) {
            val rng = Random(11)
            // rocks, left then right
            mound(c, 26f, 181f, 40f, 72f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)
            mound(c, 78f, 181f, 30f, 36f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)
            mound(c, 296f, 181f, 44f, 82f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)
            mound(c, 242f, 181f, 30f, 44f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)
            // low stones on the sand
            mound(c, 120f, 158f, 12f, 7f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)
            mound(c, 203f, 160f, 14f, 8f, Px.ROCK_MID, Px.SAND_DARK, Px.ROCK_DARK)

            // left reef
            brain(c, tips, 22, top(c, 22) + 6, 11, Px.GREEN, Px.GREEN_LIGHT, Px.GLOW_A)
            brain(c, tips, 60, top(c, 60) + 5, 9, Px.ORANGE, Px.GOLD, Px.GLOW_B)
            tube(c, tips, 32, top(c, 33) + 6, 4, 14, Px.PURPLE, Px.PINK, Px.GLOW_B)
            tube(c, tips, 38, top(c, 39) + 5, 3, 9, Px.PURPLE, Px.PINK, Px.GLOW_A)
            tube(c, tips, 68, top(c, 69) + 4, 4, 11, Px.ORANGE, Px.GOLD, Px.GLOW_A)
            fan(c, tips, 8, top(c, 8) + 4, 26, Px.RED, Px.GLOW_B)
            fan(c, tips, 48, top(c, 48) + 3, 18, Px.PURPLE, Px.GLOW_A)
            anemone(c, tips, 84, top(c, 84) + 2, 7, Px.PINK)
            for (gx in intArrayOf(14, 44, 54, 72, 90)) grass(c, gx, top(c, gx) + 2, 12 + rng.nextInt(6))

            // right reef
            brain(c, tips, 276, top(c, 276) + 6, 12, Px.PURPLE, Px.PINK, Px.GLOW_B)
            brain(c, tips, 306, top(c, 306) + 5, 9, Px.GREEN, Px.GREEN_LIGHT, Px.GLOW_A)
            tube(c, tips, 288, top(c, 289) + 6, 4, 16, Px.ORANGE, Px.GOLD, Px.GLOW_A)
            tube(c, tips, 294, top(c, 295) + 5, 3, 10, Px.ORANGE, Px.GOLD, Px.GLOW_B)
            tube(c, tips, 238, top(c, 239) + 4, 4, 12, Px.PURPLE, Px.PINK, Px.GLOW_B)
            fan(c, tips, 312, top(c, 312) + 4, 28, Px.RED, Px.GLOW_A)
            fan(c, tips, 255, top(c, 255) + 3, 20, Px.RED, Px.GLOW_B)
            anemone(c, tips, 228, top(c, 228) + 2, 8, Px.GOLD)
            anemone(c, tips, 266, top(c, 266) + 3, 7, Px.PINK)
            for (gx in intArrayOf(232, 246, 262, 282, 300, 316)) grass(c, gx, top(c, gx) + 2, 12 + rng.nextInt(6))

            // sand-level garden
            brain(c, tips, 120, 156, 6, Px.ORANGE, Px.GOLD, Px.GLOW_A)
            brain(c, tips, 205, 158, 7, Px.GREEN, Px.GREEN_LIGHT, Px.GLOW_B)
            tube(c, tips, 112, 158, 3, 7, Px.PURPLE, Px.PINK, Px.GLOW_B)
            for (gx in intArrayOf(104, 128, 192, 214)) grass(c, gx, 158, 8 + rng.nextInt(5))
        }

        /** Web of bright lines on the water near the surface and on the sand, cycled in place. */
        private fun caustics(back: PixelCanvas, fore: PixelCanvas, palette: IntArray, night: Boolean): Triple<IntArray, ByteArray, IntArray> {
            val at = ArrayList<Int>()
            val ph = ArrayList<Int>()
            val lit = ArrayList<Int>()
            val reach = if (night) 0.07f else 0.11f
            for (y in 0 until H) {
                val region = y in 4 until 52 || y in 152 until H
                if (!region) continue
                val deepen = if (y < 52) 1f - (y - 4) / 60f else 1f
                for (x in 0 until W) {
                    if (fore.get(x, y) != 0) continue
                    val a = abs(sin(x * 0.27f + 2f * sin(y * 0.31f)))
                    val b = abs(sin(y * 0.33f + 2f * sin(x * 0.23f)))
                    if (minOf(a, b) >= reach * deepen * (if (y >= 152) 0.7f else 1f)) continue
                    val idx = back.get(x, y)
                    val l = Px.lighten(idx)
                    if (l == idx) continue
                    at.add(y * W + x)
                    ph.add(((x / 4) + (y / 3)) and 7)
                    lit.add(palette[l])
                }
            }
            return Triple(at.toIntArray(), ByteArray(ph.size) { ph[it].toByte() }, lit.toIntArray())
        }
    }
}
