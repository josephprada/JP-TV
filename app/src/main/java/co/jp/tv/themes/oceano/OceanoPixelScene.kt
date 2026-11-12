package co.jp.tv.themes.oceano

import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/** The app icon of the focused jelly, already cut down to the virtual pixel grid (ARGB, opaque). */
internal class OceanoIcon(val w: Int, val h: Int, val argb: IntArray) {
    companion object {
        /** Cells across and down for a banner icon and for a square one. */
        private const val BANNER_W = 16
        private const val BANNER_H = 9
        private const val SQUARE = 9

        /**
         * Shrinks a [sw] x [sh] ARGB picture to the pixel grid: each cell takes a 3x3 spread of samples,
         * and every channel is posterised to four levels so it sits with the palette.
         */
        fun fromArgb(src: IntArray, sw: Int, sh: Int): OceanoIcon {
            val banner = sw.toFloat() / sh > 1.3f
            val w = if (banner) BANNER_W else SQUARE
            val h = if (banner) BANNER_H else SQUARE
            val out = IntArray(w * h)
            for (cy in 0 until h) for (cx in 0 until w) {
                var r = 0
                var g = 0
                var b = 0
                var n = 0
                for (sy in 0 until 3) for (sx in 0 until 3) {
                    val x = ((cx + (sx + 0.5f) / 3f) * sw / w).toInt().coerceIn(0, sw - 1)
                    val y = ((cy + (sy + 0.5f) / 3f) * sh / h).toInt().coerceIn(0, sh - 1)
                    val p = src[y * sw + x]
                    r += (p shr 16) and 255
                    g += (p shr 8) and 255
                    b += p and 255
                    n++
                }
                out[cy * w + cx] = (0xFF shl 24) or (level(r / n) shl 16) or (level(g / n) shl 8) or level(b / n)
            }
            return OceanoIcon(w, h, out)
        }

        private fun level(c: Int): Int = (c * 3 + 127) / 255 * 85
    }
}

/** What the scene needs to know each frame. The owner mutates one instance; nothing here allocates. */
internal class OceanoSceneInput {
    /** Animation step, 15 per second. */
    var tick = 0

    /** 0 = day palette, 1 = night palette; in between the two dither into each other. */
    var night = 0f

    /** Animated index of the focused app. */
    var scroll = 0f
    var focused = 0
    var count = 0

    /** Glyph index of each app's first letter (see [OceanoSprites.glyphIndex]). */
    var initials: IntArray = IntArray(0)
    var icon: OceanoIcon? = null
}

/** Where things sit, shared by the pixel scene and the text drawn over it. All in virtual pixels. */
internal object OceanoLayout {
    const val STEP = 280f / 6f
    const val CENTER_X = OceanoPixels.W / 2f
    const val TICK_MS = 66

    fun jellyX(index: Int, scroll: Float): Int = (CENTER_X + (index - scroll) * STEP).roundToInt()

    /** 1 on the focused jelly, falling to 0 a full step away. */
    fun focusOf(index: Int, scroll: Float): Float = (1f - abs(index - scroll)).coerceIn(0f, 1f)

    fun sizeIndex(focus: Float): Int = if (focus > 0.8f) 2 else if (focus > 0.34f) 1 else 0

    /** Top row of the bell of a jelly of this size, before its bob. */
    fun bellTop(sizeIndex: Int): Int = 46 - 3 * sizeIndex

    /** Row where a jelly's label sits, from 87 idle to 113 focused. */
    fun labelY(focus: Float): Float = 87f + 26f * focus

    const val VISIBLE_RADIUS = 3
}

/**
 * The whole Océano scene as pixel art: one 320x180 ARGB frame, rebuilt from two baked backdrops and a few
 * hundred sprite blits. Pure Kotlin on plain arrays, so it renders the same on the TV and in the JVM tests.
 * [render] allocates nothing; the buffers live as long as the scene.
 */
internal class OceanoPixelScene(val weather: OceanoWeather = OceanoWeather.Clear) {
    val frame = IntArray(OceanoPixels.W * OceanoPixels.H)

    private val dayBackdrop = OceanoBackdrop.bake(false, weather)
    private val nightBackdrop = OceanoBackdrop.bake(true, weather)
    private val dayPalette = OceanoPalette.day()
    private val nightPalette = OceanoPalette.night()
    private val out = PixelBlitter(frame, dayPalette, OceanoPalette.jelly())

