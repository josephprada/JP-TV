package co.jp.tv.core.util

import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ClockFormatTest {
    @Test
    fun `time is 24h zero padded`() {
        assertEquals("21:40", ClockFormat.time(LocalDateTime.of(2025, 11, 8, 21, 40, 5)))
        assertEquals("07:05", ClockFormat.time(LocalDateTime.of(2025, 1, 2, 7, 5, 0)))
        assertEquals("00:00", ClockFormat.time(LocalDateTime.of(2025, 1, 2, 0, 0, 0)))
    }

    @Test
    fun `date is spanish uppercase weekday dot day and month`() {
        assertEquals("SÁBADO · 8 NOV", ClockFormat.date(LocalDateTime.of(2025, 11, 8, 21, 40)))
        assertEquals("MIÉRCOLES · 31 DIC", ClockFormat.date(LocalDateTime.of(2025, 12, 31, 0, 0)))
        assertEquals("LUNES · 1 ENE", ClockFormat.date(LocalDateTime.of(2029, 1, 1, 0, 0)))
    }

    @Test
    fun `colon is visible on even seconds and dim on odd seconds`() {
        assertEquals(true, ClockFormat.colonVisible(LocalDateTime.of(2025, 1, 1, 0, 0, 4)))
        assertEquals(false, ClockFormat.colonVisible(LocalDateTime.of(2025, 1, 1, 0, 0, 5)))
    }

    @Test
    fun `minute progress goes from 0 to just under 1`() {
        assertEquals(0f, ClockFormat.minuteProgress(LocalDateTime.of(2025, 1, 1, 0, 0, 0)))
        assertEquals(0.5f, ClockFormat.minuteProgress(LocalDateTime.of(2025, 1, 1, 0, 0, 30)))
    }
}
