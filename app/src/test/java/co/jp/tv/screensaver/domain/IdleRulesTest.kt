package co.jp.tv.screensaver.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IdleRulesTest {
    private val minute = 60_000L

    @Test
    fun `the saver starts once the idle time has passed on the home`() {
        assertFalse(IdleRules.shouldStart(lastInputMs = 0, nowMs = 10 * minute - 1, minutes = 10, homeVisible = true, saverShowing = false))
        assertTrue(IdleRules.shouldStart(lastInputMs = 0, nowMs = 10 * minute, minutes = 10, homeVisible = true, saverShowing = false))
    }

    @Test
    fun `never means never`() {
        assertFalse(IdleRules.shouldStart(0, 9_999 * minute, minutes = 0, homeVisible = true, saverShowing = false))
        assertNull(IdleRules.msUntilStart(0, 0, minutes = 0))
    }

    @Test
    fun `it does not start away from the home or while already showing`() {
        assertFalse(IdleRules.shouldStart(0, 99 * minute, 10, homeVisible = false, saverShowing = false))
        assertFalse(IdleRules.shouldStart(0, 99 * minute, 10, homeVisible = true, saverShowing = true))
    }

    @Test
    fun `any key press resets the countdown`() {
        // Pressed at minute 8: at minute 12 only 4 minutes of silence have passed.
        assertFalse(IdleRules.shouldStart(8 * minute, 12 * minute, 10, homeVisible = true, saverShowing = false))
        assertEquals(6 * minute, IdleRules.msUntilStart(8 * minute, 12 * minute, 10))
    }

    @Test
    fun `the wait is never negative and a clock going backwards restarts the count`() {
        assertEquals(0L, IdleRules.msUntilStart(0, 99 * minute, 10))
        assertEquals(10 * minute, IdleRules.msUntilStart(lastInputMs = 5_000, nowMs = 1_000, minutes = 10))
        assertFalse(IdleRules.shouldStart(lastInputMs = 5_000, nowMs = 1_000, minutes = 10, homeVisible = true, saverShowing = false))
    }

    @Test
    fun `the choices are never then five ten fifteen thirty and the default is ten`() {
        assertEquals(listOf(0, 5, 10, 15, 30), IdleRules.CHOICES)
        assertEquals(10, IdleRules.DEFAULT_MINUTES)
        assertEquals(10, ScreensaverSettings.Default.idleMinutes)
    }

    @Test
    fun `a stored idle time off the list snaps to the closest choice`() {
        assertEquals(10, IdleRules.snap(11))
        assertEquals(30, IdleRules.snap(1000))
        assertEquals(0, IdleRules.snap(-3))
        assertEquals(5, IdleRules.snap(5))
    }
}
