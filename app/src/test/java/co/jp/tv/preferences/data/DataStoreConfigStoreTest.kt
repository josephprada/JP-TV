package co.jp.tv.preferences.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import co.jp.tv.core.sound.SoundVolume
import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.playlist.domain.PlaylistOrder
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.preferences.domain.LauncherConfig
import co.jp.tv.screensaver.domain.ScreensaverMode
import co.jp.tv.screensaver.domain.ScreensaverSettings
import co.jp.tv.weather.domain.WeatherPlace
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DataStoreConfigStoreTest {
    @TempDir lateinit var dir: File

    private fun store(name: String = "prefs") = DataStoreConfigStore(
        PreferenceDataStoreFactory.create(produceFile = { File(dir, "$name.preferences_pb") }),
    )

    @Test
    fun `nothing saved reads as null`() = runTest {
        assertNull(store().config.first())
    }

    @Test
    fun `saved order and hidden apps round trip`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(appOrder = listOf("b", "a", "c"), hiddenApps = setOf("c")) }
        val read = store.config.first()
        assertEquals(listOf("b", "a", "c"), read?.appOrder)
        assertEquals(setOf("c"), read?.hiddenApps)
    }

    @Test
    fun `appearance fields round trip`() = runTest {
        val store = store()
        val written = LauncherConfig.Default.copy(
            brand = "Casa",
            accent = 0xFF3D8BFD,
            titleFontId = "bebas_neue",
            wallpaperId = "embers",
            themeId = "jptv",
            appOrder = listOf("a"),
        )
        store.update { written }
        assertEquals(written, store.config.first())
    }

    @Test
    fun `12 hour clock format round trips`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(hourFormat = HourFormat.H12) }
        assertEquals(HourFormat.H12, store.config.first()?.hourFormat)
        store.update { it!!.copy(hourFormat = HourFormat.H24) }
        assertEquals(HourFormat.H24, store.config.first()?.hourFormat)
    }

    @Test
    fun `a saved config without a clock format reads as 24 hour`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("brand")] = "Casa"
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals("Casa", read?.brand)
        assertEquals(HourFormat.H24, read?.hourFormat)
    }

    @Test
    fun `an empty saved order is still a saved config`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default }
        assertEquals(LauncherConfig.Default, store.config.first())
    }

    @Test
    fun `update receives the previous value`() = runTest {
        val store = store()
        store.update { LauncherConfig(appOrder = listOf("a")) }
        store.update { it!!.copy(hiddenApps = setOf("a")) }
        assertEquals(LauncherConfig(appOrder = listOf("a"), hiddenApps = setOf("a")), store.config.first())
    }

    @Test
    fun `wallpaper dim and pause in apps round trip`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(wallpaperDim = 40, pauseWallpaperInApps = false) }
        val read = store.config.first()
        assertEquals(40, read?.wallpaperDim)
        assertEquals(false, read?.pauseWallpaperInApps)
    }

    @Test
    fun `a saved config from before the wallpaper settings reads as no dim and pausing`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy2.preferences_pb") })
        dataStore.edit { prefs -> prefs[booleanPreferencesKey("saved")] = true }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals(0, read?.wallpaperDim)
        assertEquals(true, read?.pauseWallpaperInApps)
    }

    @Test
    fun `the weather place round trips`() = runTest {
        val store = store()
        val giron = WeatherPlace.QuickPicks.first { it.name == "Girón" }
        store.update { LauncherConfig.Default.copy(weatherPlace = giron) }
        assertEquals(giron, store.config.first()?.weatherPlace)
    }

    @Test
    fun `a saved config from before the weather feature reads as Bucaramanga`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy3.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("brand")] = "Casa"
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals("Casa", read?.brand)
        assertEquals(WeatherPlace.Default, read?.weatherPlace)
    }

    @Test
    fun `a damaged weather place reads as Bucaramanga and keeps the rest of the config`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "damaged.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("brand")] = "Casa"
            prefs[stringPreferencesKey("weather_place")] = "not a place"
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals("Casa", read?.brand)
        assertEquals(WeatherPlace.Default, read?.weatherPlace)
    }

    @Test
    fun `sound settings round trip`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(soundsEnabled = false, soundVolume = SoundVolume.HIGH) }
        val read = store.config.first()
        assertEquals(false, read?.soundsEnabled)
        assertEquals(SoundVolume.HIGH, read?.soundVolume)
    }

    @Test
    fun `a saved config without sound settings reads as sounds on at medium`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy-sound.preferences_pb") })
        dataStore.edit { prefs -> prefs[booleanPreferencesKey("saved")] = true }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals(true, read?.soundsEnabled)
        assertEquals(SoundVolume.MEDIUM, read?.soundVolume)
    }

    @Test
    fun `an unknown stored volume reads as medium`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "bad-volume.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("sound_volume")] = "LOUDEST"
        }
        assertEquals(SoundVolume.MEDIUM, DataStoreConfigStore(dataStore).config.first()?.soundVolume)
    }

    @Test
    fun `the home override toggle round trips`() = runTest {
        val store = store()
        store.update { LauncherConfig.Default.copy(homeOverrideEnabled = true) }
        assertEquals(true, store.config.first()?.homeOverrideEnabled)
    }

    @Test
    fun `a saved config from before the home override reads as off`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy-home.preferences_pb") })
        dataStore.edit { prefs -> prefs[booleanPreferencesKey("saved")] = true }
        assertEquals(false, DataStoreConfigStore(dataStore).config.first()?.homeOverrideEnabled)
    }

    @Test
    fun `playlist and screensaver settings round trip`() = runTest {
        val store = store()
        val written = LauncherConfig.Default.copy(
            videoPlaylist = PlaylistSettings(listOf("/sdcard/JPTV/fondos/a.mp4", "/storage/ABCD-1234/JPTV/fondos/b.mkv"), RotationInterval.MIN_30, PlaylistOrder.SHUFFLE),
            imagePlaylist = PlaylistSettings(emptyList(), RotationInterval.ON_HOME_RETURN, PlaylistOrder.SEQUENTIAL),
            screensaver = ScreensaverSettings(ScreensaverMode.RANDOM, idleMinutes = 30, message = "Casa de JP"),
        )
        store.update { written }
        assertEquals(written, store.config.first())
    }

    @Test
    fun `a saved config from before playlists keeps the legacy single file behaviour`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "legacy-playlists.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("wallpaper")] = "video"
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals("video", read?.wallpaperId)
        // Nothing selected means "every file found", which includes /sdcard/JPTV/loop.mp4.
        assertEquals(PlaylistSettings.Default, read?.videoPlaylist)
        assertEquals(PlaylistSettings.Default, read?.imagePlaylist)
        assertEquals(ScreensaverSettings.Default, read?.screensaver)
    }

    @Test
    fun `unknown stored playlist and screensaver values fall back to the defaults`() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { File(dir, "bad-playlists.preferences_pb") })
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("saved")] = true
            prefs[stringPreferencesKey("video_playlist_interval")] = "EVERY_BLUE_MOON"
            prefs[stringPreferencesKey("video_playlist_order")] = "???"
            prefs[stringPreferencesKey("screensaver_mode")] = "DISCO"
            prefs[intPreferencesKey("screensaver_idle_minutes")] = 11
        }
        val read = DataStoreConfigStore(dataStore).config.first()
        assertEquals(RotationInterval.MIN_15, read?.videoPlaylist?.interval)
        assertEquals(PlaylistOrder.SEQUENTIAL, read?.videoPlaylist?.order)
        assertEquals(ScreensaverMode.CLOCK, read?.screensaver?.mode)
        assertEquals(10, read?.screensaver?.idleMinutes)
    }
}
