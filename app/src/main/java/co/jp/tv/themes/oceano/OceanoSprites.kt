package co.jp.tv.themes.oceano

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** One size of jelly: bell width and height in virtual pixels, and the tentacle length under it. */
internal class JellySize(val bellW: Int, val bellH: Int, val tentacles: Int) {
    /** Width of every sprite of this size, so bell and tentacles line up on the same centre column. */
    val canvasW: Int get() = bellW + 8
}

/** Every sprite of the scene, built once from code: nothing here runs per frame. All face right. */
internal object OceanoSprites {
    /** Idle, mid-way and focused jelly. A jelly steps through these as the focus reaches it. */
    val JELLY_SIZES = arrayOf(JellySize(18, 13, 24), JellySize(24, 17, 34), JellySize(30, 22, 44))

    const val BELL_FRAMES = 4
    const val TENTACLE_FRAMES = 4

    fun legend(ch: Char): Int = when (ch) {
        '.' -> Px.CLEAR
        'h' -> Px.HI
        'c' -> Px.W2
        'e' -> Px.W3
        'd' -> Px.W4
        'f' -> Px.FAR
        'N' -> Px.SAND_LIGHT
        'n' -> Px.SAND_MID
        'm' -> Px.SAND_DARK
        'x' -> Px.ROCK_MID
        'v' -> Px.PURPLE
        'o' -> Px.ORANGE
        'r' -> Px.RED
        'g' -> Px.GREEN
        'G' -> Px.GREEN_LIGHT
        'y' -> Px.GOLD
        'w' -> Px.WHITE
        'k' -> Px.INK
        'u' -> Px.SHELL
        'b' -> Px.BLUE
        's' -> Px.SILVER
        'p' -> Px.PINK
        else -> error("Unknown sprite char '$ch'")
    }

    // Reef fish, two frames each (tail spread, tail folded).
    val ANTHIAS_SMALL = arrayOf(
        Sprite.parse(
            "o.oo..",
            "oooook",
            "o.yyo.",
        ),
        Sprite.parse(
            "..oo..",
            ".ooook",
            "..yyo.",
        ),
    )
    val ANTHIAS = arrayOf(
        Sprite.parse(
            "o..oo...",
            "oooooook",
            "oooyyyoo",
            "o..yy...",
        ),
        Sprite.parse(
            "...oo...",
            ".ooooook",
            "oooyyyoo",
            "...yy...",
        ),
    )
    val SILVERSIDE = arrayOf(
        Sprite.parse(
            "s.sss..",
            "ssssswk",
            "..www..",
        ),
        Sprite.parse(
            "..sss..",
            ".sssswk",
            "s.www..",
        ),
    )
    val YELLOWTAIL = arrayOf(
        Sprite.parse(
            "y..yyy...",
            "yyyyyyyyk",
            "y.yywwyy.",
            "...yy....",
        ),
        Sprite.parse(
            "...yyy...",
            ".yyyyyyyk",
            "y.yywwyy.",
            "y..yy....",
        ),
    )
    val SERGEANT = arrayOf(
        Sprite.parse(
            "y.kyk..",
            "ykwykyk",
            "y.kwk..",
        ),
        Sprite.parse(
            "..kyk..",
            ".ykwkyk",
            "y.kwk..",
        ),
    )
    val ANGELFISH = arrayOf(
        Sprite.parse(
            "....bb.....",
            "...bbbb....",
            "y.bbbbbby..",
            "yybbbbbbyk.",
            "y.bbbbbby..",
            "...bbbb....",
            "....bb.....",
        ),
        Sprite.parse(
            "....bb.....",
            "...bbbb....",
            ".ybbbbbby..",
            "yybbbbbbyk.",
            ".ybbbbbby..",
            "...bbbb....",
            "....bb.....",
        ),
    )

