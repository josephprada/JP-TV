package co.jp.tv.weather.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WeatherPlaceTest {
    @Test
    fun `the default place is Bucaramanga`() {
        assertEquals("Bucaramanga", WeatherPlace.Default.name)
        assertEquals("Santander", WeatherPlace.Default.admin1)
        assertEquals(7.1254, WeatherPlace.Default.lat, 1e-6)
        assertEquals(-73.1198, WeatherPlace.Default.lon, 1e-6)
    }

    @Test
    fun `the quick picks start with the default and include the ten towns`() {
        assertEquals(WeatherPlace.Default, WeatherPlace.QuickPicks.first())
        assertEquals(
            listOf(
                "Bucaramanga", "Floridablanca", "Girón", "Piedecuesta", "Lebrija",
                "Los Santos", "San Gil", "Barrancabermeja", "Socorro", "Barichara",
            ),
            WeatherPlace.QuickPicks.map { it.name },
        )
    }

    @Test
    fun `every quick pick sits inside Santander`() {
        WeatherPlace.QuickPicks.forEach {
            assertTrue(it.lat in 6.3..7.4, "${it.name} lat ${it.lat}")
            assertTrue(it.lon in -74.0..-72.9, "${it.name} lon ${it.lon}")
        }
    }

    @Test
    fun `the label joins the town and its department`() {
        assertEquals("Girón, Santander", WeatherPlace.QuickPicks[2].label)
        assertEquals("Medellín", WeatherPlace("Medellín", null, 6.25, -75.56).label)
    }

    @Test
    fun `a place survives encode and decode`() {
        WeatherPlace.QuickPicks.forEach { assertEquals(it, WeatherPlaceCodec.decode(WeatherPlaceCodec.encode(it))) }
        val noRegion = WeatherPlace("Quibdó", null, 5.69, -76.65)
        assertEquals(noRegion, WeatherPlaceCodec.decode(WeatherPlaceCodec.encode(noRegion)))
    }

    @Test
    fun `nothing saved decodes to Bucaramanga`() {
        assertEquals(WeatherPlace.Default, WeatherPlaceCodec.decode(null))
        assertEquals(WeatherPlace.Default, WeatherPlaceCodec.decode(""))
    }

    @Test
    fun `a damaged value decodes to Bucaramanga instead of failing`() {
        listOf("garbage", "a\tb\tc", "x\ty\tnope\t1.0", "x\ty\t99.0\t10.0", "x\ty\t10.0\t200.0", "\t\t7.1\t-73.1").forEach {
            assertEquals(WeatherPlace.Default, WeatherPlaceCodec.decode(it), it)
        }
    }

    @Test
    fun `tabs and newlines in a name cannot break the encoding`() {
        val decoded = WeatherPlaceCodec.decode(WeatherPlaceCodec.encode(WeatherPlace("Bad\tName\n", "A\tB", 4.0, -74.0)))
        assertNotNull(decoded)
        assertEquals(4.0, decoded.lat, 1e-9)
        assertEquals("Bad Name", decoded.name)
        assertEquals("A B", decoded.admin1)
    }
}
