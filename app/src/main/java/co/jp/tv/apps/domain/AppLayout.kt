package co.jp.tv.apps.domain

/** Ordering rules for the app rail. */
object AppLayout {
    private val byLabel = compareBy<TvApp>({ it.label.lowercase() }, { it.packageName })

    /** Apps pinned to the top of the rail on the first run, when installed. */
    val FAVORITES = listOf(
        "com.xuper.netxxus",
        "com.google.android.youtube.tv",
        "com.netflix.mediaclient",
        "com.spotify.tv.android",
    )

    /** System and utility packages hidden on the first run, when installed. */
    val FIRST_RUN_HIDDEN = setOf(
        "com.android.tv.settings",
        "com.droidlogic.tv.settings",
        "com.droidlogic.FileBrower",
        "com.droidlogic.otaupgrade",
        "com.droidlogic.videoplayer",
    )

    /** TV apps first, then the rest; each group alphabetical. Our own package is never listed. */
    fun arrange(apps: List<TvApp>, ownPackage: String): List<TvApp> {
        val (tv, other) = apps
            .filter { it.packageName != ownPackage }
            .partition { it.isTvApp }
        return tv.sortedWith(byLabel) + other.sortedWith(byLabel)
    }

    /** Arrangement used when nothing is saved yet, from the packages in default order. */
    fun firstRun(defaultOrder: List<String>): AppOrdering {
        val installed = defaultOrder.toSet()
        val favorites = FAVORITES.filter { it in installed }
        return AppOrdering(
            order = favorites + defaultOrder.filter { it !in favorites },
            hidden = FIRST_RUN_HIDDEN.filter { it in installed }.toSet(),
        )
    }

    /** Splits [installed] into the visible and hidden apps of [ordering]; unknown packages are ignored. */
    fun resolve(installed: List<TvApp>, ordering: AppOrdering): ResolvedApps {
        val byPackage = installed.associateBy { it.packageName }
        return ResolvedApps(
            visible = ordering.visible.mapNotNull(byPackage::get),
            hidden = ordering.hiddenOrdered.mapNotNull(byPackage::get),
        )
    }

    data class ResolvedApps(val visible: List<TvApp>, val hidden: List<TvApp>)
}
