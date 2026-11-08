package co.jp.tv.apps.domain

/**
 * The user's arrangement of apps: every known package in [order] (hidden ones keep their slot)
 * plus the [hidden] set. Pure value; every operation returns a new instance.
 */
data class AppOrdering(
    val order: List<String>,
    val hidden: Set<String>,
) {
    /** Packages shown on the rail, in order. */
    val visible: List<String> get() = order.filter { it !in hidden }

    /** Packages kept off the rail, in order. */
    val hiddenOrdered: List<String> get() = order.filter { it in hidden }

    /**
     * Aligns a saved arrangement with what is installed now: saved packages first (in saved order),
     * new packages appended in [defaultOrder]; packages that are gone are dropped silently.
     */
    fun reconcile(defaultOrder: List<String>): AppOrdering {
        val installed = defaultOrder.toSet()
        val known = order.filter { it in installed }.distinct()
        val knownSet = known.toSet()
        return AppOrdering(
            order = known + defaultOrder.filter { it !in knownSet },
            hidden = hidden.filter { it in installed }.toSet(),
        )
    }

    fun moveUp(packageName: String): AppOrdering {
        val index = visible.indexOf(packageName)
        return if (index <= 0) this else moveTo(packageName, index - 1)
    }

    fun moveDown(packageName: String): AppOrdering {
        val index = visible.indexOf(packageName)
        return if (index < 0 || index >= visible.lastIndex) this else moveTo(packageName, index + 1)
    }

    /** Places a visible app at [visibleIndex] among the visible apps (clamped). */
    fun moveTo(packageName: String, visibleIndex: Int): AppOrdering {
        if (packageName !in visible) return this
        val others = visible - packageName
        val target = visibleIndex.coerceIn(0, others.size)
        val rest = order - packageName
        val insertAt = if (target < others.size) rest.indexOf(others[target]) else rest.indexOf(others.last()) + 1
        return copy(order = rest.take(insertAt) + packageName + rest.drop(insertAt))
    }

    fun hide(packageName: String): AppOrdering =
        if (packageName in order) copy(hidden = hidden + packageName) else this

    fun show(packageName: String): AppOrdering =
        if (packageName in hidden) copy(hidden = hidden - packageName) else this
}
