package co.jp.tv.themes.lofi

import co.jp.tv.weather.domain.WeatherCondition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LofiSkyTest {
    private fun of(c: WeatherCondition) = LofiSky.of(c)

    @Test
    fun `with no reading the window keeps the artboard look, rain at night only`() {
        val sky = LofiSky.of(null)
        assertEquals(LofiSky.Artboard, sky)
        assertEquals(0f, sky.rainDay)
        assertEquals(1f, sky.rainNight)
        assertEquals(0f, sky.overcast)
    }

    @Test
    fun `clear and partly cloudy skies have no rain in either phase`() {
        for (c in listOf(WeatherCondition.Clear, WeatherCondition.PartlyCloudy, WeatherCondition.Cloudy, WeatherCondition.Fog)) {
            assertEquals(0f, of(c).rainDay, "$c day")
            assertEquals(0f, of(c).rainNight, "$c night")
        }
    }

    @Test
    fun `rain gets heavier from drizzle to storm and falls by day too`() {
        val drizzle = of(WeatherCondition.Drizzle)
        val rain = of(WeatherCondition.Rain)
        val storm = of(WeatherCondition.Storm)
        assertTrue(drizzle.rainDay > 0f && drizzle.rainDay < rain.rainDay)
        assertTrue(rain.rainDay < storm.rainDay)
        assertEquals(drizzle.rainDay, drizzle.rainNight)
        assertEquals(storm.rainDay, storm.rainNight)
    }

    @Test
    fun `cloud cover and the veil grow with the weather and the sunbeam fades`() {
        assertTrue(of(WeatherCondition.Clear).clouds < of(WeatherCondition.PartlyCloudy).clouds)
        assertTrue(of(WeatherCondition.Cloudy).overcast < of(WeatherCondition.Rain).overcast)
        assertTrue(of(WeatherCondition.Rain).overcast < of(WeatherCondition.Storm).overcast)
        assertTrue(of(WeatherCondition.Clear).sunbeam > of(WeatherCondition.Cloudy).sunbeam)
        assertEquals(0f, of(WeatherCondition.Storm).sunbeam)
    }

    @Test
    fun `only fog hazes the skyline and only a storm flashes`() {
        WeatherCondition.entries.forEach {
            assertEquals(it == WeatherCondition.Fog, of(it).fog > 0f, "$it fog")
            assertEquals(it == WeatherCondition.Storm, of(it).lightning, "$it lightning")
        }
    }

    @Test
    fun `lightning is rare, brief and never above full strength`() {
        var lit = 0
        for (ms in 0 until LofiSky.LIGHTNING_PERIOD_MS step 10) {
            val f = LofiSky.flash(ms.toLong())
            assertTrue(f in 0f..1f, "$ms -> $f")
            if (f > 0f) lit++
        }
        val share = lit * 10f / LofiSky.LIGHTNING_PERIOD_MS
        assertTrue(share < 0.05f, "lit $share of the cycle")
        assertTrue(LofiSky.flash(0) > 0.5f, "the cycle opens with a flash")
        assertEquals(0f, LofiSky.flash(LofiSky.LIGHTNING_PERIOD_MS / 2L))
    }

    @Test
    fun `rain drop counts scale with the knob and drop on low end boxes`() {
        assertEquals(0, LofiSky.dropCount(0f, total = 34, lowEnd = false))
        assertEquals(34, LofiSky.dropCount(1f, total = 34, lowEnd = false))
        assertEquals(10, LofiSky.dropCount(0.3f, total = 34, lowEnd = false))
        assertTrue(LofiSky.dropCount(1f, total = 34, lowEnd = true) < 34)
        assertFalse(LofiSky.dropCount(0.3f, total = 34, lowEnd = true) > LofiSky.dropCount(0.3f, total = 34, lowEnd = false))
    }
}
