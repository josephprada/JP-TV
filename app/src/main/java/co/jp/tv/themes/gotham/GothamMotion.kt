package co.jp.tv.themes.gotham

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import co.jp.tv.core.design.JpMotion

/** Motion of the Gotham artboard. Times are milliseconds, distances design px (1920x1080). */
internal object GothamMotion {
    /** Card focus curve: the launcher entrance curve (the artboard's was slower). */
    val Focus: Easing = JpMotion.Entrance

    /** CSS `ease-in-out`, used by the sway, fog and mote loops. */
    val EaseInOut: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    const val FocusMs = JpMotion.NavMs
    const val StampMs = 260
    const val EntranceMs = 800
    const val TopBarDelayMs = 120

    /** `ng-in` rises 16 design px, i.e. 8 dp. */
    const val EntranceRiseDp = 8f

    /** `ng-sway`: -9deg to 7deg over 9 s, alternate. */
    const val SwayFromDeg = -9f
    const val SwayToDeg = 7f
    const val SwayMs = 9_000f

    /** `ng-fog`: -220px to 220px. */
    const val FogFromPx = -220f
    const val FogToPx = 220f

    /** `ng-rain`: -120px to 1500px. */
    const val RainFromPx = -120f
    const val RainToPx = 1500f

    /** `ng-blink`: 2.4 s, steps(1). */
    const val BlinkMs = 2_400f

    /**
     * Eased progress in [0, 1] of an `infinite alternate` CSS animation of [durationMs].
     * [reverse] mirrors it for `alternate-reverse`.
     */
    fun alternate(timeMs: Long, durationMs: Float, reverse: Boolean = false): Float {
        val cycle = (timeMs % (2 * durationMs)) / durationMs
        val linear = if (cycle < 1f) cycle else 2f - cycle
        return EaseInOut.transform(if (reverse) 1f - linear else linear)
    }

    /**
     * Linear progress in [0, 1) of an `infinite` CSS animation with a start delay, or -1 while
     * the delay has not elapsed (the element then shows its unanimated style).
     */
    fun loop(timeMs: Long, durationMs: Float, delayMs: Float): Float {
        val elapsed = timeMs - delayMs
        if (elapsed < 0f) return -1f
        return (elapsed % durationMs) / durationMs
    }
}
