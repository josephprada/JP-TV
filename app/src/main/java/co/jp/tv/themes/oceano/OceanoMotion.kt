package co.jp.tv.themes.oceano

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Motion of the Océano home. */
object OceanoMotion {
    /** The artboard's day/night crossfade curve (CSS `ease`); the pixel scene dithers through it. */
    val PhaseFade: Easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    const val PhaseFadeMs = 2500

    /** Brand and top-right entrance (`sw-in 0.9s`, the second one delayed 0.12 s). */
    const val EntranceMs = 900
    const val EntranceStaggerMs = 120
}
