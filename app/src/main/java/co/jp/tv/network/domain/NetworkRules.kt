package co.jp.tv.network.domain

/** Pure rules around network data: signal, latency text, internet state, sanitizing platform placeholders. */
object NetworkRules {
    private const val RSSI_MIN = -100
    private const val RSSI_MAX = -55
    private const val MAX_LEVEL = 4
    private const val UNKNOWN_SSID = "<unknown ssid>"
    private const val PLACEHOLDER_MAC = "02:00:00:00:00:00"

    /** Maps RSSI (dBm) to 0..4, linear between [RSSI_MIN] and [RSSI_MAX]. */
    fun signalLevel(rssiDbm: Int): Int = when {
        rssiDbm <= RSSI_MIN -> 0
        rssiDbm >= RSSI_MAX -> MAX_LEVEL
        else -> (rssiDbm - RSSI_MIN) * MAX_LEVEL / (RSSI_MAX - RSSI_MIN)
    }

    fun formatLatency(ms: Int?): String = if (ms == null) "—" else "$ms ms"

    /** Validated by the system's own check, or proven by our own probe to 1.1.1.1. */
    fun hasInternet(connected: Boolean, validated: Boolean, internetProbeMs: Int?): Boolean =
        connected && (validated || internetProbeMs != null)

    /** "192.168.1.4 · 12 ms": router latency first, internet latency as fallback, ip alone otherwise. */
    fun chipDetail(status: NetworkStatus): String {
        val ip = status.ip ?: return ""
        val latency = status.routerLatencyMs ?: status.internetLatencyMs ?: return ip
        return "$ip · ${formatLatency(latency)}"
    }

    /** Without the location permission Android reports "<unknown ssid>"; treat it as no name. */
    fun cleanSsid(raw: String?): String? {
        val name = raw?.removeSurrounding("\"")?.trim()
        return name?.takeIf { it.isNotEmpty() && it != UNKNOWN_SSID }
    }

    /** Android 6+ hides the real MAC behind a constant placeholder. */
    fun cleanMac(raw: String?): String? = raw?.takeIf { it.isNotBlank() && !it.equals(PLACEHOLDER_MAC, ignoreCase = true) }
}
