package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** Motion values of the Cyberpunk artboard. */
internal object CyberpunkMotion {
    /** Design `ease`: cubic-bezier(0.32,0.72,0,1), used for focus, track and parallax. */
    val Ease: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    /** CSS `ease-in-out`. */
    val CssEaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** CSS `ease-out`. */
    val CssEaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** Design `T`: 0.6s for sign focus, track and scrubber ticks. */
    const val FocusMs = 600

    /** Parallax layers: 1.1s. */
    const val ParallaxMs = 1100

    /** `cy-on` neon power-up of the focused sign. */
    const val SignOnMs = 1300

    /** `cy-in` entrance of the top-right controls. */
    const val EnterMs = 800

    /** Track step between signs, design px. */
    const val STEP = 260f

    /** Parallax per focused index, design px. */
    const val FAR_SHIFT = 6f
    const val MID_SHIFT = 16f

    /** The design has 10 apps; parallax is normalized to that range so long lists never run past the city. */
    const val DESIGN_APP_COUNT = 10
}
