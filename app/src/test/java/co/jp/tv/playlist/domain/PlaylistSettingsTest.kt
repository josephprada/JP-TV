package co.jp.tv.playlist.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaylistSettingsTest {
    private fun video(path: String, source: MediaSource = MediaSource.FONDOS) =
        MediaItem(path, path.substringAfterLast('/'), MediaKind.VIDEO, source)

    private val a = video("/sdcard/JPTV/fondos/a.mp4")
    private val b = video("/sdcard/JPTV/fondos/b.mp4")
    private val c = video("/sdcard/JPTV/fondos/c.webm")
    private val found = listOf(a, b, c)

    @Test
    fun `nothing selected means every file found`() {
        assertEquals(found, PlaylistResolver.resolve(found, PlaylistSettings.Default))
    }

    @Test
    fun `a selection keeps the order of the files found`() {
        val settings = PlaylistSettings(selected = listOf(c.id, a.id))
        assertEquals(listOf(a, c), PlaylistResolver.resolve(found, settings))
    }

    @Test
    fun `files that vanished are skipped`() {
        val settings = PlaylistSettings(selected = listOf("/gone.mp4", b.id))
        assertEquals(listOf(b), PlaylistResolver.resolve(found, settings))
    }

    @Test
    fun `a selection of only missing files falls back to every file found`() {
        val settings = PlaylistSettings(selected = listOf("/gone.mp4"))
        assertEquals(found, PlaylistResolver.resolve(found, settings))
    }

    @Test
    fun `nothing found resolves to an empty list`() {
        assertEquals(emptyList<MediaItem>(), PlaylistResolver.resolve(emptyList(), PlaylistSettings(selected = listOf(a.id))))
    }

    @Test
    fun `toggling one chip off from the default selects the rest explicitly`() {
        val next = PlaylistSettings.Default.toggle(found, b.id)
        assertEquals(listOf(a.id, c.id), next.selected)
    }

    @Test
    fun `toggling the last missing chip back on returns to follow-all`() {
        val next = PlaylistSettings(selected = listOf(a.id, c.id)).toggle(found, b.id)
        assertEquals(emptyList<String>(), next.selected)
    }

    @Test
    fun `the last selected file cannot be switched off`() {
        val only = PlaylistSettings(selected = listOf(b.id))
        assertEquals(only, only.toggle(found, b.id))
    }

    @Test
    fun `a chip reads as selected when the default is active`() {
        assertEquals(true, PlaylistSettings.Default.isSelected(found, a.id))
        assertEquals(false, PlaylistSettings(selected = listOf(a.id)).isSelected(found, c.id))
    }

    @Test
    fun `interval and order names round trip and unknown ones fall back`() {
        RotationInterval.entries.forEach { assertEquals(it, RotationInterval.fromName(it.name)) }
        PlaylistOrder.entries.forEach { assertEquals(it, PlaylistOrder.fromName(it.name)) }
        assertEquals(RotationInterval.MIN_15, RotationInterval.fromName("bogus"))
        assertEquals(RotationInterval.MIN_15, RotationInterval.fromName(null))
        assertEquals(PlaylistOrder.SEQUENTIAL, PlaylistOrder.fromName(null))
    }

    @Test
    fun `defaults rotate every fifteen minutes in order`() {
        assertEquals(RotationInterval.MIN_15, PlaylistSettings.Default.interval)
        assertEquals(PlaylistOrder.SEQUENTIAL, PlaylistSettings.Default.order)
    }
}
