package co.jp.tv.screensaver.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScreensaverRulesTest {
    @Test
    fun `defaults are the clock with the JP-TV message after ten idle minutes`() {
        val d = ScreensaverSettings.Default
        assertEquals(ScreensaverMode.CLOCK, d.mode)
        assertEquals("JP-TV", d.message)
        assertEquals(10, d.idleMinutes)
    }

    @Test
    fun `the message is trimmed capped and never empty`() {
        assertEquals("Casa", ScreensaverSettings.sanitizeMessage("  Casa "))
        assertEquals(ScreensaverSettings.MAX_MESSAGE, ScreensaverSettings.sanitizeMessage("x".repeat(100)).length)
        assertEquals("JP-TV", ScreensaverSettings.sanitizeMessage("   "))
    }

    @Test
    fun `mode names round trip and unknown ones fall back to the clock`() {
        ScreensaverMode.entries.forEach { assertEquals(it, ScreensaverMode.fromName(it.name)) }
        assertEquals(ScreensaverMode.CLOCK, ScreensaverMode.fromName("nope"))
        assertEquals(ScreensaverMode.CLOCK, ScreensaverMode.fromName(null))
    }

    @Test
    fun `a fixed mode is used as is`() {
        for (mode in listOf(ScreensaverMode.PHOTOS, ScreensaverMode.CLOCK, ScreensaverMode.LIVE, ScreensaverMode.AMBIENT)) {
            assertEquals(mode, ScreensaverModes.concrete(mode, previous = null, hasPhotos = false, hasVideos = false) { 0 })
        }
    }

    @Test
    fun `random never repeats the previous mode`() {
        for (pick in 0 until 6) {
            val next = ScreensaverModes.concrete(ScreensaverMode.RANDOM, ScreensaverMode.CLOCK, hasPhotos = true, hasVideos = true) { bound -> pick % bound }
            assertNotEquals(ScreensaverMode.CLOCK, next)
            assertNotEquals(ScreensaverMode.RANDOM, next)
        }
    }

    @Test
    fun `random skips photos and live when there is nothing to show`() {
        for (pick in 0 until 6) {
            val next = ScreensaverModes.concrete(ScreensaverMode.RANDOM, previous = null, hasPhotos = false, hasVideos = false) { bound -> pick % bound }
            assertTrue(next == ScreensaverMode.CLOCK || next == ScreensaverMode.AMBIENT, "got $next")
        }
    }

    @Test
    fun `random with a single candidate keeps it`() {
        val next = ScreensaverModes.concrete(ScreensaverMode.RANDOM, ScreensaverMode.CLOCK, hasPhotos = false, hasVideos = false) { 0 }
        assertEquals(ScreensaverMode.AMBIENT, next)
    }

    @Test
    fun `random changes every ten minutes`() {
        assertEquals(10 * 60_000L, ScreensaverModes.RANDOM_EVERY_MS)
    }

    @Test
    fun `the burn-in drift visits nine different places before repeating`() {
        val places = (0L until 9L).map { BurnInDrift.offset(it) }
        assertEquals(9, places.toSet().size)
        assertEquals(BurnInDrift.offset(0), BurnInDrift.offset(9))
    }

    @Test
    fun `the drift moves on every step and stays inside the box`() {
        for (step in 0L until 40L) {
            val here = BurnInDrift.offset(step)
            assertNotEquals(here, BurnInDrift.offset(step + 1))
            assertTrue(here.x in -1f..1f && here.y in -1f..1f)
        }
    }

    @Test
    fun `a negative step still has a place`() {
        val p = BurnInDrift.offset(-3)
        assertTrue(p.x in -1f..1f && p.y in -1f..1f)
    }

    @Test
    fun `the drift step changes once a minute`() {
        assertEquals(BurnInDrift.stepOf(epochSecond = 120), BurnInDrift.stepOf(epochSecond = 179))
        assertNotEquals(BurnInDrift.stepOf(epochSecond = 179), BurnInDrift.stepOf(epochSecond = 180))
    }
}
