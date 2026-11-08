package co.jp.tv.network.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NetworkRulesTest {
    @Test
    fun `rssi maps to five levels from 0 to 4`() {
        assertEquals(0, NetworkRules.signalLevel(-120))
        assertEquals(0, NetworkRules.signalLevel(-100))
        assertEquals(1, NetworkRules.signalLevel(-88))
        assertEquals(2, NetworkRules.signalLevel(-75))
        assertEquals(3, NetworkRules.signalLevel(-66))
        assertEquals(4, NetworkRules.signalLevel(-55))
        assertEquals(4, NetworkRules.signalLevel(-30))
    }

    @Test
    fun `signal level never decreases as rssi improves`() {
        val levels = (-110..-20).map(NetworkRules::signalLevel)
        assertEquals(levels.sorted(), levels)
    }

    @Test
    fun `latency formats as whole milliseconds or a dash`() {
        assertEquals("12 ms", NetworkRules.formatLatency(12))
        assertEquals("0 ms", NetworkRules.formatLatency(0))
        assertEquals("—", NetworkRules.formatLatency(null))
    }

    @Test
    fun `internet requires a connection`() {
        assertFalse(NetworkRules.hasInternet(connected = false, validated = true, internetProbeMs = 20))
    }

    @Test
    fun `internet is on when the system validated it or the probe answered`() {
        assertTrue(NetworkRules.hasInternet(connected = true, validated = true, internetProbeMs = null))
        assertTrue(NetworkRules.hasInternet(connected = true, validated = false, internetProbeMs = 38))
        assertFalse(NetworkRules.hasInternet(connected = true, validated = false, internetProbeMs = null))
    }

    @Test
    fun `chip detail joins ip and router latency`() {
        val s = NetworkStatus(ip = "192.168.1.4", routerLatencyMs = 12, internetLatencyMs = 40)
        assertEquals("192.168.1.4 · 12 ms", NetworkRules.chipDetail(s))
    }

    @Test
    fun `chip detail falls back to internet latency then to ip only`() {
        assertEquals("10.0.0.2 · 40 ms", NetworkRules.chipDetail(NetworkStatus(ip = "10.0.0.2", internetLatencyMs = 40)))
        assertEquals("10.0.0.2", NetworkRules.chipDetail(NetworkStatus(ip = "10.0.0.2")))
        assertEquals("", NetworkRules.chipDetail(NetworkStatus()))
    }

    @Test
    fun `unknown ssid placeholders are discarded and quotes stripped`() {
        assertEquals(null, NetworkRules.cleanSsid("<unknown ssid>"))
        assertEquals(null, NetworkRules.cleanSsid(""))
        assertEquals(null, NetworkRules.cleanSsid(null))
        assertEquals("Casa", NetworkRules.cleanSsid("\"Casa\""))
        assertEquals("Casa", NetworkRules.cleanSsid("Casa"))
    }

    @Test
    fun `mac placeholders are discarded`() {
        assertEquals(null, NetworkRules.cleanMac("02:00:00:00:00:00"))
        assertEquals(null, NetworkRules.cleanMac(null))
        assertEquals("60:35:73:aa:bb:1f", NetworkRules.cleanMac("60:35:73:aa:bb:1f"))
    }
}