    private val far: Array<Swimmer>
    private val mid: Array<Swimmer>
    private val daySchools: Array<Swimmer>
    private val crossing: Array<Swimmer>
    private val close: Array<Swimmer>
    private val turtle: Swimmer

    private val snowX = FloatArray(SNOW)
    private val snowY = FloatArray(SNOW)
    private val snowVx = FloatArray(SNOW)
    private val snowVy = FloatArray(SNOW)
    private val bubbleX = intArrayOf(34, 70, 90, 118, 150, 204, 232, 262, 284, 300, 20, 310)
    private val bubbleSpeed = FloatArray(bubbleX.size)
    private val bubbleOffset = FloatArray(bubbleX.size)

    init {
        val rnd = Random(5)
        fun between(a: Float, b: Float) = a + rnd.nextFloat() * (b - a)
        val tau = (PI * 2).toFloat()

        far = Array(12 + 10 + 2) { i ->
            when {
                i < 12 -> Swimmer.hover(OceanoSprites.FAR_SNAPPER, between(150f, 290f), between(66f, 106f), 4.5f, 1.4f, between(5.5f, 8.1f), between(0f, 5.5f), i)
                i < 22 -> Swimmer.orbit(OceanoSprites.FAR_SILVER, 183f, 78f, 100f, 15f, 23f, tau * (i - 12) / 10f, -1, i)
                i == 22 -> Swimmer.cross(arrayOf(OceanoSprites.SHARK), 55f, 120f, 52f, 1)
                else -> Swimmer.cross(OceanoSprites.MANTA, 33f, 150f, 24f, -1)
            }
        }
        mid = Array(14 + 10) { i ->
            if (i < 14) {
                val left = i % 3 == 0
                Swimmer.hover(
                    OceanoSprites.ANTHIAS_SMALL,
                    if (left) between(25f, 108f) else between(190f, 310f),
                    if (left) between(53f, 113f) else between(32f, 93f),
                    between(2.5f, 5f), 2f, between(4f, 8f), between(0f, 8f), i,
                )
            } else {
                Swimmer.orbit(OceanoSprites.ANTHIAS_SMALL, 260f, 63f, 50f, 25f, 29f, tau * (i - 14) / 10f, 1, i)
            }
        }
        daySchools = Array(16 + 8) { i ->
            if (i < 16) {
                Swimmer.orbit(OceanoSprites.SILVERSIDE, 160f, 50f, 135f, 28f, 32f, tau * i / 16f, -1, i)
            } else {
                Swimmer.orbit(OceanoSprites.YELLOWTAIL, 63f, 93f, 50f, 20f, 37f, tau * (i - 16) / 8f, 1, i)
            }
        }
        val serX = floatArrayOf(0f, 10f, 20f, 31f, 3f, 12f, 20f)
        val serY = floatArrayOf(31f, 31f, 31f, 31f, 38f, 38f, 37f)
        crossing = Array(7 + 3) { i ->
            when {
                i < 7 -> Swimmer.cross(OceanoSprites.SERGEANT, serY[i], 64f, 10f, 1, xOff = serX[i], bobAmp = 1f, bobPeriod = 1.7f + i * 0.1f, offset = i)
                i == 7 -> Swimmer.cross(OceanoSprites.ANGELFISH, 91f, 88f, 30f, -1, bobAmp = 1f, bobPeriod = 1.5f)
                i == 8 -> Swimmer.cross(OceanoSprites.ANGELFISH, 109f, 88f, 62f, -1, bobAmp = 1f, bobPeriod = 1.9f)
                else -> Swimmer.cross(OceanoSprites.CROSS_SNAPPER, 78f, 110f, 40f, 1, bobAmp = 1f, bobPeriod = 1.5f)
            }
        }
        close = Array(10 + 8) { i ->
            if (i < 10) {
                Swimmer.hover(OceanoSprites.ANTHIAS, between(183f, 308f), between(32f, 100f), between(3f, 6f), 2.5f, between(4f, 8f), between(0f, 8f), i)
            } else {
                Swimmer.orbit(OceanoSprites.ANTHIAS, 117f, 120f, 46f, 12f, 22f, tau * (i - 10) / 8f, 1, i)
            }
        }
        turtle = Swimmer.cross(OceanoSprites.TURTLE, 40f, 95f, 28f, 1, bobAmp = 3f, bobPeriod = 6f)
        for (i in 0 until SNOW) {
            snowX[i] = rnd.nextFloat() * OceanoPixels.W
            snowY[i] = rnd.nextFloat() * OceanoPixels.H * 0.9f
            snowVx[i] = between(0.8f, 2f)
            snowVy[i] = between(1f, 2.5f)
        }
        for (i in bubbleX.indices) {
            bubbleSpeed[i] = between(9f, 16f)
            bubbleOffset[i] = rnd.nextFloat() * 130f
        }
    }

