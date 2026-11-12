package co.jp.tv.themes.oceano

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Palette indices of the pixel scene. Sprites and the baked backdrop store these bytes instead of
 * colors; [OceanoPalette.day] and [OceanoPalette.night] map them to ARGB. 0 is transparent, 1..24 are
 * the 24 scene colors, 25..27 are the per-app jelly ramp (dark, base, light).
 */
internal object Px {
    const val CLEAR = 0

    // Water column, lightest first: Hi is the ray / caustic / sun color, W0 the surface, W4 the abyss.
    const val HI = 1
    const val W0 = 2
    const val W1 = 3
    const val W2 = 4
    const val W3 = 5
    const val W4 = 6
    const val WATER_FIRST = 1
    const val WATER_LAST = 6

    const val FAR = 7
    const val SAND_LIGHT = 8
    const val SAND_MID = 9
    const val SAND_DARK = 10
    const val SAND_FIRST = 8
    const val SAND_LAST = 10
    const val ROCK_DARK = 11
    const val ROCK_MID = 12
    const val PURPLE = 13
    const val ORANGE = 14
    const val RED = 15
    const val GREEN = 16
    const val GREEN_LIGHT = 17
    const val GOLD = 18
    const val WHITE = 19
    const val INK = 20
    const val SHELL = 21
    const val BLUE = 22

    /** Day: silver and pink. Night: the two bioluminescent glow colors (the day-only silver school is not drawn). */
    const val SILVER = 23
    const val PINK = 24
    const val GLOW_A = 23
    const val GLOW_B = 24

    const val JELLY_DARK = 25
    const val JELLY_BASE = 26
    const val JELLY_LIGHT = 27

    const val SCENE_COLORS = 24
    const val JELLY_FIRST = 25

    /** One step lighter in the water, sand and caustic ramps; other colors stay. */
    fun lighten(index: Int): Int = when (index) {
        in W0..W4 -> index - 1
        SAND_MID, SAND_DARK -> index - 1
        else -> index
    }
}

/**
 * Fixed palette of the Océano home (design/project/ThemeOceano.dc.html), cut down to 24 scene colors per
 * phase for the pixel scene. The only file of the theme with literal colors. The night palette is the day
 * one pushed into the deep blue, except the water ramp and the two glow colors, which are chosen by hand.
 */
object OceanoPalette {
    private fun hex(v: Long): Int = v.toInt()

    /** One tint per jelly, in the artboard's order; real apps take them by index modulo size. */
    val JellyTints: List<Color> = listOf(
        Color(0xFFB17BFF),
        Color(0xFFFF4FA8),
        Color(0xFFFF5A78),
        Color(0xFF3DFFB0),
        Color(0xFF3DE6FF),
        Color(0xFF6F9BFF),
        Color(0xFFFFAB3D),
        Color(0xFFD8FF3D),
        Color(0xFF7FD8FF),
        Color(0xFFFF6FE8),
    )

    private val DEEP = hex(0xFF010B1D)


    private val dayColors: IntArray = intArrayOf(
        0x00000000,
        // water: Hi, surface, shallow, mid, deep, abyss
        hex(0xFF8FDDF2), hex(0xFF1597CF), hex(0xFF0A6AA8), hex(0xFF074F86), hex(0xFF04355F), hex(0xFF021D3A),
        // far reef in haze
        hex(0xFF11609C),
        // sand: light, mid, dark
        hex(0xFFA9CFC4), hex(0xFF5F8F8A), hex(0xFF2A5E78),
        // rock: dark, mid
        hex(0xFF0B1B28), hex(0xFF284558),
        // coral: purple, orange, red; green, light green
        hex(0xFF8845B6), hex(0xFFD6763B), hex(0xFFE0302A), hex(0xFF4C8636), hex(0xFF7FE05A),
        // gold, white, ink, turtle shell, fish blue
        hex(0xFFFFD23A), hex(0xFFE6FBFF), hex(0xFF0A1A2A), hex(0xFF8A6634), hex(0xFF3FA0E8),
        // silver, pink
        hex(0xFFC8E4F2), hex(0xFFFF6FAE),
    )

    private val nightWater = intArrayOf(
        hex(0xFF1C5C93), hex(0xFF0B3A6C), hex(0xFF062A55), hex(0xFF041F42), hex(0xFF021430), hex(0xFF010B1D),
    )

    private val nightColors: IntArray = IntArray(dayColors.size) { i ->
        when (i) {
            0 -> 0
            in Px.WATER_FIRST..Px.WATER_LAST -> nightWater[i - Px.WATER_FIRST]
            Px.GLOW_A -> hex(0xFF3DFFE0)
            Px.GLOW_B -> hex(0xFFFF6FE8)
            Px.WHITE -> hex(0xFFB8D6E8)
            else -> shade(dayColors[i], DEEP, 0.55f)
        }
    }

    /** Colors of one tint ramp: dark, base, light. */
    private fun jellyRamp(tint: Color, out: IntArray, at: Int) {
        val base = tint.toArgb()
        out[at] = shade(base, hex(0xFF0A1A3A), 0.5f)
        out[at + 1] = base
        out[at + 2] = shade(base, hex(0xFFFFFFFF), 0.55f)
    }

    private val jellyRamps = IntArray(JellyTints.size * 3).also { r -> JellyTints.forEachIndexed { i, c -> jellyRamp(c, r, i * 3) } }

    /** Scene colors by palette index for the day, ARGB. Index 0 is transparent. */
    fun day(): IntArray = dayColors

    /** Scene colors by palette index for the night, ARGB. */
    fun night(): IntArray = nightColors

    /** Jelly ramps, three ARGB colors (dark, base, light) per tint; the same in both phases, the jellies are lights. */
    fun jelly(): IntArray = jellyRamps

    /** Blend of [from] toward [to] by [amount], opaque. */
    fun shade(from: Int, to: Int, amount: Float): Int {
        val a = (amount * 256f).toInt().coerceIn(0, 256)
        val r = (((from shr 16) and 255) * (256 - a) + ((to shr 16) and 255) * a) shr 8
        val g = (((from shr 8) and 255) * (256 - a) + ((to shr 8) and 255) * a) shr 8
        val b = ((from and 255) * (256 - a) + (to and 255) * a) shr 8
        return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
    }

    // Text and controls, drawn at screen resolution over the pixel scene.
    val Text = Color(0xFFE6FBFF)
    val White = Color(0xFFFFFFFF)

    /** Hard, blur-free drop shadow under pixel-font text. */
    val TextShadow = Color(0xFF031E3C)
    val ControlFocus = Color(0x33FFFFFF)
    val ScrubPosition = Color(0xD9EBFCFF)

    /** The scrubber's tick colors and the focused tick, as ARGB for the pixel buffer. */
    val ScrubTick: Int = hex(0xFFA9D8EC)
    val ScrubTickOn: Int = hex(0xFFFFFFFF)

    fun jellyTintIndex(index: Int): Int = Math.floorMod(index, JellyTints.size)

    fun jellyTint(index: Int): Color = JellyTints[jellyTintIndex(index)]
}
