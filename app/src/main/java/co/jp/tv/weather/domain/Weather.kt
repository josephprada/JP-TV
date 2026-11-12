package co.jp.tv.weather.domain

import java.time.LocalDateTime

/** One reading of the current weather at [placeName]. */
data class Weather(
    val condition: WeatherCondition,
    /** Whole degrees Celsius. */
    val temperatureC: Int,
    /** Daylight at the place when it was read; themes still follow the TV's own clock for their day/night scene. */
    val isDay: Boolean,
    /** Device wall-clock time of the fetch, comparable with the clock the home state ticks on. */
    val updatedAt: LocalDateTime,
    val placeName: String,
    /** Older than [WeatherRules.STALE_AFTER]: still shown, but the UI should not pretend it is current. */
    val stale: Boolean = false,
)
