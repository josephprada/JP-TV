package co.jp.tv.themes.gotham

import co.jp.tv.themes.shared.DayPhase
import co.jp.tv.weather.domain.WeatherCondition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GothamSkyTest {
    @Test
    fun `with no reading the sky keeps the artboard look, rain at night only`() {
        val sky = GothamSky.of(null)
        assertEquals(GothamSky.Artboard, sky)
        assertEquals(1f, sky.rain(night = true))
        assertEquals(0f, sky.rain(night = false))
    }

    @Test
    fun `dry skies draw no rain in either phase`() {
        listOf(WeatherCondition.Clear, WeatherCondition.PartlyCloudy, WeatherCondition.Cloudy, WeatherCondition.Fog).forEach {
            assertEquals(0f, GothamSky.of(it).rain(true), "$it night")
            assertEquals(0f, GothamSky.of(it).rain(false), "$it day")
        }
    }

    @Test
    fun `rain grows from drizzle to storm and falls by day too`() {
        val d = GothamSky.of(WeatherCondition.Drizzle).rain(false)
        val r = GothamSky.of(WeatherCondition.Rain).rain(false)
        val s = GothamSky.of(WeatherCondition.Storm).rain(false)
        assertTrue(d > 0f && d < r && r < s)
    }

    @Test
    fun `cloud cover grows with the weather`() {
        assertTrue(GothamSky.of(WeatherCondition.Clear).clouds < GothamSky.of(WeatherCondition.PartlyCloudy).clouds)
        assertTrue(GothamSky.of(WeatherCondition.PartlyCloudy).clouds < GothamSky.of(WeatherCondition.Cloudy).clouds)
        assertTrue(GothamSky.of(WeatherCondition.Cloudy).clouds <= GothamSky.of(WeatherCondition.Storm).clouds)
    }

    @Test
    fun `fog is thickest in fog, rain only mists a little and dry skies have none`() {
        val fog = GothamSky.of(WeatherCondition.Fog).fog
        WeatherCondition.entries.filter { it != WeatherCondition.Fog }.forEach {
            assertTrue(GothamSky.of(it).fog < fog, "$it")
        }
        listOf(WeatherCondition.Clear, WeatherCondition.PartlyCloudy, WeatherCondition.Cloudy).forEach {
            assertEquals(0f, GothamSky.of(it).fog, "$it")
        }
    }

    @Test
    fun `only a storm brings lightning`() {
        WeatherCondition.entries.forEach {
            assertEquals(it == WeatherCondition.Storm, GothamSky.of(it).lightning, "$it lightning")
        }
    }

    @Test
    fun `rain drops scale with the knob`() {
        assertEquals(0, GothamSky.drops(0f, 110))
        assertEquals(110, GothamSky.drops(1f, 110))
        assertEquals(33, GothamSky.drops(0.3f, 110))
    }

    @Test
    fun `the look follows the day phase`() {
        assertEquals(GothamLook.Night, GothamLook.of(DayPhase.NIGHT))
        assertEquals(GothamLook.Day, GothamLook.of(DayPhase.DAY))
    }

    @Test
    fun `by day the sky is lighter, the windows are dark and the searchlight is off`() {
        val day = GothamLook.Day
        val night = GothamLook.Night
        assertTrue(day.skyTop.red > night.skyTop.red)
        assertTrue(day.skyBottom.red > night.skyBottom.red)
        assertTrue(day.farTop.red > night.farTop.red, "the day skyline is muted, not black")
        assertFalse(day.litWindows)
        assertTrue(night.litWindows)
        assertEquals(0f, day.beam)
        assertEquals(1f, night.beam)
        assertFalse(day.beacons)
        assertTrue(night.beacons)
    }

    @Test
    fun `the night look is the original palette`() {
        val night = GothamLook.Night
        assertEquals(GothamPalette.SkyTop, night.skyTop)
        assertEquals(GothamPalette.NearFoot, night.nearFoot)
        assertEquals(GothamPalette.Rain, night.rain)
        assertEquals(GothamPalette.Ledge, night.ledge)
    }

    @Test
    fun `the near skyline stays the darkest layer in both phases`() {
        for (look in listOf(GothamLook.Night, GothamLook.Day)) {
            assertTrue(look.nearTop.red <= look.midTop.red, "near darker than mid")
            assertTrue(look.midTop.red <= look.farTop.red, "mid darker than far")
        }
    }
}
