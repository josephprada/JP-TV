package co.jp.tv.playlist.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MediaListingTest {
    @Test
    fun `video and image extensions are recognised in any case`() {
        assertEquals(MediaKind.VIDEO, MediaFileKinds.kindOf("Loop.MP4"))
        assertEquals(MediaKind.VIDEO, MediaFileKinds.kindOf("a.webm"))
        assertEquals(MediaKind.VIDEO, MediaFileKinds.kindOf("a.mkv"))
        assertEquals(MediaKind.IMAGE, MediaFileKinds.kindOf("a.jpg"))
        assertEquals(MediaKind.IMAGE, MediaFileKinds.kindOf("a.JPEG"))
        assertEquals(MediaKind.IMAGE, MediaFileKinds.kindOf("a.png"))
        assertEquals(MediaKind.IMAGE, MediaFileKinds.kindOf("a.webp"))
    }

    @Test
    fun `other files and hidden files are ignored`() {
        assertNull(MediaFileKinds.kindOf("notes.txt"))
        assertNull(MediaFileKinds.kindOf("noextension"))
        assertNull(MediaFileKinds.kindOf(".hidden.mp4"))
        assertNull(MediaFileKinds.kindOf("._a.jpg"))
    }

    private fun scanned(path: String, source: MediaSource) = ScannedFile(path, source)

    @Test
    fun `legacy files come first then fondos by name then removable storage`() {
        val items = MediaListing.build(
            listOf(
                scanned("/storage/ABCD-1234/JPTV/fondos/usb.mp4", MediaSource.USB),
                scanned("/sdcard/JPTV/fondos/b.mp4", MediaSource.FONDOS),
                scanned("/sdcard/JPTV/loop.mp4", MediaSource.LEGACY),
                scanned("/sdcard/JPTV/fondos/A.mp4", MediaSource.FONDOS),
                scanned("/sdcard/JPTV/fondos/notes.txt", MediaSource.FONDOS),
            ),
        )
        assertEquals(
            listOf("loop.mp4", "A.mp4", "b.mp4", "usb.mp4"),
            items.map { it.name },
        )
    }

    @Test
    fun `the same path is listed once`() {
        val items = MediaListing.build(
            listOf(
                scanned("/sdcard/JPTV/fondos/a.jpg", MediaSource.FONDOS),
                scanned("/sdcard/JPTV/fondos/a.jpg", MediaSource.FONDOS),
            ),
        )
        assertEquals(1, items.size)
        assertEquals("/sdcard/JPTV/fondos/a.jpg", items.single().id)
    }

    @Test
    fun `the library splits wallpaper videos wallpaper images and screensaver photos`() {
        val library = MediaLibraryState(
            items = MediaListing.build(
                listOf(
                    scanned("/sdcard/JPTV/loop.mp4", MediaSource.LEGACY),
                    scanned("/sdcard/JPTV/fondo.jpg", MediaSource.LEGACY),
                    scanned("/sdcard/JPTV/fondos/x.png", MediaSource.FONDOS),
                    scanned("/sdcard/JPTV/protector/p.jpg", MediaSource.PROTECTOR),
                ),
            ),
            loaded = true,
        )
        assertEquals(listOf("loop.mp4"), library.videos.map { it.name })
        assertEquals(listOf("fondo.jpg", "x.png"), library.images.map { it.name })
        assertEquals(listOf("p.jpg"), library.protector.map { it.name })
    }

    @Test
    fun `the legacy files alone make a one item list that follows the old behaviour`() {
        val library = MediaLibraryState(
            MediaListing.build(listOf(scanned("/sdcard/JPTV/loop.mp4", MediaSource.LEGACY))),
            loaded = true,
        )
        val resolved = PlaylistResolver.resolve(library.videos, PlaylistSettings.Default)
        assertEquals(1, resolved.size)
    }
}
