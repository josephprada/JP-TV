package co.jp.tv.core.util

import java.time.LocalDateTime

/** Pure formatting for the home clock card. Always Spanish, independent of the device locale. */
object ClockFormat {
    private val days = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")
    private val months = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

    /** Hour notation chosen by the user: [H24] ("21:40", the default) or [H12] ("9:40 PM"). */
    enum class HourFormat { H24, H12 }

    /** Clock text split so a theme can size and place hours, minutes and the AM/PM marker separately. */
    data class TimeParts(
        /** "00".."23" in [HourFormat.H24]; "1".."12" without zero padding in [HourFormat.H12]. */
        val hours: String,
        /** Always two digits, "00".."59". */
        val minutes: String,
        /** "AM" or "PM" in [HourFormat.H12]; null in [HourFormat.H24]. */
        val meridiem: String?,
    )

    /** Structured time of [now] in [format]; 00:05 -> 12/05/AM, 12:00 -> 12/00/PM, 21:40 -> 9/40/PM. */
    fun parts(now: LocalDateTime, format: HourFormat): TimeParts {
        val minutes = "%02d".format(now.minute)
        return when (format) {
            HourFormat.H24 -> TimeParts("%02d".format(now.hour), minutes, null)
            HourFormat.H12 -> {
                val hour12 = if (now.hour % 12 == 0) 12 else now.hour % 12
                TimeParts(hour12.toString(), minutes, if (now.hour < 12) "AM" else "PM")
            }
        }
    }

    /** Ready-made time of [now]: "21:40" in [HourFormat.H24], "9:40 PM" in [HourFormat.H12]. */
    fun time(now: LocalDateTime, format: HourFormat): String {
        val parts = parts(now, format)
        val clock = "${parts.hours}:${parts.minutes}"
        return if (parts.meridiem == null) clock else "$clock ${parts.meridiem}"
    }

    fun date(now: LocalDateTime): String =
        "${days[now.dayOfWeek.value - 1]} · ${now.dayOfMonth} ${months[now.monthValue - 1]}"

    fun colonVisible(now: LocalDateTime): Boolean = now.second % 2 == 0

    /** Fraction of the current minute elapsed, in [0, 1). */
    fun minuteProgress(now: LocalDateTime): Float = now.second / 60f
}
