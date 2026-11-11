package co.jp.tv.themes.oceano

import kotlin.random.Random

/**
 * Every fish of the artboard, grouped by the layer it swims in. Counts are trimmed from the
 * artboard (about 340 fish) to about 190 so a TV box keeps 60 fps; schools, paths, speeds and
 * the regions the hovering fish keep to follow the artboard. Built once from a fixed seed.
 */
internal class OceanoShoals private constructor(shapes: OceanoFishShapes) {
    private val random = Random(21)

    /** Blue-haze snappers, a far silverside school, a shark and a manta (always shown). */
    val far: List<OceanoSwimmer>

    /** Anthias hovering over the reef and their school (dimmed to 0.5 at night). */
    val mid: List<OceanoSwimmer>

    /** Silverside and yellowtail schools (day only). */
    val daySchools: List<OceanoSwimmer>

    /** Sergeant majors, angelfish and a snapper crossing the screen (always shown). */
    val crossing: List<OceanoSwimmer>

    /** Close anthias around the reef (dimmed to 0.5 at night). */
    val close: List<OceanoSwimmer>

    /** A close anthias school over the left reef (day only). */
    val closeSchool: List<OceanoSwimmer>

    init {
        far = buildList {
            repeat(18) {
                val w = 21f + random.nextFloat() * 7f
                add(
                    OceanoSwimmer(
                        shapes.snapper, w, w / 2.8f, 0.35f, OceanoSwimmer.Motion.Hover,
                        x = 900f + random.nextFloat() * 850f,
                        y = 400f + random.nextFloat() * 240f,
                        facing = if (random.nextBoolean()) 1f else -1f,
                        periodS = 5.5f + random.nextFloat() * 2.6f,
                        delayS = random.nextFloat() * 5.5f,
                        a = 28f,
                        b = -8f,
                        tint = OceanoPalette.FarSnapper,
                    ),
                )
            }
            addAll(orbit(shapes.silverside, 16, 1100f, 470f, 620f, 90f, 74f, reverse = true, 17f, 23f, 8f, 0.5f))
            add(
                OceanoSwimmer(
                    shapes.shark, 300f, 105f, 0.6f, OceanoSwimmer.Motion.Cross,
                    x = 0f, y = 330f, facing = 1f, periodS = 120f, delayS = 52f,
                    tint = OceanoPalette.FarShark, wagS = 2.6f, wagDeg = 1.2f,
                ),
            )
            add(
                OceanoSwimmer(
                    shapes.manta, 230f, 130f, 0.5f, OceanoSwimmer.Motion.Cross,
                    x = 0f, y = 200f, facing = -1f, periodS = 150f, delayS = 24f,
                    tint = OceanoPalette.FarManta, flapS = 3.2f,
                ),
            )
        }
        mid = buildList {
            repeat(30) { i ->
                val left = i % 3 == 0
                val w = 14f + random.nextFloat() * 10f
                add(
                    reefFish(
                        if (random.nextFloat() < 0.6f) shapes.anthias else shapes.anthiasFar,
                        w,
                        x = if (left) 150f + random.nextFloat() * 500f else 1150f + random.nextFloat() * 700f,
                        y = if (left) 320f + random.nextFloat() * 360f else 190f + random.nextFloat() * 370f,
                        alpha = 0.58f + random.nextFloat() * 0.24f,
                    ),
                )
            }
            addAll(orbit(shapes.anthias, 18, 1560f, 380f, 300f, 150f, 36f, reverse = false, 29f, 33f, 13f, 0.8f))
        }
        daySchools = orbit(shapes.silverside, 26, 960f, 300f, 820f, 170f, 54f, reverse = true, 32f, 37f, 13f, 1f) +
            orbit(shapes.yellowtail, 14, 380f, 560f, 300f, 120f, 42f, reverse = false, 37f, 41f, 19f, 1f)
        crossing = buildList {
            val sergeants = floatArrayOf(3f, 186f, 62f, 187f, 119f, 188f, 185f, 187f, 19f, 226f, 74f, 227f, 123f, 223f)
            val bobs = floatArrayOf(1.7f, 2.2f, 2.3f, 1.5f, 1.7f, 1.9f, 2.0f)
            for (i in bobs.indices) {
                add(
                    OceanoSwimmer(
                        shapes.sergeant, 44f, 22f, 1f, OceanoSwimmer.Motion.Cross,
                        x = sergeants[i * 2], y = sergeants[i * 2 + 1], facing = 1f, periodS = 64f, delayS = 10f,
                        wagS = 0.3f, wagDeg = 3f, bobS = bobs[i], bobPx = 6f,
                    ),
                )
            }
            add(
                OceanoSwimmer(
                    shapes.angel, 78f, 54f, 1f, OceanoSwimmer.Motion.Cross,
                    x = 20f, y = 544f, facing = -1f, periodS = 88f, delayS = 30f, bobS = 1.5f, bobPx = 6f,
                ),
            )
            add(
                OceanoSwimmer(
                    shapes.angel, 64f, 44f, 1f, OceanoSwimmer.Motion.Cross,
                    x = 1f, y = 656f, facing = -1f, periodS = 88f, delayS = 62f, bobS = 1.9f, bobPx = 6f,
                ),
            )
            add(
                OceanoSwimmer(
                    shapes.snapper, 120f, 45f, 0.55f, OceanoSwimmer.Motion.Cross,
                    x = 3f, y = 465f, facing = 1f, periodS = 110f, delayS = 40f,
                    tint = OceanoPalette.CrossSnapper, bobS = 1.5f, bobPx = 6f,
                ),
            )
        }
        close = List(24) { i ->
            val left = i % 4 == 0
            reefFish(
                shapes.anthias,
                21f + random.nextFloat() * 13f,
                x = if (left) 600f + random.nextFloat() * 80f else 1100f + random.nextFloat() * 750f,
                y = if (left) 490f + random.nextFloat() * 200f else 190f + random.nextFloat() * 410f,
                alpha = 1f,
            )
        }
        closeSchool = orbit(shapes.anthias, 12, 700f, 720f, 280f, 70f, 19f, reverse = false, 22f, 27f, 10f, 1f)
    }

