package co.jp.tv.weather.ui

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.PathParser
import co.jp.tv.weather.domain.WeatherCondition

/**
 * Line-icon geometry for every condition on a 24x24 grid (shapes after the Feather set, MIT), parsed once.
 * No emoji fonts: the TV's font set is unknown, and the icons must take the theme's colour.
 */
internal object WeatherGlyphs {
    /** One stroke of an icon, placed on the 24x24 grid by [scale] and an offset. */
    class Part(val path: Path, val scale: Float = 1f, val dx: Float = 0f, val dy: Float = 0f)

    private fun path(d: String): Path = PathParser().parsePathString(d).toPath()

    private val sun = path(
        "M7 12a5 5 0 1 0 10 0a5 5 0 1 0 -10 0 M12 1v2 M12 21v2 M4.22 4.22l1.42 1.42 M18.36 18.36l1.42 1.42 " +
            "M1 12h2 M21 12h2 M4.22 19.78l1.42-1.42 M18.36 5.64l1.42-1.42",
    )
    private val moon = path("M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z")
    private val cloud = path("M18 10h-1.26A8 8 0 1 0 9 20h9a5 5 0 0 0 0-10z")
    private val drizzle = path(
        "M8 19v2 M8 13v2 M16 19v2 M16 13v2 M12 21v2 M12 15v2 M20 16.58A5 5 0 0 0 18 7h-1.26A8 8 0 1 0 4 15.25",
    )
    private val rain = path("M16 13v8 M8 13v8 M12 15v8 M20 16.58A5 5 0 0 0 18 8h-1.26A8 8 0 1 0 4 15.25")
    private val storm = path("M19 16.9A5 5 0 0 0 18 7h-1.26a8 8 0 1 0-11.62 9 M13 11l-4 6h6l-4 6")
    private val fog = path("M4 6h13 M7 10h13 M4 14h13 M7 18h10")

    // Sun or moon peeking from behind a smaller cloud.
    private val smallSun = path("M6 8.5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0 M9 2.2v1.4 M2.4 8.5h1.4 M4.3 3.8l1 1 M13.7 3.8l-1 1 M4.3 13.2l1-1")
    private val smallMoon = path("M13 9.4A5.2 5.2 0 1 1 7.2 3.4 4 4 0 0 0 13 9.4z")

    private val byCondition: Map<Pair<WeatherCondition, Boolean>, List<Part>> = buildMap {
        for (day in listOf(true, false)) {
            put(WeatherCondition.Clear to day, listOf(Part(if (day) sun else moon)))
            put(WeatherCondition.PartlyCloudy to day, listOf(Part(if (day) smallSun else smallMoon), Part(cloud, 0.72f, 6.8f, 8.2f)))
            put(WeatherCondition.Cloudy to day, listOf(Part(cloud)))
            put(WeatherCondition.Fog to day, listOf(Part(fog)))
            put(WeatherCondition.Drizzle to day, listOf(Part(drizzle)))
            put(WeatherCondition.Rain to day, listOf(Part(rain)))
            put(WeatherCondition.Storm to day, listOf(Part(storm)))
        }
    }

    fun parts(condition: WeatherCondition, isDay: Boolean): List<Part> = byCondition.getValue(condition to isDay)
}
