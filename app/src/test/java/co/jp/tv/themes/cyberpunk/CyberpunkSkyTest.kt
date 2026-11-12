package co.jp.tv.themes.cyberpunk

import co.jp.tv.weather.domain.WeatherCondition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CyberpunkSkyTest {
    @Test
    fun `with no reading the city keeps its design, a rainy night`() {
        val sky = CyberpunkSky.of(null)
        assertEquals(CyberpunkSky.Design, sky)
        assertEquals(1f, sky.rain)
        assertEquals(0f, sky.fog)
        assertEquals(false, sky.lightning)
    }

    @Test
    fun `dry skies draw no rain`() {
        listOf(WeatherCondition.Clear, WeatherCondition.PartlyCloudy, WeatherCondition.Cloudy, WeatherCondition.Fog)
            .forEach { assertEquals(0f, CyberpunkSky.of(it).rain, "$it") }
    }

    @Test
    fun `rain grows from drizzle to storm`() {
        val drizzle = CyberpunkSky.of(WeatherCondition.Drizzle).rain
        val rain = CyberpunkSky.of(WeatherCondition.Rain).rain
        val storm = CyberpunkSky.of(WeatherCondition.Storm).rain
        assertTrue(drizzle > 0f && drizzle < rain && rain < storm)
    }

    @Test
    fun `only fog brings haze and only a storm brings lightning`() {
        WeatherCondition.entries.forEach {
            assertEquals(it == WeatherCondition.Fog, CyberpunkSky.of(it).fog > 0f, "$it fog")
            assertEquals(it == WeatherCondition.Storm, CyberpunkSky.of(it).lightning, "$it lightning")
        }
    }

    @Test
    fun `a storm is gloomier than a clear sky`() {
        assertTrue(CyberpunkSky.of(WeatherCondition.Storm).murk > CyberpunkSky.of(WeatherCondition.Cloudy).murk)
        assertEquals(0f, CyberpunkSky.of(WeatherCondition.Clear).murk)
    }

    @Test
    fun `counts scale with the knob and never exceed what the city has`() {
        assertEquals(0, CyberpunkSky.count(0f, 120))
        assertEquals(120, CyberpunkSky.count(1f, 120))
        assertEquals(42, CyberpunkSky.count(0.35f, 120))
        assertEquals(120, CyberpunkSky.count(2f, 120))
    }
}
