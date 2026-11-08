package co.jp.tv.apps.domain

/** Ordering rules for the app rail. */
object AppLayout {
    private val byLabel = compareBy<TvApp>({ it.label.lowercase() }, { it.packageName })

    /** TV apps first, then the rest; each group alphabetical. Our own package is never listed. */
    fun arrange(apps: List<TvApp>, ownPackage: String): List<TvApp> {
        val (tv, other) = apps
            .filter { it.packageName != ownPackage }
            .partition { it.isTvApp }
        return tv.sortedWith(byLabel) + other.sortedWith(byLabel)
    }
}
