package co.jp.tv.themes.shared

import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DayPhaseTest {
    private fun at(hour: Int, minute: Int = 0) = LocalDateTime.of(2026, 11, 11, hour, minute)

    @Test
    fun `night runs from 18 00 until just before 06 00`() {
        assertEquals(DayPhase.NIGHT, DayPhase.of(at(0)))
        assertEquals(DayPhase.NIGHT, DayPhase.of(at(5, 59)))
        assertEquals(DayPhase.NIGHT, DayPhase.of(at(18)))
    }

    @Test
    fun `day runs from 06 00 until just before 18 00`() {
        assertEquals(DayPhase.DAY, DayPhase.of(at(6)))
        assertEquals(DayPhase.DAY, DayPhase.of(at(17, 59)))
    }

    @Test
    fun `isNight mirrors the phase`() {
        assertTrue(DayPhase.of(at(5)).isNight)
        assertFalse(DayPhase.of(at(6)).isNight)
        assertFalse(DayPhase.of(at(17)).isNight)
        assertTrue(DayPhase.of(at(18)).isNight)
        assertTrue(DayPhase.of(at(0)).isNight)
    }
}
