package co.jp.tv.core.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/** JP design tokens: motion. */
object JpMotion {
    val Entrance: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val InOut: Easing = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)

    const val FocusMs = 350
    const val RiseMs = 700
    const val FadeMs = 800
    const val RowStaggerMs = 60
    const val BreathHalfCycleMs = 2000
}
