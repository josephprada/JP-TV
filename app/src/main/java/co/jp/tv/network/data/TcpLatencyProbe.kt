package co.jp.tv.network.data

import android.net.Network
import java.net.ConnectException
import java.net.InetSocketAddress
import javax.net.SocketFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Measures the time of a TCP handshake. A refused connection still counts: the host answered. */
internal object TcpLatencyProbe {
    /** Milliseconds to connect to [host]:[port] over [network], or null on timeout/unreachable. */
    suspend fun connectMs(network: Network, host: String, port: Int, timeoutMs: Int): Int? =
        withContext(Dispatchers.IO) { measure(network.socketFactory, host, port, timeoutMs) }

    /** First port that answers wins; null when none does. */
    suspend fun firstAnswer(network: Network, host: String, ports: List<Int>, timeoutMs: Int): Int? {
        for (port in ports) connectMs(network, host, port, timeoutMs)?.let { return it }
        return null
    }

    private fun measure(factory: SocketFactory, host: String, port: Int, timeoutMs: Int): Int? {
        val start = System.nanoTime()
        fun elapsed() = (((System.nanoTime() - start) + 999_999L) / 1_000_000L).toInt().coerceAtLeast(1)
        return try {
            factory.createSocket().use { it.connect(InetSocketAddress(host, port), timeoutMs) }
            elapsed()
        } catch (e: ConnectException) {
            if (e.message.orEmpty().contains("refused", ignoreCase = true)) elapsed() else null
        } catch (_: java.io.IOException) {
            null
        }
    }
}
