package co.jp.tv.weather.domain

import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

/** [WeatherRepository] that serves from memory, then the file cache, and only then asks [remote]. */
class CachedWeatherRepository(
    private val remote: WeatherRemote,
    private val cache: WeatherCache,
) : WeatherRepository {
    private var memoryPlace: WeatherPlace? = null
    private var memory: Weather? = null
    private var attemptPlace: WeatherPlace? = null
    private var lastAttempt: LocalDateTime? = null

    override suspend fun cached(place: WeatherPlace): Weather? {
        if (memoryPlace == place) return memory
        val loaded = cache.load(place)
        remember(place, loaded)
        return loaded
    }

    override suspend fun current(place: WeatherPlace, now: LocalDateTime): Weather? {
        val saved = cached(place)
        if (!WeatherRules.needsRefresh(saved, now)) return saved
        val previous = if (attemptPlace == place) lastAttempt else null
        if (!WeatherRules.mayRetry(previous, now)) return saved
        attemptPlace = place
        lastAttempt = now
        return try {
            val fresh = remote.fetch(place, now)
            cache.save(place, fresh)
            remember(place, fresh)
            fresh
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            saved
        }
    }

    private fun remember(place: WeatherPlace, weather: Weather?) {
        memoryPlace = place
        memory = weather
    }
}