    /** A hovering or darting reef fish, as in the artboard's `sw-hov-*` / `sw-dart-*` mix. */
    private fun reefFish(shape: OceanoFishShapes.Shape, width: Float, x: Float, y: Float, alpha: Float): OceanoSwimmer {
        val right = random.nextBoolean()
        val dart = random.nextFloat() < 0.45f
        val period = 3.4f + random.nextFloat() * 4.5f
        val delay = random.nextFloat() * 6f
        return if (dart) {
            OceanoSwimmer(
                shape, width, width * 0.42f, alpha, OceanoSwimmer.Motion.Dart,
                x = x, y = y, facing = if (right) 1f else -1f, periodS = period, delayS = delay,
                a = if (right) 80f else -80f, b = if (right) -16f else 14f,
                c = if (right) 10f else -10f, d = if (right) 4f else -4f,
                wagS = 0.3f,
            )
        } else {
            val kind = random.nextInt(2)
            OceanoSwimmer(
                shape, width, width * 0.42f, alpha, OceanoSwimmer.Motion.Hover,
                x = x, y = y, facing = if (right) 1f else -1f, periodS = period, delayS = delay,
                a = if (right) { if (kind == 0) 28f else 18f } else { if (kind == 0) -28f else -16f },
                b = if (right) { if (kind == 0) -8f else 10f } else { if (kind == 0) 6f else -12f },
                wagS = 0.35f,
            )
        }
    }

    /** A school spread over about 6 s of its loop along one ellipse, each fish slightly offset. */
    private fun orbit(
        shape: OceanoFishShapes.Shape,
        count: Int,
        cx: Float,
        cy: Float,
        rx: Float,
        ry: Float,
        periodS: Float,
        reverse: Boolean,
        minWidth: Float,
        maxWidth: Float,
        height: Float,
        alpha: Float,
    ): List<OceanoSwimmer> = List(count) {
        OceanoSwimmer(
            shape,
            minWidth + random.nextFloat() * (maxWidth - minWidth),
            height,
            alpha,
            OceanoSwimmer.Motion.Orbit,
            x = (random.nextFloat() - 0.5f) * 40f,
            y = (random.nextFloat() - 0.5f) * 24f,
            facing = 1f,
            periodS = periodS,
            delayS = 0.35f + random.nextFloat() * 6f,
            a = cx,
            b = cy,
            c = rx,
            d = ry,
            reverse = reverse,
            wagS = 0.25f + random.nextFloat() * 0.15f,
        )
    }

    companion object {
        fun build(shapes: OceanoFishShapes): OceanoShoals = OceanoShoals(shapes)
    }
}