    // Far silhouettes, in the water's own deep colors.
    val FAR_SNAPPER = arrayOf(
        Sprite.parse(
            "e.ee.",
            "eeeee",
        ),
        Sprite.parse(
            "..ee.",
            "eeeee",
        ),
    )
    val FAR_SILVER = arrayOf(
        Sprite.parse(
            "c.cc",
            ".ccc",
        ),
        Sprite.parse(
            "..cc",
            "ccc.",
        ),
    )
    val CROSS_SNAPPER: Array<Sprite> = Array(2) { f ->
        val c = PixelCanvas(20, 8)
        c.ellipse(11f, 4f, 7.5f, 3f, Px.W2)
        c.rect(0, if (f == 0) 0 else 1, 3, if (f == 0) 1 else 2, Px.W2)
        c.rect(0, if (f == 0) 6 else 5, 3, 7, Px.W2)
        c.line(3, 4, 6, 4, Px.W2)
        c.set(16, 3, Px.INK)
        c.toSprite()
    }

    val SHARK: Sprite = PixelCanvas(44, 14).also { c ->
        c.ellipse(24f, 8f, 15f, 3.6f, Px.W3)
        c.line(38, 8, 43, 5, Px.W3, 2)
        c.line(38, 8, 43, 11, Px.W3, 2)
        c.line(18, 4, 24, 0, Px.W3, 3)
        c.rect(19, 3, 24, 4, Px.W3)
        c.line(22, 10, 18, 13, Px.W3, 2)
    }.toSprite()

    val MANTA: Array<Sprite> = Array(2) { f ->
        val c = PixelCanvas(34, 15)
        val tip = if (f == 0) 1 else 5
        c.ellipse(15f, 7.5f, 5f, 4f, Px.W3)
        c.line(15, 6, 3, tip, Px.W3, 2)
        c.line(15, 9, 3, 14 - tip, Px.W3, 2)
        c.line(15, 5, 22, 3, Px.W3, 2)
        c.line(15, 10, 22, 12, Px.W3, 2)
        c.line(20, 7, 33, 7, Px.W3)
        c.toSprite()
    }

    /** Sea turtle from above, flippers in four strokes. Head to the right. */
    val TURTLE: Array<Sprite> = Array(4) { f ->
        val c = PixelCanvas(52, 32)
        val a = doubleArrayOf(115.0, 140.0, 165.0, 140.0)[f] * PI / 180.0
        val ra = doubleArrayOf(150.0, 165.0, 180.0, 165.0)[f] * PI / 180.0
        // front flippers
        for (side in intArrayOf(-1, 1)) {
            val fx = 31
            val fy = 16 + side * 6
            c.line(fx, fy, fx + (cos(a) * 15).roundToInt(), fy + side * (sin(a) * 15).roundToInt(), Px.SAND_MID, 3)
            val rx = 12
            val ry = 16 + side * 5
            c.line(rx, ry, rx + (cos(ra) * 8).roundToInt(), ry + side * (sin(ra) * 8).roundToInt(), Px.SAND_MID, 2)
        }
        // tail, head, then shell on top
        c.line(7, 16, 3, 16, Px.SAND_MID, 2)
        c.ellipse(41f, 16f, 5f, 3.2f, Px.SAND_MID)
        c.set(43, 15, Px.INK)
        c.set(44, 15, Px.INK)
        c.ellipse(22f, 16f, 13f, 10f, Px.INK)
        c.ellipse(22f, 16f, 12f, 9f, Px.SHELL)
        // scutes: a spine, two rows of plates
        c.line(14, 16, 30, 16, Px.ROCK_MID)
        for (x in intArrayOf(17, 22, 27)) {
            c.line(x, 9, x - 1, 23, Px.ROCK_MID)
        }
        c.line(13, 12, 31, 12, Px.ROCK_MID)
        c.line(13, 20, 31, 20, Px.ROCK_MID)
        c.ellipse(17f, 12f, 4f, 2f, Px.GOLD)
        c.set(18, 11, Px.SAND_LIGHT)
        c.toSprite()
    }

