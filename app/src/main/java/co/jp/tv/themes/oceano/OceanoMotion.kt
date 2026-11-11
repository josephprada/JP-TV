package co.jp.tv.themes.oceano

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

/** Motion of the Océano home. Keyframe helpers take seconds and return values in [0, 1]. */
object OceanoMotion {
    /** The artboard's jelly scroll curve, `cubic-bezier(0.32,0.72,0,1)`. */
    val Scroll: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** Duration of the scroll and of every focus transition (`0.8s`). */
    const val ScrollMs = 800

    /** CSS `ease`, used by the 2.5 s day/night crossfade. */
    val PhaseFade: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    const val PhaseFadeMs = 2500

    /** Brand and top-right entrance (`sw-in 0.9s`, the second one delayed 0.12 s). */
    const val EntranceMs = 900
    const val EntranceStaggerMs = 120

    /** Width of one jelly cell, in design px (`STEP = 280`). */
    const val StepPx = 280f

    /** Fraction of a loop of [periodS] seconds at [timeS], shifted by a CSS negative [delayS]. */
    fun loop(timeS: Float, periodS: Float, delayS: Float = 0f): Float {
        val x = (timeS + delayS) / periodS
        return x - floor(x)
    }

    /** `ease-in-out infinite alternate`: 0 -> 1 over [periodS], back to 0 over the next one. */
    fun alternate(timeS: Float, periodS: Float, delayS: Float = 0f): Float {
        val x = (timeS + delayS) / periodS
        val u = x - 2f * floor(x / 2f)
        return smooth(if (u < 1f) u else 2f - u)
    }

    /** `0%,100% {a} 50% {b}` with ease-in-out per half: 0 at the loop ends, 1 in the middle. */
    fun pulse(timeS: Float, periodS: Float, delayS: Float = 0f): Float {
        val p = loop(timeS, periodS, delayS)
        return smooth(if (p < 0.5f) p * 2f else 2f - p * 2f)
    }

    /** Close stand-in for CSS `ease-in-out`. */
    fun smooth(x: Float): Float = (0.5 - 0.5 * cos(PI * x.coerceIn(0f, 1f))).toFloat()

    fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
