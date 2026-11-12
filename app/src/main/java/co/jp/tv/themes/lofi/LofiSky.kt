package co.jp.tv.themes.lofi

import co.jp.tv.weather.domain.WeatherCondition

/**
 * What the lo-fi window shows for a weather condition, as plain knobs (all 0..1 unless noted). Without a
 * reading the window keeps the artboard's look: a rainy night and a dry day ([Artboard]).
 *
 * - [rainDay], [rainNight]: share of the rain drops falling in each phase.
 * - [clouds]: how many of the day clouds show and how opaque they are (1 = the artboard's).
 * - [overcast]: grey veil over the sky and the sun or moon; any value above 0 also puts dark clouds in the night sky.
 * - [fog]: haze rising over the skyline.
 * - [lightning]: rare flashes in the window.
 * - [sunbeam]: strength of the sunbeam on the desk by day.
 */
internal data class LofiSky(
    val rainDay: Float,
    val rainNight: Float,
    val clouds: Float,
    val overcast: Float,
    val fog: Float,
    val lightning: Boolean,
    val sunbeam: Float,
) {
    companion object {
        val Artboard = LofiSky(rainDay = 0f, rainNight = 1f, clouds = 1f, overcast = 0f, fog = 0f, lightning = false, sunbeam = 1f)

        fun of(condition: WeatherCondition?): LofiSky = when (condition) {
            null -> Artboard
            WeatherCondition.Clear -> LofiSky(0f, 0f, clouds = 0.5f, overcast = 0f, fog = 0f, lightning = false, sunbeam = 1f)
            WeatherCondition.PartlyCloudy -> LofiSky(0f, 0f, clouds = 0.85f, overcast = 0f, fog = 0f, lightning = false, sunbeam = 0.8f)
            WeatherCondition.Cloudy -> LofiSky(0f, 0f, clouds = 1f, overcast = 0.2f, fog = 0f, lightning = false, sunbeam = 0.4f)
            WeatherCondition.Fog -> LofiSky(0f, 0f, clouds = 0.7f, overcast = 0.15f, fog = 0.6f, lightning = false, sunbeam = 0.3f)
            WeatherCondition.Drizzle -> LofiSky(0.3f, 0.3f, clouds = 1f, overcast = 0.25f, fog = 0f, lightning = false, sunbeam = 0.25f)
            WeatherCondition.Rain -> LofiSky(0.75f, 0.75f, clouds = 1f, overcast = 0.32f, fog = 0f, lightning = false, sunbeam = 0.1f)
            WeatherCondition.Storm -> LofiSky(1f, 1f, clouds = 1f, overcast = 0.45f, fog = 0f, lightning = true, sunbeam = 0f)
        }

        /** Drops to draw out of [total] for a rain [amount]; the slow boxes draw 60 % of them. */
        fun dropCount(amount: Float, total: Int, lowEnd: Boolean): Int =
            (total * amount * (if (lowEnd) LOW_END_SHARE else 1f)).toInt().coerceIn(0, total)

        private const val LOW_END_SHARE = 0.6f
    }
}
