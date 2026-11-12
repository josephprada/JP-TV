package co.jp.tv.core.sound

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SoundThrottleTest {
    private val throttle = SoundThrottle()

    @Test
    fun `the first move plays at full volume`() {
        assertEquals(1f, throttle.gainFor(UiSound.Move, nowMs = 1_000))
    }

    @Test
    fun `moves closer than the minimum gap are dropped`() {
        throttle.gainFor(UiSound.Move, 1_000)
        assertNull(throttle.gainFor(UiSound.Move, 1_030))
    }

    @Test
    fun `a dropped move does not push the next one back`() {
        throttle.gainFor(UiSound.Move, 1_000)
        throttle.gainFor(UiSound.Move, 1_030)
        assertEquals(SoundThrottle.REPEAT_GAIN, throttle.gainFor(UiSound.Move, 1_065))
    }

    @Test
    fun `a held direction key plays quieter repeats`() {
        throttle.gainFor(UiSound.Move, 1_000)
        assertEquals(SoundThrottle.REPEAT_GAIN, throttle.gainFor(UiSound.Move, 1_100))
    }

    @Test
    fun `a move after a pause is back to full volume`() {
        throttle.gainFor(UiSound.Move, 1_000)
        assertEquals(1f, throttle.gainFor(UiSound.Move, 1_000 + SoundThrottle.REPEAT_WINDOW_MS + 1))
    }

    @Test
    fun `select and back are never attenuated but a double fire is dropped`() {
        assertEquals(1f, throttle.gainFor(UiSound.Select, 2_000))
        assertNull(throttle.gainFor(UiSound.Select, 2_010))
        assertEquals(1f, throttle.gainFor(UiSound.Select, 2_500))
        assertEquals(1f, throttle.gainFor(UiSound.Back, 2_501))
    }

    @Test
    fun `different sounds do not throttle each other`() {
        throttle.gainFor(UiSound.Move, 3_000)
        assertEquals(1f, throttle.gainFor(UiSound.ControlFocus, 3_001))
    }
}
