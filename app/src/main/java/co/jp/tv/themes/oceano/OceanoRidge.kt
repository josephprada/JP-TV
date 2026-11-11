package co.jp.tv.themes.oceano

import androidx.compose.ui.graphics.Path

/**
 * A reef skyline in design px (1920x1080). The artboard ships each skyline as a pre-rendered
 * polyline sampled every 6-8 px; here it is a Catmull-Rom curve through a few anchor heights read
 * off the artboard, sampled the same way, so the shape is generated rather than pasted.
 *
 * [anchors] alternates x and y and must be sorted by x.
 */
class OceanoRidge(private val anchors: FloatArray) {
    init {
        require(anchors.size >= 4 && anchors.size % 2 == 0) { "need at least two (x, y) anchors" }
    }

    private val count = anchors.size / 2
    private fun ax(i: Int) = anchors[i.coerceIn(0, count - 1) * 2]
    private fun ay(i: Int) = anchors[i.coerceIn(0, count - 1) * 2 + 1]

    val startX: Float get() = ax(0)
    val endX: Float get() = ax(count - 1)

    /** Skyline height at [x]; flat outside the anchors. */
    fun y(x: Float): Float {
        if (x <= startX) return ay(0)
        if (x >= endX) return ay(count - 1)
        var i = 0
        while (i < count - 2 && x > ax(i + 1)) i++
        val t = (x - ax(i)) / (ax(i + 1) - ax(i))
        val p0 = ay(i - 1)
        val p1 = ay(i)
        val p2 = ay(i + 1)
        val p3 = ay(i + 2)
        val t2 = t * t
        val t3 = t2 * t
        return 0.5f * (
            2f * p1 +
                (-p0 + p2) * t +
                (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
                (-p0 + 3f * p1 - 3f * p2 + p3) * t3
            )
    }

    /** Closed silhouette from the skyline down to [bottom], sampled every [step] px. */
    fun silhouette(bottom: Float, step: Float): Path {
        val path = Path()
        path.moveTo(startX, bottom)
        var x = startX
        while (x < endX) {
            path.lineTo(x, y(x))
            x += step
        }
        path.lineTo(endX, y(endX))
        path.lineTo(endX, bottom)
        path.close()
        return path
    }
}
