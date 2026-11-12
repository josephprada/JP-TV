package co.jp.tv.weather.domain

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WeatherRulesTest {
    private val t0 = LocalDateTime.of(2025, 11, 8, 12, 0)

    private fun weather(at: LocalDateTime = t0) = Weather(
        condition = WeatherCondition.Clear,
        temperatureC = 27,
        isDay = true,
        updatedAt = at,
        placeName = "Bucaramanga",
    )

    @Test
    fun `a reading younger than 30 minutes needs no refresh`() {
        assertFalse(WeatherRules.needsRefresh(weather(), t0.plusMinutes(29)))
    }

    @Test
    fun `a reading 30 minutes old is due for a refresh`() {
        assertTrue(WeatherRules.needsRefresh(weather(), t0.plusMinutes(30)))
    }

    @Test
    fun `no reading at all needs a refresh`() {
        assertTrue(WeatherRules.needsRefresh(null, t0))
    }

    @Test
    fun `a reading is not stale until 3 hours old`() {
        assertFalse(WeatherRules.isStale(weather(), t0.plusHours(3)))
        assertTrue(WeatherRules.isStale(weather(), t0.plusHours(3).plusMinutes(1)))
    }

    @Test
    fun `a clock that went backwards never makes a reading stale or due`() {
        assertFalse(WeatherRules.isStale(weather(), t0.minusHours(5)))
        assertFalse(WeatherRules.needsRefresh(weather(), t0.minusHours(5)))
    }

    @Test
    fun `withStaleness marks old readings and leaves fresh ones untouched`() {
        val fresh = weather()
        assertEquals(fresh, WeatherRules.withStaleness(fresh, t0.plusHours(1)))
        assertTrue(WeatherRules.withStaleness(fresh, t0.plusHours(4))!!.stale)
        assertEquals(null, WeatherRules.withStaleness(null, t0))
    }

    @Test
    fun `a failed refresh is retried after five minutes, not every tick`() {
        assertFalse(WeatherRules.mayRetry(lastAttempt = t0, now = t0.plusMinutes(4)))
        assertTrue(WeatherRules.mayRetry(lastAttempt = t0, now = t0.plusMinutes(5)))
        assertTrue(WeatherRules.mayRetry(lastAttempt = null, now = t0))
    }
}
