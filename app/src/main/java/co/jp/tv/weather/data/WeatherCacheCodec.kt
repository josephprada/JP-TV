package co.jp.tv.weather.data

import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCondition
import co.jp.tv.weather.domain.WeatherPlace
import java.time.LocalDateTime
import kotlin.math.abs
import org.json.JSONObject

/** JSON text of the one cached reading. A file for another place, or a damaged one, decodes to null. */
internal object WeatherCacheCodec {
    fun encode(place: WeatherPlace, weather: Weather): String = JSONObject()
        .put("lat", place.lat)
        .put("lon", place.lon)
        .put("condition", weather.condition.name)
        .put("temp", weather.temperatureC)
        .put("day", weather.isDay)
        .put("at", weather.updatedAt.toString())
        .put("place", weather.placeName)
        .toString()

    fun decode(text: String, place: WeatherPlace): Weather? = try {
        val json = JSONObject(text)
        if (abs(json.getDouble("lat") - place.lat) > EPSILON || abs(json.getDouble("lon") - place.lon) > EPSILON) {
            null
        } else {
            Weather(
                condition = WeatherCondition.valueOf(json.getString("condition")),
                temperatureC = json.getInt("temp"),
                isDay = json.getBoolean("day"),
                updatedAt = LocalDateTime.parse(json.getString("at")),
                placeName = json.getString("place"),
            )
        }
    } catch (e: Exception) {
        null
    }

    private const val EPSILON = 1e-6
}
