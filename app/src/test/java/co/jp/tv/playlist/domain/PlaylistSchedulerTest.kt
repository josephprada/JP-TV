package co.jp.tv.playlist.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlaylistSchedulerTest {
    private val minute = 60_000L

    /** Random source that replays [values] (each reduced into the asked bound). */
    private fun scripted(vararg values: Int): (Int) -> Int {
        var i = 0
        return { bound -> values[i++ % values.size] % bound }
    }

    @Test
    fun `sequential order walks the list and wraps`() {
        val scheduler = PlaylistScheduler()
        assertEquals(1, scheduler.next(0, 3, PlaylistOrder.SEQUENTIAL))
        assertEquals(2, scheduler.next(1, 3, PlaylistOrder.SEQUENTIAL))
        assertEquals(0, scheduler.next(2, 3, PlaylistOrder.SEQUENTIAL))
    }

    @Test
    fun `a single item or an empty list always stays on the first slot`() {
        val scheduler = PlaylistScheduler()
        assertEquals(0, scheduler.next(0, 1, PlaylistOrder.SEQUENTIAL))
        assertEquals(0, scheduler.next(0, 1, PlaylistOrder.SHUFFLE))
        assertEquals(0, scheduler.next(0, 0, PlaylistOrder.SHUFFLE))
    }

    @Test
    fun `shuffle never repeats the current item right away`() {
        for (seed in 0 until 8) {
            val scheduler = PlaylistScheduler(scripted(seed, seed + 3, seed + 5))
            for (current in 0 until 4) {
                val next = scheduler.next(current, 4, PlaylistOrder.SHUFFLE)
                assertTrue(next in 0 until 4)
                assertTrue(next != current, "seed $seed current $current gave $next")
            }
        }
    }

    @Test
    fun `shuffle of two items always flips to the other one`() {
        val scheduler = PlaylistScheduler(scripted(0, 1, 0, 1))
        assertEquals(1, scheduler.next(0, 2, PlaylistOrder.SHUFFLE))
        assertEquals(0, scheduler.next(1, 2, PlaylistOrder.SHUFFLE))
    }

    @Test
    fun `an out of range current index starts from the beginning`() {
        val scheduler = PlaylistScheduler()
        assertEquals(0, scheduler.next(9, 3, PlaylistOrder.SEQUENTIAL))
    }

    @Test
    fun `the interval is due once its minutes have elapsed`() {
        val scheduler = PlaylistScheduler()
        assertFalse(scheduler.isDue(RotationInterval.MIN_5, 0, 5 * minute - 1))
        assertTrue(scheduler.isDue(RotationInterval.MIN_5, 0, 5 * minute))
        assertTrue(scheduler.isDue(RotationInterval.MIN_60, 0, 60 * minute))
    }

    @Test
    fun `never and on home return are never due by time`() {
        val scheduler = PlaylistScheduler()
        assertFalse(scheduler.isDue(RotationInterval.NEVER, 0, 10_000 * minute))
        assertFalse(scheduler.isDue(RotationInterval.ON_HOME_RETURN, 0, 10_000 * minute))
        assertNull(scheduler.remainingMs(RotationInterval.NEVER, 0, 0))
        assertNull(scheduler.remainingMs(RotationInterval.ON_HOME_RETURN, 0, 0))
    }

    @Test
    fun `a clock that went backwards counts as due`() {
        assertTrue(PlaylistScheduler().isDue(RotationInterval.MIN_15, lastChangeMs = 5_000_000, nowMs = 1_000))
    }

    @Test
    fun `remaining time counts down to the next change`() {
        val scheduler = PlaylistScheduler()
        assertEquals(15 * minute, scheduler.remainingMs(RotationInterval.MIN_15, 100, 100))
        assertEquals(10 * minute, scheduler.remainingMs(RotationInterval.MIN_15, 0, 5 * minute))
        assertEquals(0L, scheduler.remainingMs(RotationInterval.MIN_15, 0, 99 * minute))
    }

    @Test
    fun `tick advances only when due and stamps the change`() {
        val scheduler = PlaylistScheduler()
        val start = PlaylistPosition(index = 1, changedAtMs = 0)
        assertEquals(start, scheduler.tick(start, 3, RotationInterval.MIN_5, PlaylistOrder.SEQUENTIAL, 4 * minute))
        assertEquals(
            PlaylistPosition(2, 5 * minute),
            scheduler.tick(start, 3, RotationInterval.MIN_5, PlaylistOrder.SEQUENTIAL, 5 * minute),
        )
    }

    @Test
    fun `with one item nothing rotates`() {
        val scheduler = PlaylistScheduler()
        val start = PlaylistPosition(0, 0)
        assertEquals(start, scheduler.tick(start, 1, RotationInterval.MIN_5, PlaylistOrder.SEQUENTIAL, 99 * minute))
        assertEquals(start, scheduler.onHomeReturn(start, 1, RotationInterval.ON_HOME_RETURN, PlaylistOrder.SEQUENTIAL, 99 * minute))
    }

    @Test
    fun `returning home advances only in the home return mode`() {
        val scheduler = PlaylistScheduler()
        val start = PlaylistPosition(0, 10)
        assertEquals(start, scheduler.onHomeReturn(start, 3, RotationInterval.MIN_15, PlaylistOrder.SEQUENTIAL, 20))
        assertEquals(PlaylistPosition(1, 20), scheduler.onHomeReturn(start, 3, RotationInterval.ON_HOME_RETURN, PlaylistOrder.SEQUENTIAL, 20))
    }

    @Test
    fun `resume keeps the saved slot while the interval has not elapsed`() {
        val scheduler = PlaylistScheduler()
        val saved = PlaylistPosition(index = 2, changedAtMs = 1_000)
        assertEquals(saved, scheduler.resume(saved, 4, RotationInterval.MIN_30, PlaylistOrder.SEQUENTIAL, 1_000 + 29 * minute))
    }

    @Test
    fun `resume after a long death advances once`() {
        val scheduler = PlaylistScheduler()
        val saved = PlaylistPosition(index = 2, changedAtMs = 0)
        assertEquals(
            PlaylistPosition(3, 500 * minute),
            scheduler.resume(saved, 4, RotationInterval.MIN_30, PlaylistOrder.SEQUENTIAL, 500 * minute),
        )
    }

    @Test
    fun `resume with nothing saved starts at the first item`() {
        assertEquals(PlaylistPosition(0, 7), PlaylistScheduler().resume(null, 3, RotationInterval.MIN_30, PlaylistOrder.SEQUENTIAL, 7))
    }

    @Test
    fun `resume clamps a saved index that no longer exists`() {
        val saved = PlaylistPosition(index = 9, changedAtMs = 5)
        assertEquals(PlaylistPosition(0, 5), PlaylistScheduler().resume(saved, 3, RotationInterval.NEVER, PlaylistOrder.SEQUENTIAL, 6))
    }

    @Test
    fun `resume in home return mode counts the cold start as coming home`() {
        val saved = PlaylistPosition(index = 0, changedAtMs = 5)
        assertEquals(
            PlaylistPosition(1, 50),
            PlaylistScheduler().resume(saved, 3, RotationInterval.ON_HOME_RETURN, PlaylistOrder.SEQUENTIAL, 50),
        )
    }

    @Test
    fun `never keeps whatever was saved`() {
        val saved = PlaylistPosition(index = 1, changedAtMs = 0)
        assertEquals(saved, PlaylistScheduler().resume(saved, 3, RotationInterval.NEVER, PlaylistOrder.SEQUENTIAL, 9_999 * minute))
    }
}
