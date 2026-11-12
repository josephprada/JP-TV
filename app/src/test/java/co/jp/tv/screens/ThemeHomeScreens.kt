package co.jp.tv.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import co.jp.tv.themes.ThemeRegistry
import co.jp.tv.weather.domain.Weather
import co.jp.tv.weather.domain.WeatherCondition
import co.jp.tv.wallpaper.WallpaperRegistry
import java.time.LocalDateTime
import org.junit.Test

/** One Home render per theme, over the default wallpaper, like MainActivity composes it. */
class ThemeHomeScreens : ScreenshotTest() {
    private fun home(
        themeId: String,
        suffix: String = "",
        at: LocalDateTime = ScreenFixtures.now,
        keys: List<Key> = emptyList(),
        weather: Weather? = null,
    ) = shoot("home-$themeId$suffix", keys) {
        val state = ScreenFixtures.homeState(themeId, at = at, weather = weather)
        WallpaperRegistry.byId(state.config.wallpaperId)
            .Render(Color(state.config.accent), paused = true, modifier = Modifier)
        ThemeRegistry.byId(themeId).Home(state, ScreenFixtures.actions)
    }

    private fun homeDay(themeId: String) = home(themeId, "-day", ScreenFixtures.dayNow)

    @Test fun jptv() = home("jptv")
    @Test fun pixel() = home("pixel")
    @Test fun terminal() = home("terminal")
    @Test fun cartelera() = home("cartelera")
    @Test fun zen() = home("zen")
    @Test fun hud() = home("hud")
    @Test fun vinilo() = home("vinilo")
    @Test fun lofi() = home("lofi")
    @Test fun gotham() = home("gotham")
    @Test fun multiverso() = home("multiverso")
    @Test fun cyberpunk() = home("cyberpunk")
    @Test fun oceano() = home("oceano")

    // Themes that follow DayPhase (lofi, cyberpunk, oceano, gotham); the rest ignore the hour.
    @Test fun lofiDay() = homeDay("lofi")
    @Test fun cyberpunkDay() = homeDay("cyberpunk")
    @Test fun oceanoDay() = homeDay("oceano")
    @Test fun gothamDay() = homeDay("gotham")

    // Weather: the badge in each header, and the scenes that react to it.
    @Test fun jptvCloudy() = home("jptv", "-weather", weather = ScreenFixtures.weather(WeatherCondition.PartlyCloudy, temperatureC = 24))

    private fun w(c: WeatherCondition, night: Boolean = true) = ScreenFixtures.weather(c, night = night)

    @Test fun lofiRainNight() = home("lofi", "-rain", weather = w(WeatherCondition.Rain))
    @Test fun lofiStormNight() = home("lofi", "-storm", weather = w(WeatherCondition.Storm))
    @Test fun lofiRainDay() = home("lofi", "-rain-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Rain, night = false))
    @Test fun lofiFogDay() = home("lofi", "-fog-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Fog, night = false))
    @Test fun lofiClearNight() = home("lofi", "-clear", weather = w(WeatherCondition.Clear))

    @Test fun cyberpunkRain() = home("cyberpunk", "-rain", weather = w(WeatherCondition.Rain))
    @Test fun cyberpunkStorm() = home("cyberpunk", "-storm", weather = w(WeatherCondition.Storm))
    @Test fun cyberpunkFog() = home("cyberpunk", "-fog", weather = w(WeatherCondition.Fog))
    @Test fun cyberpunkClearDay() = home("cyberpunk", "-clear-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Clear, night = false))

    @Test fun oceanoStorm() = home("oceano", "-storm", weather = w(WeatherCondition.Storm))
    @Test fun oceanoRainDay() = home("oceano", "-rain-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Rain, night = false))
    @Test fun oceanoPartlyCloudyDay() = home("oceano", "-partly-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.PartlyCloudy, night = false))

    @Test fun gothamRain() = home("gotham", "-rain", weather = w(WeatherCondition.Rain))
    @Test fun gothamStorm() = home("gotham", "-storm", weather = w(WeatherCondition.Storm))
    @Test fun gothamClearNight() = home("gotham", "-clear", weather = w(WeatherCondition.Clear))
    @Test fun gothamFogDay() = home("gotham", "-fog-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Fog, night = false))
    @Test fun gothamRainDay() = home("gotham", "-rain-day", ScreenFixtures.dayNow, weather = w(WeatherCondition.Rain, night = false))

    @Test fun registryMatchesTests() {
        // Keeps this file honest when a theme is added to the registry.
        val covered = setOf(
            "jptv", "pixel", "terminal", "cartelera", "zen", "hud", "vinilo", "lofi",
            "gotham", "multiverso", "cyberpunk", "oceano",
        )
        check(ThemeRegistry.all.map { it.id }.toSet() == covered) { "Add the new theme to ThemeHomeScreens" }
    }
}
