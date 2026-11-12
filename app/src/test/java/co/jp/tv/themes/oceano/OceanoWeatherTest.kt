package co.jp.tv.themes.oceano

import co.jp.tv.weather.domain.WeatherCondition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OceanoWeatherTest {
    @Test
    fun `every condition has a sky and no reading is a clear sky`() {
        assertEquals(OceanoWeather.Clear, OceanoWeather.of(null))
        assertEquals(OceanoWeather.Clear, OceanoWeather.of(WeatherCondition.Clear))
        assertEquals(OceanoWeather.PartlyCloudy, OceanoWeather.of(WeatherCondition.PartlyCloudy))
        assertEquals(OceanoWeather.Cloudy, OceanoWeather.of(WeatherCondition.Cloudy))
        assertEquals(OceanoWeather.Fog, OceanoWeather.of(WeatherCondition.Fog))
        assertEquals(OceanoWeather.Drizzle, OceanoWeather.of(WeatherCondition.Drizzle))
        assertEquals(OceanoWeather.Rain, OceanoWeather.of(WeatherCondition.Rain))
        assertEquals(OceanoWeather.Storm, OceanoWeather.of(WeatherCondition.Storm))
    }

    @Test
    fun `the sky gets darker and wetter from clear to storm`() {
        assertTrue(OceanoWeather.Clear.rays > OceanoWeather.PartlyCloudy.rays)
        assertTrue(OceanoWeather.PartlyCloudy.rays > OceanoWeather.Cloudy.rays)
        assertTrue(OceanoWeather.Drizzle.ripples < OceanoWeather.Rain.ripples)
        assertTrue(OceanoWeather.Rain.ripples < OceanoWeather.Storm.ripples)
        assertTrue(OceanoWeather.Storm.darken > OceanoWeather.Rain.darken)
    }
}
