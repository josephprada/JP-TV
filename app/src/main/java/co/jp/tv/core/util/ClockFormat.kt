package co.jp.tv.core.util

import java.time.LocalDateTime

/** Pure formatting for the home clock card. Always Spanish, independent of the device locale. */
object ClockFormat {
    private val days = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")
    private val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

    fun time(now: LocalDateTime): String = "%02d:%02d".format(now.hour, now.minute)

    fun date(now: LocalDateTime): String =
        "${days[now.dayOfWeek.value - 1]} · ${now.dayOfMonth} ${months[now.monthValue - 1]}"

    fun colonVisible(now: LocalDateTime): Boolean = now.second % 2 == 0

    /** Fraction of the current minute elapsed, in [0, 1). */
    fun minuteProgress(now: LocalDateTime): Float = now.second / 60f
}
