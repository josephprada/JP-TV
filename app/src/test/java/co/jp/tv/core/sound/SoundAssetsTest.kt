package co.jp.tv.core.sound

import co.jp.tv.core.sound.data.SoundPoolUiSounds
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Every theme ships its four sounds as short, mono, 16-bit, 22050 Hz clips (see tools/sounds/synth.py). */
class SoundAssetsTest {
    private val themes = listOf(
        "jptv", "pixel", "terminal", "cartelera", "zen", "hud", "vinilo", "lofi",
        "gotham", "multiverso", "cyberpunk", "oceano",
    )
    private val raw = File("src/main/res/raw")

    @Test
    fun `resource names follow snd theme sound`() {
        assertEquals("snd_zen_move", SoundPoolUiSounds.resourceName("zen", UiSound.Move))
        assertEquals("snd_zen_select", SoundPoolUiSounds.resourceName("zen", UiSound.Select))
        assertEquals("snd_zen_back", SoundPoolUiSounds.resourceName("zen", UiSound.Back))
        assertEquals("snd_zen_control", SoundPoolUiSounds.resourceName("zen", UiSound.ControlFocus))
    }

    @Test
    fun `every theme has all four clips within the length and format limits`() {
        for (theme in themes) for (sound in UiSound.entries) {
            val name = SoundPoolUiSounds.resourceName(theme, sound)
            val file = File(raw, "$name.wav")
            assertTrue(file.isFile, "missing $name.wav")
            val header = ByteBuffer.wrap(file.readBytes()).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals(1, header.getShort(22).toInt(), "$name channels")
            assertEquals(22050, header.getInt(24), "$name sample rate")
            assertEquals(16, header.getShort(34).toInt(), "$name bits")
            val millis = header.getInt(40) / 2 * 1000L / 22050
            assertTrue(millis in 10..150, "$name is $millis ms")
        }
    }

    @Test
    fun `every registered theme id has a sound set`() {
        val ids = File("src/main/java/co/jp/tv/themes/ThemeRegistry.kt").readLines()
            .mapNotNull { Regex("""Entry\("(\w+)"\)""").find(it)?.groupValues?.get(1) }
        assertEquals(themes.sorted(), ids.sorted())
    }
}
