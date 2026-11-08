package co.jp.tv.network.domain

/** Connectivity snapshot. Real data arrives in phase 3; until then themes get [Unknown]. */
data class NetworkStatus(
    val kind: Kind,
    val ssid: String?,
    val ip: String?,
    val latencyMs: Int?,
    /** 0..4 */
    val signalBars: Int,
) {
    enum class Kind { WIFI, ETHERNET, OFFLINE, UNKNOWN }

    companion object {
        val Unknown = NetworkStatus(Kind.UNKNOWN, null, null, null, 0)
    }
}
