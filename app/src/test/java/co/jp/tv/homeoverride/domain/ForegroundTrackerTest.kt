package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ForegroundTrackerTest {
    private val own = "co.jp.tv"
    private val resolved = setOf("com.vendor.home")
    private val tracker = ForegroundTracker()

    private fun pending() = tracker.pendingStock(resolved, own)

    @Test
    fun `a known stock launcher in front is pending`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        assertEquals("com.jm.jmlauncher", pending())
    }

    @Test
    fun `a launcher found by the resolvers is pending`() {
        tracker.onResumed("com.vendor.home", 100)
        assertEquals("com.vendor.home", pending())
    }

    @Test
    fun `only the latest resumed package counts`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.onResumed("com.netflix.ninja", 200)
        assertNull(pending())
    }

    @Test
    fun `jp-tv itself is ignored`() {
        tracker.onResumed(own, 100)
        assertNull(pending())
    }

    @Test
    fun `stock launcher replaced by jp-tv is not pending`() {
        tracker.onResumed("com.htc.htclauncherhighenglishd08", 100)
        tracker.onResumed(own, 150)
        assertNull(pending())
    }

    @Test
    fun `transient settings after the stock launcher is ignored`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.onResumed("com.android.settings", 120)
        assertNull(pending())
    }

    @Test
    fun `settings on its own is never stock`() {
        tracker.onResumed("com.android.settings", 100)
        assertNull(pending())
    }

    @Test
    fun `the chained stock launcher is the one reported`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.onResumed("com.htc.htclauncherhighenglishd08", 110)
        assertEquals("com.htc.htclauncherhighenglishd08", pending())
    }

    @Test
    fun `a handled event is not reported again when the window overlaps`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.markHandled()
        tracker.onResumed("com.jm.jmlauncher", 100)
        assertNull(pending())
    }

    @Test
    fun `an unhandled event stays pending across polls`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.onResumed("com.jm.jmlauncher", 100)
        assertEquals("com.jm.jmlauncher", pending())
    }

    @Test
    fun `a new stock event after a handled one is pending again`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.markHandled()
        tracker.onResumed(own, 200)
        tracker.onResumed("com.jm.jmlauncher", 300)
        assertEquals("com.jm.jmlauncher", pending())
    }

    @Test
    fun `older events replayed by an overlapping window are ignored`() {
        tracker.onResumed(own, 200)
        tracker.onResumed("com.jm.jmlauncher", 100)
        assertNull(pending())
    }

    @Test
    fun `reset forgets everything`() {
        tracker.onResumed("com.jm.jmlauncher", 100)
        tracker.reset()
        assertNull(pending())
    }
}
