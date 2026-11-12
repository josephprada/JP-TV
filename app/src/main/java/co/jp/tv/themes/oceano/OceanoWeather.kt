package co.jp.tv.themes.oceano

import co.jp.tv.weather.domain.WeatherCondition

/**
 * What the sky is doing above the reef, mapped from the real reading by [of] (no reading draws [Clear]).
 * Each condition is only a set of knobs for the pixel scene:
 *
 * - [rays]: strength of the sun window and light shafts, 0..1.
 * - [darken]: steps down the water ramp the whole water column is pushed (0 = clear water).
 * - [chop]: amplitude, in virtual pixels, of the wave line at the surface.
 * - [ripples]: rain rings spreading on the surface at any moment.
 * - [haze]: how much the far reef dissolves into the water, 0..1.
 */
internal enum class OceanoWeather(
    val rays: Float,
    val darken: Int,
    val chop: Float,
    val ripples: Int,
    val haze: Float,
) {
    Clear(rays = 1f, darken = 0, chop = 0.8f, ripples = 0, haze = 0f),
    PartlyCloudy(rays = 0.65f, darken = 0, chop = 1f, ripples = 0, haze = 0.05f),
    Cloudy(rays = 0.35f, darken = 1, chop = 1.2f, ripples = 0, haze = 0.1f),
    Drizzle(rays = 0.25f, darken = 1, chop = 1.2f, ripples = 5, haze = 0.15f),
    Rain(rays = 0.15f, darken = 1, chop = 1.6f, ripples = 10, haze = 0.2f),
    Storm(rays = 0f, darken = 2, chop = 2.6f, ripples = 18, haze = 0.25f),
    Fog(rays = 0.2f, darken = 0, chop = 0.8f, ripples = 0, haze = 0.65f),;

    companion object {
        /** The scene for a reading; the sky is clear until there is one. */
        fun of(condition: WeatherCondition?): OceanoWeather = when (condition) {
            null, WeatherCondition.Clear -> Clear
            WeatherCondition.PartlyCloudy -> PartlyCloudy
            WeatherCondition.Cloudy -> Cloudy
            WeatherCondition.Fog -> Fog
            WeatherCondition.Drizzle -> Drizzle
            WeatherCondition.Rain -> Rain
            WeatherCondition.Storm -> Storm
        }
    }
}
