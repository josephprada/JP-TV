package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HomeOverrideStateTest {
    private fun state(toggle: Boolean = true, a11y: Boolean = false, usage: Boolean = false, overlay: Boolean = false) =
        HomeOverrideState.of(toggle, a11y, usage, overlay)

    @Test
    fun `toggle off is disabled whatever is granted`() {
        assertEquals(HomeOverrideState(HomeOverrideStatus.DISABLED), state(toggle = false, a11y = true, usage = true, overlay = true))
    }

    @Test
    fun `accessibility enabled is active through accessibility`() {
        assertEquals(HomeOverrideState(HomeOverrideStatus.ACTIVE, DetectionMethod.ACCESSIBILITY), state(a11y = true))
    }

    @Test
    fun `usage access and overlay are active through usage data`() {
        assertEquals(HomeOverrideState(HomeOverrideStatus.ACTIVE, DetectionMethod.USAGE_STATS), state(usage = true, overlay = true))
    }

    @Test
    fun `accessibility wins when both backends are available`() {
        assertEquals(DetectionMethod.ACCESSIBILITY, state(a11y = true, usage = true, overlay = true).method)
    }

    @Test
    fun `nothing granted lists both permissions`() {
        val s = state()
        assertEquals(HomeOverrideStatus.NEEDS_PERMISSION, s.status)
        assertEquals(listOf(MissingPermission.USAGE_ACCESS, MissingPermission.OVERLAY), s.missing)
        assertEquals(null, s.method)
    }

    @Test
    fun `only the missing permission is listed`() {
        assertEquals(listOf(MissingPermission.OVERLAY), state(usage = true).missing)
        assertEquals(listOf(MissingPermission.USAGE_ACCESS), state(overlay = true).missing)
    }
}
