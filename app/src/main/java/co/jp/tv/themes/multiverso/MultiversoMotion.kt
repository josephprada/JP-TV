package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Motion values of the Multiverso artboard. Entrances use JpMotion.Entrance (same curve). */
internal object MultiversoMotion {
    /** The artboard's `ease` constant: cubic-bezier(0.32,0.72,0,1), used for focus and parallax. */
    val Swift: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** CSS `ease-in-out`, used by the nebula drift and the star twinkle. */
    val CssEaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** CSS `ease-out`, used by the ripples. */
    val CssEaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** Focus transition `T = 0.7s ease`. */
    const val FocusMs = 700

    /** Star and timeline parallax `transform 1.1s ease`. */
    const val ParallaxMs = 1100

    /** `mv-in 0.9s`. */
    const val EntranceMs = 900

    const val TrunkFlowMs = 6_000L
    const val BranchFlowMs = 3_400L
    const val RippleMs = 3_200L
    const val RippleDelayMs = 1_600L
    const val RingSpinOnMs = 22_000L
    const val RingSpinOffMs = 60_000L
}
