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

    @Test
    fun `signal percent is linear between -100 and -55 dbm and clamped`() {
        assertEquals(0, NetworkRules.signalPercent(-120))
        assertEquals(0, NetworkRules.signalPercent(-100))
        assertEquals(62, NetworkRules.signalPercent(-72))
        assertEquals(100, NetworkRules.signalPercent(-55))
        assertEquals(100, NetworkRules.signalPercent(-20))
    }

    @Test
    fun `band label follows the wifi frequency`() {
        assertEquals("2.4 GHZ", NetworkRules.bandLabel(2437))
        assertEquals("5 GHZ", NetworkRules.bandLabel(5180))
        assertEquals("6 GHZ", NetworkRules.bandLabel(6115))
        assertEquals(null, NetworkRules.bandLabel(0))
        assertEquals(null, NetworkRules.bandLabel(null))
    }

    @Test
    fun `mac keeps the vendor prefix and the last byte`() {
        assertEquals("60:35:73:··:··:1F", NetworkRules.maskMac("60:35:73:AB:CD:1F"))
        assertEquals("not-a-mac", NetworkRules.maskMac("not-a-mac"))
    }

    @Test
    fun `ssid placeholder is matched ignoring case and surrounding quotes`() {
        assertEquals(null, NetworkRules.cleanSsid("\"<unknown ssid>\""))
        assertEquals(null, NetworkRules.cleanSsid("<UNKNOWN SSID>"))
        assertEquals(null, NetworkRules.cleanSsid("  \"\"  "))
    }

    @Test
    fun `ssid keeps inner quotes and spaces but trims the edges`() {
        assertEquals("Casa de \"JP\"", NetworkRules.cleanSsid("\"Casa de \"JP\"\""))
        assertEquals("Mi Red", NetworkRules.cleanSsid("  Mi Red "))
    }

    @Test
    fun `a known ssid needs nothing from the user`() {
        assertEquals(SsidAccess.Available, NetworkRules.ssidAccess("Casa", permissionGranted = false, locationOn = false))
    }

    @Test
    fun `without the permission the user must grant it first`() {
        assertEquals(SsidAccess.NeedsPermission, NetworkRules.ssidAccess(null, permissionGranted = false, locationOn = false))
        assertEquals(SsidAccess.NeedsPermission, NetworkRules.ssidAccess(null, permissionGranted = false, locationOn = true))
    }

    @Test
    fun `with the permission but location services off the user must turn them on`() {
        assertEquals(SsidAccess.NeedsLocation, NetworkRules.ssidAccess(null, permissionGranted = true, locationOn = false))
    }

    @Test
    fun `with everything granted an unknown ssid is just unavailable`() {
        assertEquals(SsidAccess.Unavailable, NetworkRules.ssidAccess(null, permissionGranted = true, locationOn = true))
    }
}
