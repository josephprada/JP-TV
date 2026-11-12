package co.jp.tv.themes.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LightningTest {
    @Test
    fun `lightning is rare, brief and never above full strength`() {
        var lit = 0
        for (ms in 0 until Lightning.DEFAULT_PERIOD_MS step 10) {
            val f = Lightning.flash(ms.toLong())
            assertTrue(f in 0f..1f, "$ms -> $f")
            if (f > 0f) lit++
        }
        val share = lit * 10f / Lightning.DEFAULT_PERIOD_MS
        assertTrue(share < 0.05f, "lit $share of the cycle")
        assertTrue(Lightning.flash(0) > 0.5f, "the cycle opens with a flash")
        assertEquals(0f, Lightning.flash(Lightning.DEFAULT_PERIOD_MS / 2L))
    }

    @Test
    fun `the cycle repeats and a longer period spaces the flashes out`() {
        assertEquals(Lightning.flash(30), Lightning.flash(30L + Lightning.DEFAULT_PERIOD_MS))
        assertEquals(0f, Lightning.flash(14_000, periodMs = 17_000))
        assertTrue(Lightning.flash(17_010, periodMs = 17_000) > 0.5f)
    }
}
