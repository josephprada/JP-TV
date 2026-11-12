package co.jp.tv.network.domain

/** What stands between the app and the Wi-Fi name (Android hides it without location access). */
enum class SsidAccess { Available, NeedsPermission, NeedsLocation, Unavailable }

/** Connectivity snapshot shown by the chip and the network panel. */
data class NetworkStatus(
    val state: State = State.UNKNOWN,
    val transport: Transport? = null,
    /** Null when unknown or hidden by Android (no location permission). */
    val ssid: String? = null,
    /** Why [ssid] is missing, so the panel can ask for the permission or for location services. */
    val ssidAccess: SsidAccess = SsidAccess.Available,
    val ip: String? = null,
    val gateway: String? = null,
    val dns: List<String> = emptyList(),
    /** 0..4 */
    val signalLevel: Int = 0,
    val routerLatencyMs: Int? = null,
    val internetLatencyMs: Int? = null,
    val hasInternet: Boolean = false,
    /** Wi-Fi only: 0..100 from RSSI and the band label ("5 GHZ"); null when unknown. */
    val signalPercent: Int? = null,
    val band: String? = null,
    /** Only present when obtainable without extra permissions. */
    val mac: String? = null,
) {
    enum class State { CONNECTED, DISCONNECTED, UNKNOWN }
    enum class Transport { WIFI, ETHERNET }

    val isConnected: Boolean get() = state == State.CONNECTED

    companion object {
        val Unknown = NetworkStatus()
        val Disconnected = NetworkStatus(state = State.DISCONNECTED)
    }
}
