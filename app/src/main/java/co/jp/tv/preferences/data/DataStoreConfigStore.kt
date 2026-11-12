package co.jp.tv.preferences.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import co.jp.tv.core.sound.SoundVolume
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.preferences.domain.ConfigStore
import co.jp.tv.screensaver.domain.IdleRules
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.weather.domain.WeatherPlaceCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Adapter: Jetpack DataStore (Preferences) for the full [LauncherConfig]. */
class DataStoreConfigStore(private val dataStore: DataStore<Preferences>) : ConfigStore {

    override val config: Flow<LauncherConfig?> = dataStore.data.map { it.toConfig() }

    override suspend fun update(transform: (LauncherConfig?) -> LauncherConfig) {
        dataStore.edit { prefs ->
            val next = transform(prefs.toConfig())
            prefs[SAVED] = true
            prefs[BRAND] = next.brand
            prefs[ACCENT] = next.accent
            prefs[TITLE_FONT] = next.titleFontId
            prefs[WALLPAPER] = next.wallpaperId
            prefs[THEME] = next.themeId
            prefs[APP_ORDER] = next.appOrder.joinToString(SEPARATOR)
            prefs[HIDDEN_APPS] = next.hiddenApps
            prefs[HOUR_FORMAT] = next.hourFormat.name
            prefs[WALLPAPER_DIM] = next.wallpaperDim
            prefs[PAUSE_IN_APPS] = next.pauseWallpaperInApps
            prefs[WEATHER_PLACE] = WeatherPlaceCodec.encode(next.weatherPlace)
            prefs[SOUNDS_ENABLED] = next.soundsEnabled
            prefs[SOUND_VOLUME] = next.soundVolume.name
            prefs[HOME_OVERRIDE] = next.homeOverrideEnabled
            prefs.write(VIDEO_PLAYLIST, next.videoPlaylist)
            prefs.write(IMAGE_PLAYLIST, next.imagePlaylist)
            prefs[SCREENSAVER_MODE] = next.screensaver.mode.name
            prefs[SCREENSAVER_IDLE] = next.screensaver.idleMinutes
            prefs[SCREENSAVER_MESSAGE] = next.screensaver.message
        }
    }

    private fun androidx.datastore.preferences.core.MutablePreferences.write(keys: PlaylistKeys, settings: PlaylistSettings) {
        this[keys.selected] = settings.selected.joinToString(SEPARATOR)
        this[keys.interval] = settings.interval.name
        this[keys.order] = settings.order.name
    }

    private fun Preferences.read(keys: PlaylistKeys) = PlaylistSettings(
        selected = this[keys.selected].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
        interval = RotationInterval.fromName(this[keys.interval]),
        order = PlaylistOrder.fromName(this[keys.order]),
    )

    private fun Preferences.toConfig(): LauncherConfig? =
        if (this[SAVED] != true) {
            null
        } else {
            LauncherConfig(
                brand = this[BRAND] ?: LauncherConfig.DEFAULT_BRAND,
                accent = this[ACCENT] ?: LauncherConfig.Default.accent,
                titleFontId = this[TITLE_FONT] ?: LauncherConfig.Default.titleFontId,
                wallpaperId = this[WALLPAPER] ?: LauncherConfig.DEFAULT_WALLPAPER_ID,
                themeId = this[THEME] ?: LauncherConfig.DEFAULT_THEME_ID,
                appOrder = this[APP_ORDER].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
                hiddenApps = this[HIDDEN_APPS].orEmpty(),
                hourFormat = HourFormat.entries.firstOrNull { it.name == this[HOUR_FORMAT] } ?: HourFormat.H24,
                wallpaperDim = this[WALLPAPER_DIM] ?: 0,
                pauseWallpaperInApps = this[PAUSE_IN_APPS] ?: true,
                weatherPlace = WeatherPlaceCodec.decode(this[WEATHER_PLACE]),
                soundsEnabled = this[SOUNDS_ENABLED] ?: true,
                soundVolume = SoundVolume.entries.firstOrNull { it.name == this[SOUND_VOLUME] } ?: SoundVolume.MEDIUM,
                homeOverrideEnabled = this[HOME_OVERRIDE] ?: false,
                videoPlaylist = read(VIDEO_PLAYLIST),
                imagePlaylist = read(IMAGE_PLAYLIST),
                screensaver = ScreensaverSettings(
                    mode = ScreensaverMode.fromName(this[SCREENSAVER_MODE]),
                    idleMinutes = IdleRules.snap(this[SCREENSAVER_IDLE] ?: IdleRules.DEFAULT_MINUTES),
                    message = ScreensaverSettings.sanitizeMessage(this[SCREENSAVER_MESSAGE] ?: ScreensaverSettings.DEFAULT_MESSAGE),
                ),
            )
        }

    private companion object {
        const val SEPARATOR = "\n"
        val SAVED = booleanPreferencesKey("saved")
        val BRAND = stringPreferencesKey("brand")
        val ACCENT = longPreferencesKey("accent")
        val TITLE_FONT = stringPreferencesKey("title_font")
        val WALLPAPER = stringPreferencesKey("wallpaper")
        val THEME = stringPreferencesKey("theme")
        val APP_ORDER = stringPreferencesKey("app_order")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")

        /** Absent on installs from before the setting existed; reads as 24-hour. */
        val WALLPAPER_DIM = intPreferencesKey("wallpaper_dim")
        val PAUSE_IN_APPS = booleanPreferencesKey("pause_wallpaper_in_apps")
        val HOUR_FORMAT = stringPreferencesKey("hour_format")

        /** Absent before the weather feature existed, or unreadable: reads as Bucaramanga. */
        val WEATHER_PLACE = stringPreferencesKey("weather_place")

        /** Absent before the sounds existed: reads as on, at medium volume. */
        val SOUNDS_ENABLED = booleanPreferencesKey("sounds_enabled")
        val SOUND_VOLUME = stringPreferencesKey("sound_volume")

        /** Absent before the home override existed: reads as off, so nothing changes until the user opts in. */
        val HOME_OVERRIDE = booleanPreferencesKey("home_override")

        /** Absent before playlists existed: nothing selected (every file found, legacy file included), 15 min, in order. */
        class PlaylistKeys(prefix: String) {
            val selected = stringPreferencesKey("${prefix}_playlist_selected")
            val interval = stringPreferencesKey("${prefix}_playlist_interval")
            val order = stringPreferencesKey("${prefix}_playlist_order")
        }

        val VIDEO_PLAYLIST = PlaylistKeys("video")
        val IMAGE_PLAYLIST = PlaylistKeys("image")

        /** Absent before the screensaver existed: the clock after ten idle minutes. */
        val SCREENSAVER_MODE = stringPreferencesKey("screensaver_mode")
        val SCREENSAVER_IDLE = intPreferencesKey("screensaver_idle_minutes")
        val SCREENSAVER_MESSAGE = stringPreferencesKey("screensaver_message")
    }
}