    val BUBBLES: Array<Sprite> = arrayOf(
        Sprite.parse("hh", "hh"),
        Sprite.parse(".h.", "h.h", ".h."),
        Sprite.parse(".hh.", "h..h", "h..h", ".hh."),
    )

    /** Rain rings spreading on the surface, three frames. */
    val RIPPLES: Array<Sprite> = Array(3) { f ->
        val rx = 3f + f * 3f
        val ry = 1f + f * 0.8f
        val c = PixelCanvas(15, 5)
        val cx = 7.5f
        val cy = 2.5f
        for (y in 0 until 5) for (x in 0 until 15) {
            val d = ((x + 0.5f - cx) / rx).let { it * it } + ((y + 0.5f - cy) / ry).let { it * it }
            if (d in 0.55f..1.15f) c.set(x, y, Px.HI)
        }
        c.toSprite()
    }

    private fun bellHeight(size: JellySize, frame: Int): Int = (size.bellH * floatArrayOf(1f, 0.93f, 0.86f, 0.93f)[frame]).roundToInt()

    /** Height of the bell drawn for [frame]; the bell is bottom-anchored in its sprite. */
    fun bellRows(sizeIndex: Int, frame: Int): Int = bellHeight(JELLY_SIZES[sizeIndex], frame)

    /** The pulsing bell: a dome with a dark outline, a light rim and four white spots. */
    val BELLS: Array<Array<Sprite>> = Array(JELLY_SIZES.size) { s ->
        val size = JELLY_SIZES[s]
        Array(BELL_FRAMES) { f -> buildBell(size, f) }
    }

    private fun buildBell(size: JellySize, frame: Int): Sprite {
        val c = PixelCanvas(size.canvasW, size.bellH + 1)
        val sx = floatArrayOf(1f, 1.05f, 1.1f, 1.05f)[frame]
        val bw = (size.bellW * sx).roundToInt().let { if (it % 2 == 1) it + 1 else it }
        val bh = bellHeight(size, frame)
        val hd = max(3, (bh * 0.78f).roundToInt())
        val cx = size.canvasW / 2f
        val rx = bw / 2f
        val top = size.bellH + 1 - bh
        for (y in 0 until bh) {
            val hw = if (y < hd) {
                val k = (hd - y - 0.5f) / hd
                rx * sqrt(1f - k * k)
            } else {
                rx * (1f - 0.05f * (y - hd + 1))
            }
            for (x in 0 until size.canvasW) {
                if (kotlin.math.abs(x + 0.5f - cx) <= hw) c.set(x, top + y, Px.JELLY_BASE)
            }
        }
        // highlight arc, then the rim, then the outline over both
        for (y in 0 until bh) for (x in 0 until size.canvasW) {
            val dx = (x + 0.5f - (cx - rx * 0.28f)) / (rx * 0.38f)
            val dy = (top + y + 0.5f - (top + hd * 0.38f)) / (hd * 0.22f)
            if (c.get(x, top + y) == Px.JELLY_BASE && dx * dx + dy * dy <= 1f) c.set(x, top + y, Px.JELLY_LIGHT)
        }
        for (x in 0 until size.canvasW) if (c.get(x, size.bellH - 1) == Px.JELLY_BASE) c.set(x, size.bellH - 1, Px.JELLY_LIGHT)
        c.outline(Px.JELLY_DARK)
        val spot = max(2, (rx / 3f).roundToInt())
        val spotY = top + (hd * 0.72f).roundToInt()
        val mid = size.canvasW / 2
        for (x in intArrayOf(mid - 1, mid, mid - 1 - spot, mid + spot)) c.set(x, spotY, Px.WHITE)
        return c.toSprite()
    }

    /** Thin tentacles and three oral arms, hanging from the bell's bottom row. */
    val TENTACLES: Array<Array<Sprite>> = Array(JELLY_SIZES.size) { s ->
        Array(TENTACLE_FRAMES) { f -> buildTentacles(JELLY_SIZES[s], s, f) }
    }

