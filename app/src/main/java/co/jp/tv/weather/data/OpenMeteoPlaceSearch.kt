package co.jp.tv.weather.data

import co.jp.tv.weather.domain.PlaceSearch
import co.jp.tv.weather.domain.WeatherPlace
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException

/** Adapter: town search on the keyless Open-Meteo geocoding API, Spanish names, Colombia only. */
class OpenMeteoPlaceSearch(private val transport: HttpTransport) : PlaceSearch {
    override suspend fun search(query: String): List<WeatherPlace> {
        val name = query.trim()
        if (name.length < MIN_QUERY) return emptyList()
        val url = "https://geocoding-api.open-meteo.com/v1/search" +
            "?name=${URLEncoder.encode(name, "UTF-8")}&count=10&language=es&countryCode=CO"
        return try {
            OpenMeteoJson.parsePlaces(transport.get(url))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val MIN_QUERY = 2
    }
}