    /** Paints one frame of [inp] into [frame]. */
    fun render(inp: OceanoSceneInput) {
        val w = OceanoPixels.W
        val h = OceanoPixels.H
        val tick = inp.tick
        val t = tick * OceanoLayout.TICK_MS / 1000f
        val nightNow = inp.night >= 0.5f
        nightBoost = inp.night
        val backdrop = if (nightNow) nightBackdrop else dayBackdrop
        out.palette = if (nightNow) nightPalette else dayPalette

        // Water, rays, far reef and sand: a straight copy, or an ordered-dither mix while the phase changes.
        when {
            inp.night <= 0f -> System.arraycopy(dayBackdrop.back, 0, frame, 0, frame.size)
            inp.night >= 1f -> System.arraycopy(nightBackdrop.back, 0, frame, 0, frame.size)
            else -> {
                val th = (inp.night * 16f).toInt()
                val d = dayBackdrop.back
                val n = nightBackdrop.back
                for (y in 0 until h) for (x in 0 until w) {
                    val i = y * w + x
                    frame[i] = if (OceanoPixels.bayer(x, y) < th) n[i] else d[i]
                }
            }
        }

        // Caustics: the lit pixels walk across their ring of eight steps.
        val cAt = backdrop.causticAt
        val cPhase = backdrop.causticPhase
        val cLit = backdrop.causticLit
        val step = tick / 3
        for (i in cAt.indices) {
            if (((step + cPhase[i]) and 7) < LIT_STEPS) frame[cAt[i]] = cLit[i]
        }

        drawSnow(t, tick, nightNow)

        val dayFade = ((1f - inp.night) * 16f).roundToInt()
        val reefFade = 16 - (inp.night * 8f).roundToInt()
        for (s in far) { s.update(tick, t); s.draw(out, reefFade) }
        for (s in mid) { s.update(tick, t); s.draw(out, reefFade) }
        for (s in daySchools) { s.update(tick, t); s.draw(out, dayFade) }
        for (s in crossing) { s.update(tick, t); s.draw(out) }
        turtle.update(tick, t)
        turtle.drawWithFrame(out, tick)

        val fAt = backdrop.foreAt
        val fColor = backdrop.foreColor
        for (i in fAt.indices) frame[fAt[i]] = fColor[i]

        for (s in close) { s.update(tick, t); s.draw(out, reefFade) }
        drawBubbles(t)
        drawGlow(backdrop, tick)
        drawSurface(t)
        drawJellies(inp, t, tick)
        drawScrubber(inp)
    }

    private fun drawSnow(t: Float, tick: Int, night: Boolean) {
        val w = OceanoPixels.W
        val h = OceanoPixels.H
        val white = out.palette[Px.WHITE]
        for (i in 0 until SNOW) {
            val x = ((snowX[i] + snowVx[i] * t) % w).toInt()
            val y = ((snowY[i] + snowVy[i] * t) % h).toInt()
            if (night && i % 3 == 0) {
                val twinkle = ((tick / 4 + i) and 3) != 0
                if (twinkle) out.dot(x, y, if (i % 2 == 0) Px.GLOW_A else Px.GLOW_B)
            } else {
                out.blend(x, y, white, if (night) 56 else 96)
            }
        }
    }

    private fun drawBubbles(t: Float) {
        for (i in bubbleX.indices) {
            val rise = (t * bubbleSpeed[i] + bubbleOffset[i]) % 130f
            val y = (152f - rise).roundToInt()
            val x = bubbleX[i] + (sin(t * 2f + i) * 1.4f).roundToInt()
            val s = OceanoSprites.BUBBLES[i % OceanoSprites.BUBBLES.size]
            out.blit(s, x, y)
        }
    }

