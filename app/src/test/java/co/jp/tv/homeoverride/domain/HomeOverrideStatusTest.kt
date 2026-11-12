package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeOverrideStatusTest {
    private val service = "co.jp.tv/co.jp.tv.homeoverride.data.HomeOverrideService"

    @Test
    fun `the service is found in the enabled list in full or short form`() {
        assertTrue(AccessibilityServices.isEnabled("a.b/.C:$service", "co.jp.tv", "co.jp.tv.homeoverride.data.HomeOverrideService"))
        assertTrue(AccessibilityServices.isEnabled("co.jp.tv/.homeoverride.data.HomeOverrideService", "co.jp.tv", "co.jp.tv.homeoverride.data.HomeOverrideService"))
    }

    @Test
    fun `null blank or other services are not enabled`() {
        val cls = "co.jp.tv.homeoverride.data.HomeOverrideService"
        assertFalse(AccessibilityServices.isEnabled(null, "co.jp.tv", cls))
        assertFalse(AccessibilityServices.isEnabled("", "co.jp.tv", cls))
        assertFalse(AccessibilityServices.isEnabled("com.other/.Svc:co.jp.tv/.Other", "co.jp.tv", cls))
    }
}
