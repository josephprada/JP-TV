package co.jp.tv.themes.cyberpunk

import co.jp.tv.weather.domain.WeatherCondition

/**
 * What the neon city's weather looks like for a condition, as knobs (0..1): [rain] is the share of the rain
 * drops and street splashes drawn, [fog] the haze over the towers, [murk] a dark veil over the sky, and
 * [lightning] rare flashes. Without a reading the city keeps its design look ([Design]: steady rain).
 */
internal data class CyberpunkSky(
    val rain: Float,
    val fog: Float,
    val murk: Float,
    val lightning: Boolean,
) {
    companion object {
        val Design = CyberpunkSky(rain = 1f, fog = 0f, murk = 0f, lightning = false)

        fun of(condition: WeatherCondition?): CyberpunkSky = when (condition) {
            null -> Design
            WeatherCondition.Clear, WeatherCondition.PartlyCloudy -> CyberpunkSky(0f, 0f, 0f, false)
            WeatherCondition.Cloudy -> CyberpunkSky(0f, 0f, 0.18f, false)
            WeatherCondition.Fog -> CyberpunkSky(0f, 0.55f, 0.1f, false)
            WeatherCondition.Drizzle -> CyberpunkSky(0.35f, 0f, 0.1f, false)
            WeatherCondition.Rain -> CyberpunkSky(0.7f, 0f, 0.18f, false)
            WeatherCondition.Storm -> CyberpunkSky(1f, 0f, 0.32f, true)
        }

        /** How many of [total] particles a rain [amount] draws. */
        fun count(amount: Float, total: Int): Int = (total * amount).toInt().coerceIn(0, total)
    }
}
