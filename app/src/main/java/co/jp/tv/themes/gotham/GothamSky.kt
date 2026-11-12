package co.jp.tv.themes.gotham

import co.jp.tv.weather.domain.WeatherCondition

/**
 * What the Gotham sky does for a weather condition, as knobs: [rainDay] and [rainNight] are the share of the
 * rain drops falling in each phase (0..1), [clouds] scales the cloud banks (1 = the artboard's), [fog] is the
 * veil over the skyline (0..1), [murk] a dark veil over the sky (0..1) and [lightning] rare flashes behind the
 * skyline. Without a reading the sky keeps the artboard look ([Artboard]): a rainy night and a dry day.
 */
internal data class GothamSky(
    val rainDay: Float,
    val rainNight: Float,
    val clouds: Float,
    val fog: Float,
    val murk: Float,
    val lightning: Boolean,
) {
    fun rain(night: Boolean): Float = if (night) rainNight else rainDay

    companion object {
        val Artboard = GothamSky(rainDay = 0f, rainNight = 1f, clouds = 1f, fog = 0f, murk = 0f, lightning = false)

        fun of(condition: WeatherCondition?): GothamSky = when (condition) {
            null -> Artboard
            WeatherCondition.Clear -> GothamSky(0f, 0f, clouds = 0.35f, fog = 0f, murk = 0f, lightning = false)
            WeatherCondition.PartlyCloudy -> GothamSky(0f, 0f, clouds = 0.7f, fog = 0f, murk = 0f, lightning = false)
            WeatherCondition.Cloudy -> GothamSky(0f, 0f, clouds = 1f, fog = 0f, murk = 0.08f, lightning = false)
            WeatherCondition.Fog -> GothamSky(0f, 0f, clouds = 0.8f, fog = 0.6f, murk = 0.05f, lightning = false)
            WeatherCondition.Drizzle -> GothamSky(0.3f, 0.3f, clouds = 1f, fog = 0.1f, murk = 0.1f, lightning = false)
            WeatherCondition.Rain -> GothamSky(0.75f, 0.75f, clouds = 1f, fog = 0.15f, murk = 0.18f, lightning = false)
            WeatherCondition.Storm -> GothamSky(1f, 1f, clouds = 1f, fog = 0.15f, murk = 0.3f, lightning = true)
        }

        /** How many of [total] rain drops an [amount] draws. */
        fun drops(amount: Float, total: Int): Int = (total * amount).toInt().coerceIn(0, total)
    }
}
