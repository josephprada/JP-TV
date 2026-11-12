package co.jp.tv.weather.domain

import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CachedWeatherRepositoryTest {
    private val t0 = LocalDateTime.of(2025, 11, 8, 12, 0)
    private val bga = WeatherPlace.Default
    private val giron = WeatherPlace.QuickPicks.first { it.name == "Girón" }

    private class FakeRemote : WeatherRemote {
        var calls = 0
        var failing = false
        var condition = WeatherCondition.Rain
        override suspend fun fetch(place: WeatherPlace, now: LocalDateTime): Weather {
            calls++
            if (failing) throw IOException("offline")
            return Weather(condition, 24, isDay = true, updatedAt = now, placeName = place.name)
        }
    }

    private class FakeCache : WeatherCache {
        val saved = mutableMapOf<WeatherPlace, Weather>()
        override suspend fun load(place: WeatherPlace): Weather? = saved[place]
        override suspend fun save(place: WeatherPlace, weather: Weather) {
            saved[place] = weather
        }
    }

    private val remote = FakeRemote()
    private val cache = FakeCache()
    private val repo = CachedWeatherRepository(remote, cache)

    @Test
    fun `the first call fetches, caches and returns the reading`() = runTest {
        val w = repo.current(bga, t0)
        assertEquals(WeatherCondition.Rain, w?.condition)
        assertEquals(1, remote.calls)
        assertEquals(w, cache.saved[bga])
    }

    @Test
    fun `a reading under 30 minutes old is served without the network`() = runTest {
        repo.current(bga, t0)
        repo.current(bga, t0.plusMinutes(29))
        assertEquals(1, remote.calls)
    }

    @Test
    fun `after 30 minutes the next call refreshes`() = runTest {
        repo.current(bga, t0)
        remote.condition = WeatherCondition.Clear
        val w = repo.current(bga, t0.plusMinutes(30))
        assertEquals(2, remote.calls)
        assertEquals(WeatherCondition.Clear, w?.condition)
        assertEquals(t0.plusMinutes(30), w?.updatedAt)
    }

    @Test
    fun `offline keeps serving the last reading`() = runTest {
        val first = repo.current(bga, t0)
        remote.failing = true
        assertEquals(first, repo.current(bga, t0.plusHours(1)))
        assertEquals(first, repo.current(bga, t0.plusHours(5)))
    }

    @Test
    fun `offline with nothing cached returns null`() = runTest {
        remote.failing = true
        assertNull(repo.current(bga, t0))
    }

    @Test
    fun `a failed refresh is not retried on every call`() = runTest {
        repo.current(bga, t0)
        remote.failing = true
        repeat(10) { repo.current(bga, t0.plusMinutes(31).plusSeconds(it * 20L)) }
        assertEquals(2, remote.calls)
        repo.current(bga, t0.plusMinutes(37))
        assertEquals(3, remote.calls)
    }

    @Test
    fun `a new process reads the file cache before touching the network`() = runTest {
        repo.current(bga, t0)
        val fresh = CachedWeatherRepository(remote, cache)
        fresh.current(bga, t0.plusMinutes(10))
        assertEquals(1, remote.calls)
    }

    @Test
    fun `cached returns the saved reading even when old`() = runTest {
        repo.current(bga, t0)
        val fresh = CachedWeatherRepository(remote, cache)
        assertEquals(t0, fresh.cached(bga)?.updatedAt)
        assertEquals(1, remote.calls)
    }

    @Test
    fun `another place never gets the previous place reading`() = runTest {
        repo.current(bga, t0)
        val w = repo.current(giron, t0.plusMinutes(1))
        assertEquals("Girón", w?.placeName)
        assertEquals(2, remote.calls)
        remote.failing = true
        assertNull(repo.current(WeatherPlace("Quibdó", null, 5.69, -76.65), t0.plusHours(2)))
    }

    @Test
    fun `changing place fetches at once even right after a failure`() = runTest {
        remote.failing = true
        repo.current(bga, t0)
        remote.failing = false
        assertEquals("Girón", repo.current(giron, t0.plusSeconds(5))?.placeName)
    }

    @Test
    fun `the null repository has nothing`() = runTest {
        assertNull(WeatherRepository.None.current(bga, t0))
        assertNull(WeatherRepository.None.cached(bga))
    }
}
