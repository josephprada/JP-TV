package co.jp.tv.core.util

import co.jp.tv.core.util.ClockFormat.HourFormat
import co.jp.tv.core.util.ClockFormat.TimeParts
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ClockFormatTest {
    private fun at(hour: Int, minute: Int) = LocalDateTime.of(2025, 11, 8, hour, minute, 7)

    @Test
    fun `time is 24h zero padded`() {
        assertEquals("21:40", ClockFormat.time(LocalDateTime.of(2025, 11, 8, 21, 40, 5), HourFormat.H24))
        assertEquals("07:05", ClockFormat.time(LocalDateTime.of(2025, 1, 2, 7, 5, 0), HourFormat.H24))
        assertEquals("00:00", ClockFormat.time(LocalDateTime.of(2025, 1, 2, 0, 0, 0), HourFormat.H24))
    }

    @Test
    fun `24h time keeps two digit hours and minutes`() {
        assertEquals("00:05", ClockFormat.time(at(0, 5), HourFormat.H24))
        assertEquals("09:40", ClockFormat.time(at(9, 40), HourFormat.H24))
        assertEquals("12:00", ClockFormat.time(at(12, 0), HourFormat.H24))
        assertEquals("12:30", ClockFormat.time(at(12, 30), HourFormat.H24))
        assertEquals("21:40", ClockFormat.time(at(21, 40), HourFormat.H24))
    }

    @Test
    fun `12h time has an unpadded hour from 1 to 12 and an AM or PM suffix`() {
        assertEquals("12:05 AM", ClockFormat.time(at(0, 5), HourFormat.H12))
        assertEquals("9:40 AM", ClockFormat.time(at(9, 40), HourFormat.H12))
        assertEquals("12:00 PM", ClockFormat.time(at(12, 0), HourFormat.H12))
        assertEquals("12:30 PM", ClockFormat.time(at(12, 30), HourFormat.H12))
        assertEquals("9:40 PM", ClockFormat.time(at(21, 40), HourFormat.H12))
    }

    @Test
    fun `24h parts have no meridiem`() {
        assertEquals(TimeParts("00", "05", null), ClockFormat.parts(at(0, 5), HourFormat.H24))
        assertEquals(TimeParts("09", "40", null), ClockFormat.parts(at(9, 40), HourFormat.H24))
        assertEquals(TimeParts("12", "00", null), ClockFormat.parts(at(12, 0), HourFormat.H24))
        assertEquals(TimeParts("12", "30", null), ClockFormat.parts(at(12, 30), HourFormat.H24))
        assertEquals(TimeParts("21", "40", null), ClockFormat.parts(at(21, 40), HourFormat.H24))
    }

    @Test
    fun `12h parts split hour, minutes and meridiem`() {
        assertEquals(TimeParts("12", "05", "AM"), ClockFormat.parts(at(0, 5), HourFormat.H12))
        assertEquals(TimeParts("9", "40", "AM"), ClockFormat.parts(at(9, 40), HourFormat.H12))
        assertEquals(TimeParts("12", "00", "PM"), ClockFormat.parts(at(12, 0), HourFormat.H12))
        assertEquals(TimeParts("12", "30", "PM"), ClockFormat.parts(at(12, 30), HourFormat.H12))
        assertEquals(TimeParts("9", "40", "PM"), ClockFormat.parts(at(21, 40), HourFormat.H12))
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