    private fun drawGlow(backdrop: OceanoBackdrop, tick: Int) {
        val at = backdrop.glowAt
        val color = backdrop.glowColor
        for (i in at.indices) {
            if (((tick / 5 + i) and 3) == 0) continue
            frame[at[i]] = color[i]
            val p = at[i]
            if (p > OceanoPixels.W) out.blend(p % OceanoPixels.W, p / OceanoPixels.W - 1, color[i], 90)
        }
    }

    /** The wave line at the top, and rain rings on it when the weather has any. */
    private fun drawSurface(t: Float) {
        val w = OceanoPixels.W
        val hi = out.palette[Px.HI]
        val chop = weather.chop
        val speed = 0.9f + chop * 0.6f
        for (x in 0 until w) {
            val ys = 2 + (chop * sin(x * 0.19f + t * speed)).roundToInt()
            for (y in 0 until ys) out.dotArgb(x, y, hi)
            out.blend(x, ys, hi, 110)
        }
        for (i in 0 until weather.ripples) {
            val ph = t / 1.2f + i * 0.37f
            val k = floor(ph)
            val u = ph - k
            val seed = (k.toInt() * 31 + i * 97) and 0x7fffffff
            val rx = (seed * 1103515245L + 12345L).let { ((it shr 8) and 0xffff).toInt() } % (w - 20) + 10
            val ry = 6 + (seed % 7)
            val s = OceanoSprites.RIPPLES[(u * 3f).toInt().coerceIn(0, 2)]
            out.blit(s, rx - s.w / 2, ry - s.h / 2)
        }
    }

    private val seq = intArrayOf(0, 1, 2, 3, 2, 1)

    private fun drawJellies(inp: OceanoSceneInput, t: Float, tick: Int) {
        if (inp.count <= 0) return
        val center = inp.scroll.roundToInt()
        for (d in OceanoLayout.VISIBLE_RADIUS downTo 0) {
            val left = center - d
            if (left in 0 until inp.count) drawJelly(inp, left, t, tick)
            if (d > 0 && center + d < inp.count) drawJelly(inp, center + d, t, tick)
        }
    }

    private fun drawJelly(inp: OceanoSceneInput, i: Int, t: Float, tick: Int) {
        val cx = OceanoLayout.jellyX(i, inp.scroll)
        if (cx < -24 || cx > OceanoPixels.W + 24) return
        val focus = OceanoLayout.focusOf(i, inp.scroll)
        val si = OceanoLayout.sizeIndex(focus)
        val size = OceanoSprites.JELLY_SIZES[si]
        val tint = OceanoPalette.jellyTintIndex(i)
        val period = (if (si == 2) 2.4f else 2.8f) + (i % 3) * 0.4f
        val u = ((t + i * 0.6f) / period).let { it - floor(it) }
        val bellFrame = seq[(u * seq.size).toInt().coerceIn(0, seq.size - 1)]
        val bob = (2f * sin(2f * PI.toFloat() * (t + i * 0.7f) / (3.4f + (i % 4) * 0.5f))).roundToInt()
        val top = OceanoLayout.bellTop(si) + bob
        val x0 = cx - size.canvasW / 2
        val fade = (((minOf(cx, OceanoPixels.W - 1 - cx)) - 4) * 16 / 30).coerceIn(0, 16)
        val bh = OceanoSprites.bellRows(si, bellFrame)

        drawHalo(cx, top + size.bellH / 2, si, focus, tint, fade)
        out.blit(OceanoSprites.TENTACLES[si][(tick / 4 + i) and 3], x0, top + size.bellH + 1, tint = tint, fade = fade)
        out.blit(OceanoSprites.BELLS[si][bellFrame], x0, top, tint = tint, fade = fade)

        val bellMidY = top + size.bellH + 1 - bh / 2
        val icon = inp.icon
        if (si == 2 && i == inp.focused && icon != null) {
            val ix = cx - icon.w / 2
            val iy = bellMidY - icon.h / 2
            for (yy in -1..icon.h) for (xx in -1..icon.w) {
                val edge = yy < 0 || yy == icon.h || xx < 0 || xx == icon.w
                if (fade < 16 && OceanoPixels.bayer(ix + xx, iy + yy) >= fade) continue
                if (edge) out.dot(ix + xx, iy + yy, Px.INK) else out.dotArgb(ix + xx, iy + yy, icon.argb[yy * icon.w + xx])
            }
        } else if (fade > 8 && i < inp.initials.size) {
            val scale = if (si == 2) 2 else 1
            val gx = cx - OceanoSprites.GLYPH_W * scale / 2
            val gy = bellMidY - OceanoSprites.GLYPH_H * scale / 2 + 1
            val g = inp.initials[i]
            drawGlyphShadowed(g, gx, gy, scale, tint)
        }
    }

