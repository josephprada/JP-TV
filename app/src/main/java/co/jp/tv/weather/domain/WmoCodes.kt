package co.jp.tv.weather.domain

/** Maps the WMO weather interpretation codes Open-Meteo reports onto a [WeatherCondition]. */
object WmoCodes {
    /**
     * Snow has no icon of its own (it does not fall in Santander) and reads as [WeatherCondition.Cloudy],
     * and so does any code the table does not know.
     */
    fun condition(code: Int): WeatherCondition = when (code) {
        0, 1 -> WeatherCondition.Clear
        2 -> WeatherCondition.PartlyCloudy
        3 -> WeatherCondition.Cloudy
        45, 48 -> WeatherCondition.Fog
        51, 53, 55, 56, 57 -> WeatherCondition.Drizzle
        61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.Rain
        95, 96, 99 -> WeatherCondition.Storm
        else -> WeatherCondition.Cloudy
    }
}
