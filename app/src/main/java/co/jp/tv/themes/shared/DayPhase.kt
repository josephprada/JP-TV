package co.jp.tv.themes.shared

import java.time.LocalDateTime

/** Part of the day a theme draws: night from 18:00 until 05:59, day from 06:00 until 17:59. */
enum class DayPhase {
    DAY,
    NIGHT,
    ;

    /** True for [NIGHT]. */
    val isNight: Boolean get() = this == NIGHT

    companion object {
        /** Phase for the local wall-clock time [now]; night when the hour is >= 18 or < 6. */
        fun of(now: LocalDateTime): DayPhase =
            if (now.hour >= NIGHT_START_HOUR || now.hour < DAY_START_HOUR) NIGHT else DAY

        private const val DAY_START_HOUR = 6
        private const val NIGHT_START_HOUR = 18
    }
}
