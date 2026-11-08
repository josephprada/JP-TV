package co.jp.tv.network.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import co.jp.tv.network.domain.NetworkMonitor
import co.jp.tv.network.domain.NetworkRules
import co.jp.tv.network.domain.NetworkStatus
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Adapter over ConnectivityManager + WifiManager.
 *
 * Collecting [status] registers the default-network callback and starts a probe loop (router and
 * 1.1.1.1 every [PROBE_INTERVAL_MS]); cancelling the collection unregisters and stops everything.
 * All blocking work runs on the IO dispatcher.
 */
class ConnectivityNetworkMonitor(context: Context) : NetworkMonitor {
    private val appContext = context.applicationContext
    private val connectivity = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifi = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val probeTrigger = Channel<Unit>(Channel.CONFLATED)

    override fun probeNow() {
        probeTrigger.trySend(Unit)
    }

    /** What the system told us about the default network; a null [network] means "no connection". */
    private data class Link(
        val network: Network? = null,
        val caps: NetworkCapabilities? = null,
        val props: LinkProperties? = null,
    )

    private data class Latency(val router: Int? = null, val internet: Int? = null)

    override val status: Flow<NetworkStatus> = channelFlow {
        // null = the first callback has not arrived yet.
        val link = MutableStateFlow<Link?>(null)
        val latency = MutableStateFlow(Latency())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                link.update { if (it?.network == network) it else Link(network) }
                probeTrigger.trySend(Unit)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                link.update { (it?.takeIf { l -> l.network == network } ?: Link(network)).copy(caps = caps) }
            }

            override fun onLinkPropertiesChanged(network: Network, props: LinkProperties) {
                val previousGateway = link.value?.takeIf { it.network == network }?.props?.let(::gatewayOf)
                link.update { (it?.takeIf { l -> l.network == network } ?: Link(network)).copy(props = props) }
                // Routes usually arrive after the first probe started: probe again once the gateway is known.
                if (gatewayOf(props) != previousGateway) probeTrigger.trySend(Unit)
            }

            override fun onLost(network: Network) {
                link.update { if (it?.network == network) Link() else it }
                latency.value = Latency()
            }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        if (connectivity.activeNetwork == null) link.update { it ?: Link() }

        launch(Dispatchers.IO) {
            while (isActive) {
                val current = link.value
                val network = current?.network
                val gateway = current?.props?.let(::gatewayOf)
                if (network == null) {
                    latency.value = Latency()
                } else {
                    val result = probe(network, gateway)
                    // The network may have been lost (and latency reset) while probing: drop stale results.
                    if (link.value?.network == network) latency.value = result
                }
                withTimeoutOrNull(PROBE_INTERVAL_MS) { probeTrigger.receive() }
            }
        }

        launch(Dispatchers.IO) {
            combine(link, latency) { l, lat -> l?.let { assemble(it, lat) } ?: NetworkStatus.Unknown }
                .collect { send(it) }
        }

        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }

    private suspend fun probe(network: Network, gateway: String?): Latency = coroutineScope {
        val router = async {
            gateway?.let { TcpLatencyProbe.firstAnswer(network, it, ROUTER_PORTS, ROUTER_TIMEOUT_MS) }
        }
        val internet = async { TcpLatencyProbe.connectMs(network, INTERNET_HOST, INTERNET_PORT, INTERNET_TIMEOUT_MS) }
        Latency(router.await(), internet.await())
    }

    private fun assemble(link: Link, latency: Latency): NetworkStatus {
        if (link.network == null) return NetworkStatus.Disconnected
        val caps = link.caps
        val props = link.props
        val transport = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkStatus.Transport.WIFI
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkStatus.Transport.ETHERNET
            else -> null
        }
        val validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val wifiInfo = if (transport == NetworkStatus.Transport.WIFI) wifiInfoOrNull() else null
        return NetworkStatus(
            state = NetworkStatus.State.CONNECTED,
            transport = transport,
            ssid = NetworkRules.cleanSsid(wifiInfo?.ssid),
            ip = props?.let(::ipOf),
            gateway = props?.let(::gatewayOf),
            dns = props?.let(::dnsOf).orEmpty(),
            signalLevel = when (transport) {
                NetworkStatus.Transport.WIFI -> wifiInfo?.rssi?.let(NetworkRules::signalLevel) ?: 0
                NetworkStatus.Transport.ETHERNET -> 4
                null -> 0
            },
            routerLatencyMs = latency.router,
            internetLatencyMs = latency.internet,
            hasInternet = NetworkRules.hasInternet(true, validated, latency.internet),
            mac = props?.interfaceName?.let(::macOf),
        )
    }

    @Suppress("DEPRECATION")
    private fun wifiInfoOrNull() = try {
        wifi?.connectionInfo
    } catch (_: SecurityException) {
        null
    }

    private fun ipOf(props: LinkProperties): String? {
        val addresses = props.linkAddresses.map { it.address }
        return (addresses.firstOrNull { it is Inet4Address } ?: addresses.firstOrNull())?.hostAddress
    }

    private fun gatewayOf(props: LinkProperties): String? {
        val gateways = props.routes.filter { it.isDefaultRoute }.mapNotNull { it.gateway }
        return (gateways.firstOrNull { it is Inet4Address } ?: gateways.firstOrNull())?.hostAddress
    }

    private fun dnsOf(props: LinkProperties): List<String> {
        val all: List<InetAddress> = props.dnsServers
        val v4 = all.filterIsInstance<Inet4Address>()
        return (v4.ifEmpty { all }).mapNotNull { it.hostAddress }
    }

    /** Hardware address of the interface; null when Android hides it (API 30+) or it is a placeholder. */
    private fun macOf(interfaceName: String): String? = try {
        NetworkInterface.getByName(interfaceName)?.hardwareAddress
            ?.joinToString(":") { "%02X".format(it) }
            .let(NetworkRules::cleanMac)
    } catch (_: java.io.IOException) {
        null
    }

    private companion object {
        const val PROBE_INTERVAL_MS = 10_000L
        val ROUTER_PORTS = listOf(53, 80)
        const val ROUTER_TIMEOUT_MS = 1_500
        const val INTERNET_HOST = "1.1.1.1"
        const val INTERNET_PORT = 443
        const val INTERNET_TIMEOUT_MS = 3_000
    }
}
