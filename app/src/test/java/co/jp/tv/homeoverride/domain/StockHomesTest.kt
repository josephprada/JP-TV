package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StockHomesTest {
    private val own = "co.jp.tv"

    @Test
    fun `known stock launchers are recognised without resolving anything`() {
        assertTrue(StockHomes.isStockHome("com.jm.jmlauncher", emptySet(), own))
        assertTrue(StockHomes.isStockHome("com.htc.htclauncherhighenglishd08", emptySet(), own))
        assertTrue(StockHomes.isStockHome("com.smart.tv.launcher", emptySet(), own))
    }

    @Test
    fun `a package that resolves HOME on this device counts`() {
        assertTrue(StockHomes.isStockHome("com.vendor.home", setOf("com.vendor.home"), own))
    }

    @Test
    fun `ordinary apps are not stock homes`() {
        assertFalse(StockHomes.isStockHome("com.netflix.ninja", setOf("com.vendor.home"), own))
    }

    @Test
    fun `JP-TV itself is never redirected`() {
        assertFalse(StockHomes.isStockHome(own, setOf(own), own))
    }

    @Test
    fun `settings and system ui are never redirected even if they resolve HOME`() {
        val resolved = setOf("com.android.settings", "com.android.systemui", "android")
        assertFalse(StockHomes.isStockHome("com.android.settings", resolved, own))
        assertFalse(StockHomes.isStockHome("com.android.systemui", resolved, own))
        assertFalse(StockHomes.isStockHome("android", resolved, own))
        assertFalse(StockHomes.isStockHome("com.android.tv.settings", setOf("com.android.tv.settings"), own))
    }

    @Test
    fun `a null package is not a stock home`() {
        assertFalse(StockHomes.isStockHome(null, emptySet(), own))
    }

    @Test
    fun `the resolved set drops JP-TV and the never-touch packages`() {
        val cleaned = StockHomes.candidates(setOf(own, "com.android.settings", "com.jm.jmlauncher"), own)
        assertEquals(setOf("com.jm.jmlauncher"), cleaned)
    }
}
