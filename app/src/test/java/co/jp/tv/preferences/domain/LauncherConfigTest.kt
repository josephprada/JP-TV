package co.jp.tv.preferences.domain

import co.jp.tv.core.sound.SoundVolume
import co.jp.tv.playlist.domain.MediaKind
import co.jp.tv.playlist.domain.PlaylistSettings
import co.jp.tv.playlist.domain.RotationInterval
import co.jp.tv.screensaver.domain.ScreensaverSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LauncherConfigTest {
    @Test
    fun `dim moves in ten point steps`() {
        assertEquals(10, LauncherConfig.stepDim(0, +1))
        assertEquals(30, LauncherConfig.stepDim(40, -1))
    }

    @Test
    fun `dim never leaves the allowed range`() {
        assertEquals(0, LauncherConfig.stepDim(0, -1))
        assertEquals(LauncherConfig.MAX_DIM, LauncherConfig.stepDim(LauncherConfig.MAX_DIM, +1))
    }

    @Test
    fun `an off-grid stored dim snaps to the next step`() {
        assertEquals(40, LauncherConfig.stepDim(35, +1))
        assertEquals(30, LauncherConfig.stepDim(35, -1))
    }

    @Test
    fun `brand is trimmed and capped`() {
        assertEquals("Casa", LauncherConfig.sanitizeBrand("  Casa  "))
        assertEquals("A".repeat(LauncherConfig.MAX_BRAND), LauncherConfig.sanitizeBrand("A".repeat(40)))
    }

    @Test
    fun `a blank brand falls back to the default`() {
        assertEquals(LauncherConfig.DEFAULT_BRAND, LauncherConfig.sanitizeBrand("   "))
        assertEquals(LauncherConfig.DEFAULT_BRAND, LauncherConfig.sanitizeBrand(""))
    }

    @Test
    fun `sounds are on at a moderate volume by default`() {
        assertEquals(true, LauncherConfig.Default.soundsEnabled)
        assertEquals(SoundVolume.MEDIUM, LauncherConfig.Default.soundVolume)
    }

    @Test
    fun `using JP-TV as home is opt-in`() {
        assertEquals(false, LauncherConfig.Default.homeOverrideEnabled)
    }

    @Test
    fun `playlists default to every file found rotating every fifteen minutes`() {
        assertEquals(PlaylistSettings.Default, LauncherConfig.Default.videoPlaylist)
        assertEquals(PlaylistSettings.Default, LauncherConfig.Default.imagePlaylist)
    }

    @Test
    fun `the screensaver is the clock after ten idle minutes by default`() {
        assertEquals(ScreensaverSettings.Default, LauncherConfig.Default.screensaver)
    }

    @Test
    fun `the playlist settings of a kind are found by kind`() {
        val config = LauncherConfig.Default.copy(
            videoPlaylist = PlaylistSettings(interval = RotationInterval.MIN_5),
            imagePlaylist = PlaylistSettings(interval = RotationInterval.NEVER),
        )
        assertEquals(RotationInterval.MIN_5, config.playlist(MediaKind.VIDEO).interval)
        assertEquals(RotationInterval.NEVER, config.playlist(MediaKind.IMAGE).interval)
        assertEquals(RotationInterval.NEVER, config.withPlaylist(MediaKind.IMAGE, PlaylistSettings(interval = RotationInterval.NEVER)).imagePlaylist.interval)
    }
}
