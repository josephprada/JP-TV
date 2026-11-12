package co.jp.tv.themes.cyberpunk

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import co.jp.tv.core.design.JpMotion

/** Motion values of the Cyberpunk artboard. */
internal object CyberpunkMotion {
    /** Launcher entrance curve for focus, track and parallax (the design's `ease` was slower). */
    val Ease: Easing = JpMotion.Entrance

    /** CSS `ease-in-out`. */
    val CssEaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** CSS `ease-out`. */
    val CssEaseOut: Easing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** Sign focus, track and scrubber ticks (the design's 0.6 s, shortened for the remote). */
    const val FocusMs = JpMotion.NavMs

    /** Parallax layers (the design's 1.1 s, shortened). */
    const val ParallaxMs = 600

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
