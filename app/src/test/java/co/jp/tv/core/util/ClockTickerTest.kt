package co.jp.tv.core.util

import app.cash.turbine.test
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ClockTickerTest {
    private val start = LocalDateTime.of(2025, 11, 8, 21, 40, 0)

    private fun TestScope.fakeNow(offsetMs: Long = 0) =
        { start.plusNanos((testScheduler.currentTime + offsetMs) * 1_000_000) }

    @Test
    fun `emits immediately and then once per second`() = runTest {
        ClockTicker.ticks(now = fakeNow(), periodMs = 1000).test {
            assertEquals(start, awaitItem())
            assertEquals(start.plusSeconds(1), awaitItem())
            assertEquals(start.plusSeconds(2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `aligns to the period boundary when starting mid second`() = runTest {
        ClockTicker.ticks(now = fakeNow(offsetMs = 250), periodMs = 1000).test {
            assertEquals(start.plusNanos(250_000_000), awaitItem())
            // next emission lands on the next whole second
            assertEquals(start.plusSeconds(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
