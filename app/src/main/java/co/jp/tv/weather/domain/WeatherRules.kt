package co.jp.tv.weather.domain

import java.time.Duration
import java.time.LocalDateTime

/** When to ask the network again and when to stop trusting what we have. */
object WeatherRules {
    /** A reading this old is due for a refresh. */
    val REFRESH_AFTER: Duration = Duration.ofMinutes(30)

    /** Past this age the last reading is kept but marked [Weather.stale]. */
    val STALE_AFTER: Duration = Duration.ofHours(3)

    /** After a failed fetch, wait this long before trying again. */
    val RETRY_AFTER: Duration = Duration.ofMinutes(5)

    fun needsRefresh(weather: Weather?, now: LocalDateTime): Boolean =
        weather == null || age(weather, now) >= REFRESH_AFTER

    fun isStale(weather: Weather, now: LocalDateTime): Boolean = age(weather, now) > STALE_AFTER

    fun mayRetry(lastAttempt: LocalDateTime?, now: LocalDateTime): Boolean =
        lastAttempt == null || Duration.between(lastAttempt, now) >= RETRY_AFTER

    /** [weather] flagged stale when it is old enough; the same instance otherwise, so equal states stay equal. */
    fun withStaleness(weather: Weather?, now: LocalDateTime): Weather? = when {
        weather == null -> null
        isStale(weather, now) == weather.stale -> weather
        else -> weather.copy(stale = !weather.stale)
    }

    /** Never negative: a clock set back makes a reading look new, not old. */
    private fun age(weather: Weather, now: LocalDateTime): Duration =
        Duration.between(weather.updatedAt, now).let { if (it.isNegative) Duration.ZERO else it }
}
