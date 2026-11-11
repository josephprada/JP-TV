package co.jp.tv.themes.cyberpunk

/**
 * The artboard's CSS keyframes, evaluated at scene time. An animation still inside its delay has no
 * effect, as in CSS without a fill mode: callers keep the element's base value for a null progress.
 */
internal object CyberpunkKeyframes {
    /** Progress in [0, 1) of an infinite animation at [time] seconds, or null while still delayed. */
    fun progress(time: Float, duration: Float, delay: Float): Float? {
        if (time < delay || duration <= 0f) return null
        return ((time - delay) % duration) / duration
    }

    /** `cy-flicker`: 0-18% 1, 20% 0.4, 22% 1, 62% 1, 63% 0.4, 64% 1 (linear). */
    fun flicker(p: Float): Float = linear(p, FLICKER_AT, FLICKER_VALUE)

    /** `cy-twinkle`: 0.3 -> 1 -> 0.3, ease-in-out per half. */
    fun twinkle(p: Float): Float {
        val half = if (p < 0.5f) p / 0.5f else (p - 0.5f) / 0.5f
        val eased = CyberpunkMotion.CssEaseInOut.transform(half)
        return if (p < 0.5f) 0.3f + 0.7f * eased else 1f - 0.7f * eased
    }

    /** `cy-blink` with steps(1): 1 for the first half, 0.1 for the second. */
    fun blink(p: Float): Float = if (p < 0.5f) 1f else 0.1f

    /** `cy-holo`: 0.6, flickering between 47% and 54% (linear). */
    fun holo(p: Float): Float = linear(p, HOLO_AT, HOLO_VALUE)

    /** `cy-on`: neon power-up, 0.25 -> 1 with two dips (linear). */
    fun signOn(p: Float): Float = linear(p.coerceIn(0f, 1f), ON_AT, ON_VALUE)

    private fun linear(p: Float, at: FloatArray, value: FloatArray): Float {
        for (i in 1 until at.size) {
            if (p <= at[i]) {
                val span = at[i] - at[i - 1]
                val f = if (span <= 0f) 1f else (p - at[i - 1]) / span
                return value[i - 1] + (value[i] - value[i - 1]) * f
            }
        }
        return value.last()
    }

    private val FLICKER_AT = floatArrayOf(0f, 0.18f, 0.2f, 0.22f, 0.62f, 0.63f, 0.64f, 1f)
    private val FLICKER_VALUE = floatArrayOf(1f, 1f, 0.4f, 1f, 1f, 0.4f, 1f, 1f)
    private val HOLO_AT = floatArrayOf(0f, 0.47f, 0.48f, 0.5f, 0.52f, 0.54f, 1f)
    private val HOLO_VALUE = floatArrayOf(0.6f, 0.6f, 0.25f, 0.7f, 0.3f, 0.6f, 0.6f)
    private val ON_AT = floatArrayOf(0f, 0.08f, 0.12f, 0.2f, 0.27f, 0.33f, 1f)
    private val ON_VALUE = floatArrayOf(0.25f, 1f, 0.35f, 1f, 0.5f, 1f, 1f)
}
