package co.jp.tv.weather.data

import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherPlace
import co.jp.tv.weather.domain.WeatherRemote
import java.time.LocalDateTime
import java.util.Locale

/** Adapter: current conditions from the keyless Open-Meteo forecast API. */
class OpenMeteoWeatherRemote(private val transport: HttpTransport) : WeatherRemote {
    override suspend fun fetch(place: WeatherPlace, now: LocalDateTime): Weather {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=${coordinate(place.lat)}&longitude=${coordinate(place.lon)}" +
            "&current=temperature_2m,weather_code,is_day&timezone=auto"
        return OpenMeteoJson.parseWeather(transport.get(url), place, now)
    }

    private fun coordinate(value: Double) = String.format(Locale.ROOT, "%.4f", value)
}
