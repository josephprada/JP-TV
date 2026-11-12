package co.jp.tv.themes.multiverso

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import co.jp.tv.core.design.JpMotion

/** Motion values of the Multiverso artboard. Entrances use JpMotion.Entrance (same curve). */
internal object MultiversoMotion {
    /** The launcher entrance curve; the artboard's slower `ease` was too sluggish for D-pad moves. */
    val Swift: Easing = JpMotion.Entrance

    /** CSS `ease-in-out`, used by the nebula drift and the star twinkle. */
    val CssEaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** CSS `ease-out`, used by the ripples. */
    val CssEaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** Focus transition (the artboard's 0.7 s, shortened for the remote). */
    const val FocusMs = JpMotion.NavMs

    /** Star and timeline parallax (the artboard's 1.1 s, shortened). */
    const val ParallaxMs = 600

    /** `mv-in 0.9s`. */
    const val EntranceMs = 900

    const val TrunkFlowMs = 6_000L
    const val BranchFlowMs = 3_400L
    const val RippleMs = 3_200L
    const val RippleDelayMs = 1_600L
    const val RingSpinOnMs = 22_000L
    const val RingSpinOffMs = 60_000L
}
