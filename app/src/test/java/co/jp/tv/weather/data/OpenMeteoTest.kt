package co.jp.tv.weather.data

import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCondition
import co.jp.tv.weather.domain.WeatherPlace
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpenMeteoTest {
    private val now = LocalDateTime.of(2025, 11, 8, 14, 20)

    private fun fixture(name: String): String =
        checkNotNull(javaClass.getResource("/weather/$name")) { "missing fixture $name" }.readText()

    private class FakeTransport(private val body: String? = null, private val error: Exception? = null) : HttpTransport {
        val urls = mutableListOf<String>()
        override suspend fun get(url: String): String {
            urls += url
            error?.let { throw it }
            return body!!
        }
    }

    // forecast

    @Test
    fun `the forecast request asks for the three current fields at the place with local time`() = runTest {
        val transport = FakeTransport(fixture("forecast-bucaramanga.json"))
        OpenMeteoWeatherRemote(transport).fetch(WeatherPlace.Default, now)
        val url = transport.urls.single()
        assertTrue(url.startsWith("https://api.open-meteo.com/v1/forecast?"), url)
        assertTrue("latitude=7.1254" in url, url)
        assertTrue("longitude=-73.1198" in url, url)
        assertTrue("current=temperature_2m,weather_code,is_day" in url, url)
        assertTrue("timezone=auto" in url, url)
    }

    @Test
    fun `a forecast fixture parses into a reading stamped with the fetch time`() = runTest {
        val weather = OpenMeteoWeatherRemote(FakeTransport(fixture("forecast-bucaramanga.json"))).fetch(WeatherPlace.Default, now)
        assertEquals(
            Weather(WeatherCondition.Rain, temperatureC = 27, isDay = true, updatedAt = now, placeName = "Bucaramanga"),
            weather,
        )
    }

    @Test
    fun `is_day zero parses as night`() {
        val json = """{"current":{"time":"2025-11-08T22:00","temperature_2m":19.4,"weather_code":95,"is_day":0}}"""
        val weather = OpenMeteoJson.parseWeather(json, WeatherPlace.Default, now)
        assertFalse(weather.isDay)
        assertEquals(WeatherCondition.Storm, weather.condition)
        assertEquals(19, weather.temperatureC)
    }

    @Test
    fun `negative temperatures round to the nearest degree`() {
        val json = """{"current":{"temperature_2m":-1.6,"weather_code":3,"is_day":1}}"""
        assertEquals(-2, OpenMeteoJson.parseWeather(json, WeatherPlace.Default, now).temperatureC)
    }

    @Test
    fun `a forecast without a current block is an error`() {
        assertThrows(IOException::class.java) { OpenMeteoJson.parseWeather("""{"latitude":1}""", WeatherPlace.Default, now) }
    }

    @Test
    fun `a forecast with a missing field is an error`() {
        val json = """{"current":{"temperature_2m":20.0,"is_day":1}}"""
        assertThrows(IOException::class.java) { OpenMeteoJson.parseWeather(json, WeatherPlace.Default, now) }
    }

    @Test
    fun `text that is not json is an error`() {
        assertThrows(IOException::class.java) { OpenMeteoJson.parseWeather("<html>502</html>", WeatherPlace.Default, now) }
    }

    @Test
    fun `a transport failure reaches the caller so the repository can fall back`() = runTest {
        val remote = OpenMeteoWeatherRemote(FakeTransport(error = IOException("timeout")))
        val failure = runCatching { remote.fetch(WeatherPlace.Default, now) }.exceptionOrNull()
        assertTrue(failure is IOException, "$failure")
    }

    // geocoding

    @Test
    fun `the search request is Spanish, limited to Colombia and ten results`() = runTest {
        val transport = FakeTransport(fixture("geocoding-giron.json"))
        OpenMeteoPlaceSearch(transport).search("Girón")
        val url = transport.urls.single()
        assertTrue(url.startsWith("https://geocoding-api.open-meteo.com/v1/search?"), url)
        assertTrue("name=Gir%C3%B3n" in url, url)
        assertTrue("count=10" in url, url)
        assertTrue("language=es" in url, url)
        assertTrue("countryCode=CO" in url, url)
    }

    @Test
    fun `geocoding results become places with their department`() = runTest {
        val places = OpenMeteoPlaceSearch(FakeTransport(fixture("geocoding-giron.json"))).search("Girón")
        assertEquals(3, places.size)
        assertEquals(WeatherPlace("Girón", "Santander", 7.06833, -73.16973), places[0])
        assertEquals("Cundinamarca", places[1].admin1)
        assertNull(places[2].admin1)
        assertEquals("Villa Girón", places[2].name)
    }

    @Test
    fun `a search with no results is an empty list`() = runTest {
        assertEquals(emptyList<WeatherPlace>(), OpenMeteoPlaceSearch(FakeTransport(fixture("geocoding-empty.json"))).search("zzzz"))
    }

    @Test
    fun `a blank or one letter query never hits the network`() = runTest {
        val transport = FakeTransport(fixture("geocoding-giron.json"))
        val search = OpenMeteoPlaceSearch(transport)
        assertEquals(emptyList<WeatherPlace>(), search.search("   "))
        assertEquals(emptyList<WeatherPlace>(), search.search(" a "))
        assertTrue(transport.urls.isEmpty())
    }

    @Test
    fun `a failing search is an empty list, not a crash`() = runTest {
        assertEquals(emptyList<WeatherPlace>(), OpenMeteoPlaceSearch(FakeTransport(error = IOException("offline"))).search("Girón"))
        assertEquals(emptyList<WeatherPlace>(), OpenMeteoPlaceSearch(FakeTransport("not json")).search("Girón"))
    }

    @Test
    fun `results with unusable coordinates are skipped`() {
        val json = """{"results":[{"name":"A","latitude":95.0,"longitude":0.0},{"name":"","latitude":1.0,"longitude":2.0},{"name":"B","latitude":1.0,"longitude":2.0}]}"""
        assertEquals(listOf(WeatherPlace("B", null, 1.0, 2.0)), OpenMeteoJson.parsePlaces(json))
    }

    // file cache codec

    @Test
    fun `a cached reading round trips for the same place`() {
        val weather = Weather(WeatherCondition.Fog, -3, isDay = false, updatedAt = now, placeName = "Bucaramanga", stale = true)
        val text = WeatherCacheCodec.encode(WeatherPlace.Default, weather)
        assertEquals(weather.copy(stale = false), WeatherCacheCodec.decode(text, WeatherPlace.Default))
    }

    @Test
    fun `a cached reading is ignored for another place`() {
        val weather = Weather(WeatherCondition.Clear, 22, isDay = true, updatedAt = now, placeName = "Bucaramanga")
        val text = WeatherCacheCodec.encode(WeatherPlace.Default, weather)
        assertNull(WeatherCacheCodec.decode(text, WeatherPlace.QuickPicks[2]))
    }

    @Test
    fun `a damaged cache decodes to nothing`() {
        listOf("", "garbage", "{}", """{"lat":7.1254,"lon":-73.1198,"condition":"Nope","temp":1,"day":true,"at":"2025-11-08T14:20","place":"x"}""")
            .forEach { assertNull(WeatherCacheCodec.decode(it, WeatherPlace.Default), it) }
    }
}
