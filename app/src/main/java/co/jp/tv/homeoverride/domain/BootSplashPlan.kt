package co.jp.tv.homeoverride.domain

/** What the boot splash does on each check. */
enum class SplashStep { FORWARD, WAIT, STOCK_HOME }

/**
 * Decisions of the direct-boot splash: it is the only JP-TV activity visible before the user's storage is
 * unlocked, so it forwards to the real home as soon as the unlock happens and gives up after [TIMEOUT_MS].
 */
object BootSplashPlan {
    const val TIMEOUT_MS = 20_000L

    /** Stock launchers tried first on the fallback, in this order. */
    val PREFERRED_FALLBACK = listOf(
        "com.htc.htclauncherhighenglishd08",
        "com.smart.tv.launcher",
        "com.google.android.tvlauncher",
    )

    /** Never a fallback: JmLauncher would start the package in persist.sys.home.selectpkg, that is, this splash again. */
    private const val JM_LAUNCHER = "com.jm.jmlauncher"

    fun decide(unlocked: Boolean, elapsedMs: Long, stockTried: Boolean): SplashStep = when {
        unlocked -> SplashStep.FORWARD
        elapsedMs >= TIMEOUT_MS && !stockTried -> SplashStep.STOCK_HOME
        else -> SplashStep.WAIT
    }

    /** Resolved home packages (resolver order) reduced to the usable fallbacks: preferred ones first, then the rest. */
    fun fallbackOrder(resolved: List<String>, ownPackage: String): List<String> {
        val usable = resolved.distinct().filter { it != ownPackage && it != JM_LAUNCHER && it !in StockHomes.NEVER }
        return PREFERRED_FALLBACK.filter { it in usable } + usable.filter { it !in PREFERRED_FALLBACK }
    }
}
