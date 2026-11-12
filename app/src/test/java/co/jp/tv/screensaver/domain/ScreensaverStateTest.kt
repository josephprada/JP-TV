package co.jp.tv.screensaver.domain

import app.cash.turbine.test
import co.jp.tv.playlist.domain.MediaItem
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.MediaLibraryState
import co.jp.tv.playlist.domain.MediaSource
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.preferences.domain.LauncherConfig
import java.time.LocalDateTime
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScreensaverStateTest {
    private val now = LocalDateTime.of(2025, 11, 8, 21, 40, 30)

    private fun image(path: String, source: MediaSource = MediaSource.FONDOS) =
        MediaItem(path, path.substringAfterLast('/'), MediaKind.IMAGE, source)

    private val wall = image("/sdcard/JPTV/fondos/wall.jpg")
    private val photo = image("/sdcard/JPTV/protector/photo.jpg", MediaSource.PROTECTOR)

    @Test
    fun `photos come from the protector folder when it has pictures`() {
        val library = MediaLibraryState(listOf(wall, photo), loaded = true)
        val state = ScreensaverState.of(now, LauncherConfig.Default, null, library)
        assertEquals(listOf(photo), state.photos)
    }

    @Test
    fun `without a protector folder the photos are the image playlist`() {
        val library = MediaLibraryState(listOf(wall), loaded = true)
        val state = ScreensaverState.of(now, LauncherConfig.Default, null, library)
        assertEquals(listOf(wall), state.photos)
    }

    @Test
    fun `the image playlist selection narrows the photos`() {
        val other = image("/sdcard/JPTV/fondos/other.jpg")
        val library = MediaLibraryState(listOf(wall, other), loaded = true)
        val config = LauncherConfig.Default.copy(imagePlaylist = PlaylistSettings(selected = listOf(other.id)))
        assertEquals(listOf(other), ScreensaverState.of(now, config, null, library).photos)
    }

    @Test
    fun `the state keeps the clock the weather and the config`() {
        val state = ScreensaverState.of(now, LauncherConfig.Default, null, MediaLibraryState.Empty)
        assertEquals(now, state.now)
        assertEquals(LauncherConfig.Default, state.config)
        assertTrue(state.photos.isEmpty())
        assertEquals(false, state.mediaLoaded)
    }

    @Test
    fun `the clock is read once a minute and aligned to the minute`() {
        assertEquals(30_000L, ScreensaverStates.msToNextMinute(LocalDateTime.of(2025, 1, 1, 10, 0, 30)))
        assertEquals(60_000L, ScreensaverStates.msToNextMinute(LocalDateTime.of(2025, 1, 1, 10, 0, 0)))
        assertEquals(1L, ScreensaverStates.msToNextMinute(LocalDateTime.of(2025, 1, 1, 10, 0, 59, 999_000_000)))
    }

    @Test
    fun `the flow combines config clock media and weather`() = runTest {
        val library = MediaLibraryState(listOf(photo), loaded = true)
        ScreensaverStates.flow(
            config = flowOf(LauncherConfig.Default.copy(brand = "Casa")),
            ticks = flowOf(now),
            library = flowOf(library),
            weather = flowOf(null),
        ).test {
            val state = awaitItem()
            assertEquals("Casa", state.config.brand)
            assertEquals(listOf(photo), state.photos)
            assertEquals(now, state.now)
            awaitComplete()
        }
    }

    @Test
    fun `nothing saved yet reads as the default config`() = runTest {
        ScreensaverStates.flow(flowOf(null), flowOf(now), flowOf(MediaLibraryState.Empty), flowOf(null)).test {
            assertEquals(LauncherConfig.Default, awaitItem().config)
            awaitComplete()
        }
    }
}
