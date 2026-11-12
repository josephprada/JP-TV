package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeOverrideStatusTest {
    @Test
    fun `active needs the toggle and the service`() {
        assertEquals(HomeOverrideStatus.ACTIVE, HomeOverrideStatus.of(toggleOn = true, serviceEnabled = true))
    }

    @Test
    fun `toggle on without the service asks for permission`() {
        assertEquals(HomeOverrideStatus.NEEDS_PERMISSION, HomeOverrideStatus.of(toggleOn = true, serviceEnabled = false))
    }

    @Test
    fun `toggle off is disabled whatever the service says`() {
        assertEquals(HomeOverrideStatus.DISABLED, HomeOverrideStatus.of(toggleOn = false, serviceEnabled = true))
        assertEquals(HomeOverrideStatus.DISABLED, HomeOverrideStatus.of(toggleOn = false, serviceEnabled = false))
    }

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
