package co.jp.tv.themes.lofi

import java.time.LocalDateTime

/** The artboard's date, "SÁB 08 NOV", built without locale APIs. */
internal object LofiDate {
    /** [days] starts on Sunday (DOM..SÁB) like JavaScript's `getDay()`; [months] starts on January. */
    fun format(now: LocalDateTime, days: Array<String>, months: Array<String>): String {
        val day = days[now.dayOfWeek.value % 7]
        val date = now.dayOfMonth.toString().padStart(2, '0')
        val month = months[now.monthValue - 1]
        return "$day $date $month"
    }
}
