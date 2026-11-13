package co.jp.tv.homeoverride.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BootSplashPlanTest {
    private val timeout = BootSplashPlan.TIMEOUT_MS

    @Test
    fun `unlocked user is forwarded to the home straight away`() {
        assertEquals(SplashStep.FORWARD, BootSplashPlan.decide(unlocked = true, elapsedMs = 0, stockTried = false))
    }

    @Test
    fun `locked user waits for the unlock`() {
        assertEquals(SplashStep.WAIT, BootSplashPlan.decide(unlocked = false, elapsedMs = 0, stockTried = false))
        assertEquals(SplashStep.WAIT, BootSplashPlan.decide(unlocked = false, elapsedMs = timeout - 1, stockTried = false))
    }

    @Test
    fun `the unlock event forwards even after a long wait`() {
        assertEquals(SplashStep.FORWARD, BootSplashPlan.decide(unlocked = true, elapsedMs = timeout - 1, stockTried = false))
        assertEquals(SplashStep.FORWARD, BootSplashPlan.decide(unlocked = true, elapsedMs = timeout * 3, stockTried = true))
    }

    @Test
    fun `timeout without unlock falls back to the stock home`() {
        assertEquals(SplashStep.STOCK_HOME, BootSplashPlan.decide(unlocked = false, elapsedMs = timeout, stockTried = false))
    }

    @Test
    fun `when no stock home resolved the splash stays and keeps waiting`() {
        assertEquals(SplashStep.WAIT, BootSplashPlan.decide(unlocked = false, elapsedMs = timeout + 5_000, stockTried = true))
    }

    @Test
    fun `fallback prefers the known launchers in order and skips JP-TV, JmLauncher and system packages`() {
        val resolved = listOf(
            "co.jp.tv",
            "com.jm.jmlauncher",
            "com.android.settings",
            "com.vendor.home",
            "com.google.android.tvlauncher",
            "com.smart.tv.launcher",
            "com.htc.htclauncherhighenglishd08",
        )
        assertEquals(
            listOf(
                "com.htc.htclauncherhighenglishd08",
                "com.smart.tv.launcher",
                "com.google.android.tvlauncher",
                "com.vendor.home",
            ),
            BootSplashPlan.fallbackOrder(resolved, ownPackage = "co.jp.tv"),
        )
    }

    @Test
    fun `fallback order is empty when nothing else resolves`() {
        assertEquals(emptyList<String>(), BootSplashPlan.fallbackOrder(listOf("co.jp.tv", "com.jm.jmlauncher"), "co.jp.tv"))
    }
}
