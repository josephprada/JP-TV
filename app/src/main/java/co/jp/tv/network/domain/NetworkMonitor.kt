package co.jp.tv.network.domain

import kotlinx.coroutines.flow.Flow

/**
 * Port: live network status.
 *
 * [status] is cold: collecting it starts the callbacks and the latency probes, cancelling stops them.
 */
interface NetworkMonitor {
    val status: Flow<NetworkStatus>

    /** Re-runs the latency probes right away (the "PROBAR CONEXIÓN" button). */
    fun probeNow()

    /** Re-reads what depends on permissions and system switches (the network name) after they changed. */
    fun refreshAccess()
}
