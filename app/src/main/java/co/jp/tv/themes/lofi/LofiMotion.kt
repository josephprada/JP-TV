package co.jp.tv.themes.lofi

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Timing of the lo-fi artboard. CSS `ease-in-out` maps to `JpMotion.InOut`; these two have no token. */
internal object LofiMotion {
    /** CSS `ease`: the 2 s day/night opacity transitions. */
    val Ease: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

    /** CSS `ease-out`: the mug steam. */
    val EaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    const val PHASE_FADE_MS = 2000

    /** Position in [0, 1) of a looping animation of [periodMs] started [delayMs] after the clock origin. */
    fun loop(ms: Long, periodMs: Int, delayMs: Int = 0): Float =
        (ms - delayMs).mod(periodMs.toLong()).toFloat() / periodMs

    /** CSS `alternate`: 0 -> 1 during [periodMs], then 1 -> 0. */
    fun alternate(ms: Long, periodMs: Int): Float {
        val t = loop(ms, periodMs * 2) * 2f
        return if (t <= 1f) t else 2f - t
    }
}
