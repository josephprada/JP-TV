package co.jp.tv.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import co.jp.tv.playlist.ui.LocalImageSource
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.screensaver.ui.ScreensaverContent
import co.jp.tv.screensaver.ui.ScreensaverSettingsScreen
import co.jp.tv.weather.domain.WeatherCondition
import org.junit.Test

/** The screensaver modes (one composable shared by the overlay and the DreamService) and its settings screen. */
class SaverScreens : ScreenshotTest() {
    private val accent = Color(LauncherConfig.Default.accent)

    @Test fun clockAndMessage() = shoot("screensaver-clock") {
        ScreensaverContent(
            ScreensaverMode.CLOCK,
            ScreenFixtures.saverState(screensaver = ScreensaverSettings(message = "Casa de JP")),
        )
    }

    @Test fun clockAndMessageTwelveHours() = shoot("screensaver-clock-12h") {
        val state = ScreenFixtures.saverState(weather = ScreenFixtures.weather(WeatherCondition.Rain, night = true, temperatureC = 19))
        ScreensaverContent(ScreensaverMode.CLOCK, state.copy(config = state.config.copy(hourFormat = co.jp.tv.core.util.ClockFormat.HourFormat.H12)))
    }

    @Test fun photos() = shoot("screensaver-photos") {
        CompositionLocalProvider(LocalImageSource provides ScreenFixtures.imageSource) {
            ScreensaverContent(ScreensaverMode.PHOTOS, ScreenFixtures.saverState())
        }
    }

    /** No pictures to show: the mode falls back to the clock instead of a black screen. */
    @Test fun photosWithoutPictures() = shoot("screensaver-photos-empty") {
        ScreensaverContent(ScreensaverMode.PHOTOS, ScreenFixtures.saverState(photos = emptyList()))
    }

    private fun ambient(themeId: String) = shoot("screensaver-ambient-$themeId") {
        ScreensaverContent(ScreensaverMode.AMBIENT, ScreenFixtures.saverState(themeId = themeId, weather = ScreenFixtures.weather(WeatherCondition.Clear)))
    }

    @Test fun ambientOceano() = ambient("oceano")
    @Test fun ambientCyberpunk() = ambient("cyberpunk")
    @Test fun ambientLofi() = ambient("lofi")
    @Test fun ambientGotham() = ambient("gotham")

    /** A theme without a scene of its own shows the chosen wallpaper. */
    @Test fun ambientDefault() = ambient("jptv")

    private fun settingsScreen(name: String, keys: List<Key> = emptyList(), settings: ScreensaverSettings = ScreensaverSettings.Default) =
        shoot(name, keys) {
            ScreensaverSettingsScreen(
                settings = settings,
                accent = accent,
                titleFontId = LauncherConfig.Default.titleFontId,
                onChange = {},
                onPreview = {},
                onOpenSystemSettings = {},
            )
        }

    @Test fun settingsDefault() = settingsScreen("screensaver-settings")
    @Test fun settingsPhotosMode() = settingsScreen("screensaver-settings-photos", settings = ScreensaverSettings(mode = ScreensaverMode.PHOTOS, idleMinutes = 5))

    /** Down from the first mode chip walks through the cards to the message box. */
    @Test fun settingsFocusIdle() = settingsScreen("screensaver-settings-focus-idle", List(2) { Key.DirectionDown })
    @Test fun settingsFocusButtons() = settingsScreen("screensaver-settings-focus-buttons", List(8) { Key.DirectionRight })
}
