package co.jp.tv.weather.domain

import java.time.LocalDateTime

/** Where the readings come from (the network). Throws when there is nothing to return. */
interface WeatherRemote {
    suspend fun fetch(place: WeatherPlace, now: LocalDateTime): Weather
}

/** Last reading per place, kept across restarts so the home has something to show offline. */
interface WeatherCache {
    suspend fun load(place: WeatherPlace): Weather?
    suspend fun save(place: WeatherPlace, weather: Weather)
}

/** Free-text town search for the place picker. */
interface PlaceSearch {
    /** Matches for [query], best first; empty when nothing matches or the network is down. */
    suspend fun search(query: String): List<WeatherPlace>

    object None : PlaceSearch {
        override suspend fun search(query: String): List<WeatherPlace> = emptyList()
    }
}

/** What the home asks for: a reading that is as fresh as the refresh policy allows. */
interface WeatherRepository {
    /** The last known reading for [place], however old, without touching the network. */
    suspend fun cached(place: WeatherPlace): Weather?

    /**
     * The reading for [place] at [now]: the cached one while it is under [WeatherRules.REFRESH_AFTER] old,
     * otherwise a fresh fetch. When the fetch fails the last known reading is returned (or null).
     */
    suspend fun current(place: WeatherPlace, now: LocalDateTime): Weather?

    object None : WeatherRepository {
        override suspend fun cached(place: WeatherPlace): Weather? = null
        override suspend fun current(place: WeatherPlace, now: LocalDateTime): Weather? = null
    }
}
