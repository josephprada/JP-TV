package co.jp.tv.core.sound

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SoundVolumeTest {
    @Test
    fun `stepping up walks low medium high and stops`() {
        assertEquals(SoundVolume.MEDIUM, SoundVolume.LOW.step(+1))
        assertEquals(SoundVolume.HIGH, SoundVolume.MEDIUM.step(+1))
        assertEquals(SoundVolume.HIGH, SoundVolume.HIGH.step(+1))
    }

    @Test
    fun `stepping down walks back and stops`() {
        assertEquals(SoundVolume.MEDIUM, SoundVolume.HIGH.step(-1))
        assertEquals(SoundVolume.LOW, SoundVolume.MEDIUM.step(-1))
        assertEquals(SoundVolume.LOW, SoundVolume.LOW.step(-1))
    }

    @Test
    fun `gains grow with the level and stay below full scale`() {
        val gains = SoundVolume.entries.map { it.gain }
        assertEquals(gains.sorted(), gains)
        assertEquals(true, gains.all { it in 0.01f..0.95f })
    }
}
