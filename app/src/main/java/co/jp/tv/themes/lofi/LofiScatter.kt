package co.jp.tv.themes.lofi

/**
 * Rain drops and lit city windows, generated exactly like the artboard's `renderVals()`:
 * Park-Miller generator with `seed = 3`, `seed = seed * 16807 % 2147483647`, rain first
 * (left, height, duration, delay per drop), then lights (left, top, colour per light).
 * Positions are in artboard px relative to the window.
 */
internal class LofiScatter private constructor(
    val rainX: FloatArray,
    val rainHeight: FloatArray,
    val rainPeriodMs: IntArray,
    val rainDelayMs: IntArray,
    val lightX: FloatArray,
    val lightY: FloatArray,
    val lightAmber: BooleanArray,
) {
    companion object {
        const val RAIN_COUNT = 34
        const val LIGHT_COUNT = 14

        fun create(): LofiScatter {
            var seed = 3L
            fun rnd(): Double {
                seed = seed * 16807L % 2147483647L
                return seed / 2147483647.0
            }
            val rainX = FloatArray(RAIN_COUNT)
            val rainHeight = FloatArray(RAIN_COUNT)
            val rainPeriodMs = IntArray(RAIN_COUNT)
            val rainDelayMs = IntArray(RAIN_COUNT)
            for (i in 0 until RAIN_COUNT) {
                rainX[i] = Math.round(rnd() * 760).toFloat()
                rainHeight[i] = Math.round(30 + rnd() * 40).toFloat()
                // toFixed(2) seconds -> milliseconds.
                rainPeriodMs[i] = Math.round((0.7 + rnd() * 0.6) * 100).toInt() * 10
                rainDelayMs[i] = Math.round(rnd() * 1.2 * 100).toInt() * 10
            }
            val lightX = FloatArray(LIGHT_COUNT)
            val lightY = FloatArray(LIGHT_COUNT)
            val lightAmber = BooleanArray(LIGHT_COUNT)
            for (i in 0 until LIGHT_COUNT) {
                lightX[i] = Math.round(20 + rnd() * 720).toFloat()
                lightY[i] = Math.round(330 + rnd() * 160).toFloat()
                lightAmber[i] = rnd() > 0.5
            }
            return LofiScatter(rainX, rainHeight, rainPeriodMs, rainDelayMs, lightX, lightY, lightAmber)
        }
    }
}