    private fun drawGlyphShadowed(g: Int, x: Int, y: Int, scale: Int, tint: Int) {
        val shadow = out.jelly[tint * 3]
        // shadow pass in the jelly's dark color, then white on top
        for (r in 0 until OceanoSprites.GLYPH_H) for (q in 0 until OceanoSprites.GLYPH_W) {
            if (!OceanoSprites.glyphBit(g, r, q)) continue
            for (sy in 0 until scale) for (sx in 0 until scale) out.dotArgb(x + q * scale + sx + 1, y + r * scale + sy + 1, shadow)
        }
        OceanoSprites.drawGlyph(out, g, x, y, scale, Px.WHITE)
    }

    /** Dithered glow around the bell in the jelly's color: three discs, thinning outward. */
    private fun drawHalo(cx: Int, cy: Int, si: Int, focus: Float, tint: Int, fade: Int) {
        if (fade <= 0) return
        val radius = intArrayOf(14, 20, 28)[si]
        val argb = out.jelly[tint * 3 + 1]
        val a = (46 + 64 * focus + 20 * nightBoost).toInt()
        val r2 = radius * radius
        for (dy in -radius..radius) for (dx in -radius..radius) {
            val d2 = dx * dx + dy * dy
            if (d2 > r2) continue
            val x = cx + dx
            val y = cy + dy
            val inner = d2 * 4 <= r2
            val mid = d2 * 16 <= r2 * 9
            val on = when {
                inner -> true
                mid -> ((x + y) and 1) == 0
                else -> (x and 1) == 0 && (y and 1) == 0
            }
            if (!on) continue
            if (fade < 16 && OceanoPixels.bayer(x, y) >= fade) continue
            out.blend(x, y, argb, if (inner) a * 2 / 3 else a)
        }
    }

    private var nightBoost = 0f

    /** Line of ticks along the bottom, one per app, with a lit one that follows the focus. */
    private fun drawScrubber(inp: OceanoSceneInput) {
        val n = inp.count
        if (n <= 0) return
        val y = SCRUB_Y
        val x0 = SCRUB_X0
        val x1 = SCRUB_X1
        for (yy in y - 3..y + 3) for (xx in x0 - 8..x1 + 8) {
            if (((xx + yy) and 1) == 0) out.blend(xx, yy, out.palette[Px.INK], 110)
        }
        for (x in x0..x1) if ((x and 1) == 0) out.dotArgb(x, y, OceanoPalette.ScrubTick)
        val span = (x1 - x0).toFloat()
        var tickStep = 1
        while (n > 1 && span / (n - 1) * tickStep < 3f) tickStep++
        var i = 0
        while (i < n) {
            val x = if (n > 1) x0 + (i * span / (n - 1)).roundToInt() else (x0 + x1) / 2
            out.dotArgb(x, y - 1, OceanoPalette.ScrubTick)
            out.dotArgb(x, y, OceanoPalette.ScrubTick)
            out.dotArgb(x, y + 1, OceanoPalette.ScrubTick)
            i += tickStep
        }
        val pos = inp.scroll.coerceIn(0f, (n - 1).coerceAtLeast(0).toFloat())
        val mx = if (n > 1) x0 + (pos * span / (n - 1)).roundToInt() else (x0 + x1) / 2
        val tint = out.jelly[OceanoPalette.jellyTintIndex(inp.focused) * 3 + 1]
        for (dy in -3..3) for (dx in -4..4) {
            val d = abs(dx) + abs(dy)
            if (d in 3..4) out.blend(mx + dx, y + dy, tint, 110)
        }
        for (dy in -2..2) for (dx in -2..2) if (abs(dx) + abs(dy) <= 2) out.dotArgb(mx + dx, y + dy, OceanoPalette.ScrubTickOn)
    }

    private companion object {
        const val SNOW = 44
        const val LIT_STEPS = 3
        const val SCRUB_Y = 171
        const val SCRUB_X0 = 94
        const val SCRUB_X1 = 226
    }
}
