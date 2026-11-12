package co.jp.tv.themes.oceano

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * One animal on a fixed path, placed from the scene time alone (no state, no allocation): it crosses
 * the screen, hovers around a point, or circles a school's centre. Sprites face right; [flip] mirrors
 * them when the animal swims left. The tail flaps between the sprite's frames five times a second.
 */
internal class Swimmer private constructor(
    private val frames: Array<Sprite>,
    private val kind: Int,
    private val x0: Float,
    private val y0: Float,
    private val rx: Float,
    private val ry: Float,
    private val period: Float,
    private val delay: Float,
    private val dir: Int,
    private val bobAmp: Float,
    private val bobPeriod: Float,
    private val frameStep: Int,
    private val frameOffset: Int,
) {
    private var x = 0f
    private var y = 0f
    private var flip = false

    /** Moves the swimmer to where it is at tick [tick] (scene seconds [t]). */
    fun update(tick: Int, t: Float) {
        when (kind) {
            CROSS -> {
                val u = loop((t + delay) / period)
                val span = OceanoPixels.W + 2f * frames[0].w
                val travel = u * span
                x = if (dir > 0) -frames[0].w + travel + x0 else OceanoPixels.W + frames[0].w - travel - x0
                y = y0 + bobAmp * sin(TWO_PI * t / bobPeriod)
                flip = dir < 0
            }
            HOVER -> {
                val ph = TWO_PI * (t + delay) / period
                x = x0 + rx * sin(ph)
                y = y0 + ry * sin(ph * 1.7f + 1f)
                flip = cos(ph) < 0f
            }
            else -> {
                val th = TWO_PI * (t + delay) / period * dir
                x = x0 + rx * cos(th)
                y = y0 + ry * sin(th)
                flip = -sin(th) * dir < 0f
            }
        }
        frame = ((tick / frameStep) + frameOffset) and 1
    }

    private var frame = 0

    fun draw(out: PixelBlitter, fade: Int = 16) {
        val s = frames[if (frames.size == 1) 0 else frame % frames.size]
        out.blit(s, (x - s.w / 2f).roundToInt(), (y - s.h / 2f).roundToInt(), flip, fade = fade)
    }

    /** Index of the sprite frame for turtles, which have four, instead of the two-frame fish tail. */
    fun drawWithFrame(out: PixelBlitter, tick: Int) {
        val s = frames[(tick / 4) % frames.size]
        out.blit(s, (x - s.w / 2f).roundToInt(), (y - s.h / 2f).roundToInt(), flip)
    }

    companion object {
        private const val CROSS = 0
        private const val HOVER = 1
        private const val ORBIT = 2
        private const val TWO_PI = (PI * 2).toFloat()

        private fun loop(v: Float): Float = v - floor(v)

        /** Crosses the screen once per [period] seconds on a line at [y], [xOff] pixels ahead of the group's lead. */
        fun cross(
            frames: Array<Sprite>, y: Float, period: Float, delay: Float, dir: Int,
            xOff: Float = 0f, bobAmp: Float = 0f, bobPeriod: Float = 6f, offset: Int = 0,
        ) = Swimmer(frames, CROSS, xOff, y, 0f, 0f, period, delay, dir, bobAmp, bobPeriod, 3, offset)

        /** Hovers around ([x], [y]) in a small loop of radii [rx] by [ry]. */
        fun hover(frames: Array<Sprite>, x: Float, y: Float, rx: Float, ry: Float, period: Float, delay: Float, offset: Int = 0) =
            Swimmer(frames, HOVER, x, y, rx, ry, period, delay, 1, 0f, 1f, 3, offset)

        /** Circles ([cx], [cy]) on an ellipse, starting [theta] radians round it. */
        fun orbit(frames: Array<Sprite>, cx: Float, cy: Float, rx: Float, ry: Float, period: Float, theta: Float, dir: Int, offset: Int = 0) =
            Swimmer(frames, ORBIT, cx, cy, rx, ry, period, theta / TWO_PI * period * dir, dir, 0f, 1f, 3, offset)
    }
}