    private fun buildTentacles(size: JellySize, sizeIndex: Int, phase: Int): Sprite {
        val c = PixelCanvas(size.canvasW, size.tentacles)
        val mid = size.canvasW / 2
        val n = 4 + sizeIndex
        val span = size.bellW * 0.7f
        val len = size.tentacles
        val turn = phase * (PI / 2.0)
        for (i in 0 until n) {
            val x0 = (mid - span / 2f + i * span / (n - 1)).roundToInt()
            for (r in 0 until len) {
                if (r > len * 0.62f && (r + i) % 2 != 0) continue
                val amp = minOf(1.9f, r * 0.12f)
                val dx = (amp * sin(turn + r * 0.5 + i * 1.7)).roundToInt()
                c.set(x0 + dx, r, if (r % 4 < 2) Px.JELLY_BASE else Px.JELLY_LIGHT)
            }
        }
        val arms = (len * 0.55f).roundToInt()
        for ((k, x0) in intArrayOf(mid - 4, mid - 1, mid + 2).withIndex()) {
            for (r in 0 until arms) {
                val amp = minOf(1.2f, r * 0.08f)
                val dx = (amp * sin(turn * 0.8 + r * 0.4 + k * 2.1)).roundToInt()
                val color = if (r % 3 == 0) Px.WHITE else Px.JELLY_LIGHT
                c.set(x0 + dx, r, color)
                c.set(x0 + dx + 1, r, color)
            }
        }
        return c.toSprite()
    }

    // 3x5 glyphs for the letter on each jelly: five rows of three bits, '#' set.
    private const val GLYPH_ROWS = 5
    private const val GLYPHS_AZ = "010101111101101 110101110101110 011100100100011 110101101101110 111100110100111 " +
        "111100110100100 011100101101011 101101111101101 111010010010111 001001001101010 101101110101101 " +
        "100100100100111 101111111101101 110101101101101 010101101101010 110101110100100 010101101111011 " +
        "110101110101101 011100010001110 111010010010010 101101101101111 101101101101010 101101111111101 " +
        "101101010101101 101101010010010 111001010100111"
    private const val GLYPHS_09 = "111101101101111 010110010010111 110001010100111 110001010001110 101101111001001 " +
        "111100110001110 011100111101111 111001010010010 111101111101111 111101111001110"

    private val glyphBits: Array<IntArray> = run {
        val all = (GLYPHS_AZ.split(' ') + GLYPHS_09.split(' ')).filter { it.isNotEmpty() }
        Array(all.size) { g -> IntArray(15) { if (all[g][it] == '1') 1 else 0 } }
    }

    /** Index into the glyph table for [ch]: A-Z, 0-9, accents folded onto their letter, anything else '?' as a dot-less block. */
    fun glyphIndex(ch: Char): Int {
        val base = java.text.Normalizer.normalize(ch.toString(), java.text.Normalizer.Form.NFD).firstOrNull()?.uppercaseChar() ?: ch
        return when (base) {
            in 'A'..'Z' -> base - 'A'
            in '0'..'9' -> 26 + (base - '0')
            else -> 14
        }
    }

    fun glyphBit(g: Int, row: Int, col: Int): Boolean = glyphBits[g][row * 3 + col] == 1

    const val GLYPH_W = 3
    const val GLYPH_H = 5

    /** Draws glyph [g] with its top-left at (x, y), [scale] pixels per bit, in palette color [c]. */
    fun drawGlyph(out: PixelBlitter, g: Int, x: Int, y: Int, scale: Int, c: Int) {
        val bits = glyphBits[g]
        for (r in 0 until GLYPH_H) for (q in 0 until GLYPH_W) {
            if (bits[r * GLYPH_W + q] == 0) continue
            for (sy in 0 until scale) for (sx in 0 until scale) out.dot(x + q * scale + sx, y + r * scale + sy, c)
        }
    }
}
