package co.jp.tv.themes.shared

/** Scroll-window maths for a rail that shows [size] rows out of [count]. */
object RailWindow {
    fun top(previousTop: Int, focus: Int, count: Int, size: Int): Int {
        if (count <= size) return 0
        val maxTop = count - size
        val moved = when {
            focus < previousTop -> focus
            focus >= previousTop + size -> focus - size + 1
            else -> previousTop
        }
        return moved.coerceIn(0, maxTop)
    }

    fun above(top: Int): Int = top

    fun below(top: Int, count: Int, size: Int): Int = (count - (top + size)).coerceAtLeast(0)
}
