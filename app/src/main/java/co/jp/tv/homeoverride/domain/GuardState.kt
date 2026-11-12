package co.jp.tv.homeoverride.domain

/** What the safety guards remember between runs of the process (a crash kills the process, so it cannot live in memory). */
data class GuardState(
    /** Wall-clock ms of every crash and every redirect that failed to show JP-TV. */
    val troubleAt: List<Long> = emptyList(),
    /** The override stays out of the way until this wall-clock ms (the user opened the stock launcher on purpose). */
    val graceUntil: Long = 0L,
    /** When the last redirect happened and JP-TV has not reported drawn yet. */
    val pendingSince: Long? = null,
)

/** Port: persistence of [GuardState]. */
interface GuardStateStore {
    fun load(): GuardState
    fun save(state: GuardState)
}
