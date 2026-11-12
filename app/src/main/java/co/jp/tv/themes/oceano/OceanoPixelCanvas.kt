package co.jp.tv.themes.oceano

import kotlin.math.abs
import kotlin.math.max

/** The scene's virtual resolution: 320x180 pixels, shown at an exact 4x on a 1280x720 screen. */
internal object OceanoPixels {
    const val W = 320
    const val H = 180

    /** 4x4 ordered-dither thresholds, 0..15. */
    private val bayer = intArrayOf(
        0, 8, 2, 10,
        12, 4, 14, 6,
        3, 11, 1, 9,
        15, 7, 13, 5,
    )

    fun bayer(x: Int, y: Int): Int = bayer[((y and 3) shl 2) or (x and 3)]
}

/** A palette-indexed pixel surface with the few shapes the sprites and the backdrop need. */
internal class PixelCanvas(val w: Int, val h: Int) {
    val px = ByteArray(w * h)

    fun get(x: Int, y: Int): Int = if (x in 0 until w && y in 0 until h) px[y * w + x].toInt() else 0

    fun set(x: Int, y: Int, c: Int) {
        if (x in 0 until w && y in 0 until h) px[y * w + x] = c.toByte()
    }

    /** Sets the pixel only where it is still transparent. */
    fun setBehind(x: Int, y: Int, c: Int) {
        if (get(x, y) == 0) set(x, y, c)
    }

    fun rect(x0: Int, y0: Int, x1: Int, y1: Int, c: Int) {
        for (y in y0..y1) for (x in x0..x1) set(x, y, c)
    }

    /** Filled ellipse centred on (cx, cy) with radii rx, ry. */
    fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float, c: Int) {
        val x0 = (cx - rx).toInt() - 1
        val x1 = (cx + rx).toInt() + 1
        val y0 = (cy - ry).toInt() - 1
        val y1 = (cy + ry).toInt() + 1
        for (y in y0..y1) for (x in x0..x1) {
            val dx = (x + 0.5f - cx) / rx
            val dy = (y + 0.5f - cy) / ry
            if (dx * dx + dy * dy <= 1f) set(x, y, c)
        }
    }

    /** Bresenham line, [thick] pixels wide horizontally. */
    fun line(x0: Int, y0: Int, x1: Int, y1: Int, c: Int, thick: Int = 1) {
        var x = x0
        var y = y0
        val dx = abs(x1 - x0)
        val dy = -abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx + dy
        while (true) {
            for (t in 0 until thick) set(x + t, y, c)
            if (x == x1 && y == y1) break
            val e2 = 2 * err
            if (e2 >= dy) { err += dy; x += sx }
            if (e2 <= dx) { err += dx; y += sy }
        }
    }

    /** Dark ring on the sprite: every filled pixel with an empty 4-neighbour becomes [c]. */
    fun outline(c: Int) {
        val copy = px.copyOf()
        for (y in 0 until h) for (x in 0 until w) {
            if (copy[y * w + x].toInt() == 0) continue
            val edge = x == 0 || y == 0 || x == w - 1 || y == h - 1 ||
                copy[y * w + x - 1].toInt() == 0 || copy[y * w + x + 1].toInt() == 0 ||
                copy[(y - 1) * w + x].toInt() == 0 || copy[(y + 1) * w + x].toInt() == 0
            if (edge) px[y * w + x] = c.toByte()
        }
    }

    fun toSprite(): Sprite = Sprite(w, h, px)
}

/** A finished palette-indexed image; 0 is transparent. */
internal class Sprite(val w: Int, val h: Int, val px: ByteArray) {
    companion object {
        /** Builds a sprite from ASCII rows: '.' is clear, the rest map through [OceanoSprites.LEGEND]. */
        fun parse(vararg rows: String): Sprite {
            val w = rows.maxOf { it.length }
            val out = ByteArray(w * rows.size)
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, ch -> out[y * w + x] = OceanoSprites.legend(ch).toByte() }
            }
            return Sprite(w, rows.size, out)
        }
    }
}

/**
 * Writes sprites and shapes into the frame without allocating: [frame] is the ARGB buffer of one frame,
 * [palette] maps scene indices, [jelly] holds three ARGB colors per jelly tint.
 */
internal class PixelBlitter(val frame: IntArray, var palette: IntArray, var jelly: IntArray) {
    /**
     * Draws [s] with its top-left at (x, y). [flip] mirrors it horizontally. [tint] picks the jelly ramp for
     * indices 25..27. [fade] in 0..16 keeps only that many sixteenths of the pixels (ordered dither), 16 = all.
     */
    fun blit(s: Sprite, x: Int, y: Int, flip: Boolean = false, tint: Int = 0, fade: Int = 16) {
        if (fade <= 0) return
        val w = OceanoPixels.W
        val h = OceanoPixels.H
        val y0 = max(0, -y)
        val y1 = minOf(s.h, h - y)
        val x0 = max(0, -x)
        val x1 = minOf(s.w, w - x)
        if (x1 <= x0 || y1 <= y0) return
        val ramp = tint * 3
        for (sy in y0 until y1) {
            val row = (y + sy) * w + x
            for (sx in x0 until x1) {
                val src = s.px[sy * s.w + if (flip) s.w - 1 - sx else sx].toInt()
                if (src == 0) continue
                if (fade < 16 && OceanoPixels.bayer(x + sx, y + sy) >= fade) continue
                frame[row + sx] = if (src >= Px.JELLY_FIRST) jelly[ramp + src - Px.JELLY_FIRST] else palette[src]
            }
        }
    }

    /** One pixel of palette color [c]. */
    fun dot(x: Int, y: Int, c: Int) {
        if (x in 0 until OceanoPixels.W && y in 0 until OceanoPixels.H) frame[y * OceanoPixels.W + x] = palette[c]
    }

    /** One pixel of raw ARGB. */
    fun dotArgb(x: Int, y: Int, argb: Int) {
        if (x in 0 until OceanoPixels.W && y in 0 until OceanoPixels.H) frame[y * OceanoPixels.W + x] = argb
    }

    /** Blends [argb] over the pixel by [alpha256] (0..256). */
    fun blend(x: Int, y: Int, argb: Int, alpha256: Int) {
        if (x !in 0 until OceanoPixels.W || y !in 0 until OceanoPixels.H) return
        val i = y * OceanoPixels.W + x
        val d = frame[i]
        val inv = 256 - alpha256
        val r = (((d shr 16) and 255) * inv + ((argb shr 16) and 255) * alpha256) shr 8
        val g = (((d shr 8) and 255) * inv + ((argb shr 8) and 255) * alpha256) shr 8
        val b = ((d and 255) * inv + (argb and 255) * alpha256) shr 8
        frame[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }
}
