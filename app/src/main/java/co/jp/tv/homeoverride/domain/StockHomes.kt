package co.jp.tv.homeoverride.domain

/** Which packages count as "the stock launcher" that JP-TV may take the screen from. */
object StockHomes {
    /** Home apps of the firmwares seen so far; the device's own HOME resolvers are added at runtime. */
    val KNOWN = setOf(
        "com.jm.jmlauncher",
        "com.htc.htclauncherhighenglishd08",
        "com.smart.tv.launcher",
    )

    /** Never redirected, even if they resolve HOME (Settings hosts FallbackHome, needed while booting). */
    val NEVER = setOf("com.android.settings", "com.android.systemui", "android")

    fun candidates(resolved: Set<String>, ownPackage: String): Set<String> = resolved - NEVER - ownPackage

    fun isStockHome(packageName: String?, resolved: Set<String>, ownPackage: String): Boolean =
        packageName != null && packageName != ownPackage && packageName !in NEVER &&
            (packageName in KNOWN || packageName in resolved)
}
