package co.jp.tv.weather.data

import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherPlace
import co.jp.tv.weather.domain.WmoCodes
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.roundToInt
import org.json.JSONException
import org.json.JSONObject

/** Reads the two Open-Meteo answers we use. Anything malformed is an [IOException], like a broken connection. */
internal object OpenMeteoJson {
    /** The `current` block of a forecast, stamped with [now] (the device clock, not the model's time). */
    fun parseWeather(json: String, place: WeatherPlace, now: LocalDateTime): Weather = try {
        val current = JSONObject(json).optJSONObject("current") ?: throw IOException("Forecast has no current block")
        if (!current.has("temperature_2m") || !current.has("weather_code")) throw IOException("Forecast is missing fields")
        Weather(
            condition = WmoCodes.condition(current.getInt("weather_code")),
            temperatureC = current.getDouble("temperature_2m").roundToInt(),
            isDay = current.optInt("is_day", 1) != 0,
            updatedAt = now,
            placeName = place.name,
        )
    } catch (e: JSONException) {
        throw IOException("Unreadable forecast", e)
    }

    /** Geocoding `results`, best first; entries without a name or with impossible coordinates are dropped. */
    fun parsePlaces(json: String): List<WeatherPlace> = try {
        val results = JSONObject(json).optJSONArray("results")
        buildList {
            for (i in 0 until (results?.length() ?: 0)) {
                val item = results?.optJSONObject(i) ?: continue
                val name = item.optString("name").trim()
                if (name.isEmpty() || !item.has("latitude") || !item.has("longitude")) continue
                val lat = item.getDouble("latitude")
                val lon = item.getDouble("longitude")
                if (lat !in -90.0..90.0 || lon !in -180.0..180.0) continue
                add(WeatherPlace(name, item.optString("admin1").trim().ifEmpty { null }, lat, lon))
            }
        }
    } catch (e: JSONException) {
        throw IOException("Unreadable search results", e)
    }
}
