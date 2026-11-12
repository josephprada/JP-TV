package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WatcherPlanTest {
    private fun mode(
        toggle: Boolean = true,
        usage: Boolean = true,
        overlay: Boolean = true,
        screen: Boolean = true,
        a11y: Boolean = false,
    ) = WatcherPlan.mode(toggle, usage, overlay, screen, a11y)

    @Test
    fun `polls when everything is in place`() {
        assertEquals(WatcherMode.POLLING, mode())
    }

    @Test
    fun `stops when the toggle is off`() {
        assertEquals(WatcherMode.STOPPED, mode(toggle = false))
    }

    @Test
    fun `stops when a permission is missing`() {
        assertEquals(WatcherMode.STOPPED, mode(usage = false))
        assertEquals(WatcherMode.STOPPED, mode(overlay = false))
    }

    @Test
    fun `idles with the screen off`() {
        assertEquals(WatcherMode.IDLE, mode(screen = false))
    }

    @Test
    fun `idles while the accessibility service is bound`() {
        assertEquals(WatcherMode.IDLE, mode(a11y = true))
    }

    @Test
    fun `stopping wins over idling`() {
        assertEquals(WatcherMode.STOPPED, mode(toggle = false, screen = false))
    }

    @Test
    fun `can run needs the toggle and both accesses`() {
        assertTrue(WatcherPlan.canRun(true, true, true))
        assertFalse(WatcherPlan.canRun(true, true, false))
        assertFalse(WatcherPlan.canRun(false, true, true))
    }

    @Test
    fun `query covers the last two seconds on the first poll`() {
        assertEquals(8_000L, WatcherPlan.queryStart(now = 10_000, lastQueryEnd = 0))
    }

    @Test
    fun `query restarts slightly before the previous one ended`() {
        assertEquals(9_550L, WatcherPlan.queryStart(now = 10_000, lastQueryEnd = 9_700))
    }
}
