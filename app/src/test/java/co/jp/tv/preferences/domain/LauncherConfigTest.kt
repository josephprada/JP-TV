package co.jp.tv.preferences.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LauncherConfigTest {
    @Test
    fun `dim moves in ten point steps`() {
        assertEquals(10, LauncherConfig.stepDim(0, +1))
        assertEquals(30, LauncherConfig.stepDim(40, -1))
    }

    @Test
    fun `dim never leaves the allowed range`() {
        assertEquals(0, LauncherConfig.stepDim(0, -1))
        assertEquals(LauncherConfig.MAX_DIM, LauncherConfig.stepDim(LauncherConfig.MAX_DIM, +1))
    }

    @Test
    fun `an off-grid stored dim snaps to the next step`() {
        assertEquals(40, LauncherConfig.stepDim(35, +1))
        assertEquals(30, LauncherConfig.stepDim(35, -1))
    }

    @Test
    fun `brand is trimmed and capped`() {
        assertEquals("Casa", LauncherConfig.sanitizeBrand("  Casa  "))
        assertEquals("A".repeat(LauncherConfig.MAX_BRAND), LauncherConfig.sanitizeBrand("A".repeat(40)))
    }

    @Test
    fun `a blank brand falls back to the default`() {
        assertEquals(LauncherConfig.DEFAULT_BRAND, LauncherConfig.sanitizeBrand("   "))
        assertEquals(LauncherConfig.DEFAULT_BRAND, LauncherConfig.sanitizeBrand(""))
    }
}
