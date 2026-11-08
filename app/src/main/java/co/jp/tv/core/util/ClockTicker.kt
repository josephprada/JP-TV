package co.jp.tv.core.util

import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object ClockTicker {
    /** Emits `now()` immediately, then on every [periodMs] boundary of the wall clock. */
    fun ticks(now: () -> LocalDateTime = { LocalDateTime.now() }, periodMs: Long = 1000): Flow<LocalDateTime> = flow {
        while (true) {
            val current = now()
            emit(current)
            val intoPeriod = (current.nano / 1_000_000L) % periodMs
            delay(periodMs - intoPeriod)
        }
    }
}
