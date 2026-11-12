package co.jp.tv.weather.data

import android.util.Log
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCache
import co.jp.tv.weather.domain.WeatherPlace
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Adapter: the last reading lives in one small JSON file (it is a cache, so no DataStore and no backup). */
class FileWeatherCache(directory: File) : WeatherCache {
    private val file = File(directory, "weather-cache.json")

    override suspend fun load(place: WeatherPlace): Weather? = withContext(Dispatchers.IO) {
        try {
            if (file.isFile) WeatherCacheCodec.decode(file.readText(), place) else null
        } catch (e: IOException) {
            Log.w(TAG, "Could not read the weather cache", e)
            null
        }
    }

    override suspend fun save(place: WeatherPlace, weather: Weather) {
        withContext(Dispatchers.IO) {
            try {
                val tmp = File(file.parentFile, file.name + ".tmp")
                tmp.writeText(WeatherCacheCodec.encode(place, weather))
                if (!tmp.renameTo(file)) tmp.copyTo(file, overwrite = true)
            } catch (e: IOException) {
                Log.w(TAG, "Could not write the weather cache", e)
            }
        }
    }

    private companion object {
        const val TAG = "FileWeatherCache"
    }
}
