package co.jp.tv.themes.oceano

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import kotlin.random.Random

/**
 * Static reef geometry of the artboard, in design px, built once: the far reef in blue haze, the
 * sand floor with the mid reef, and the three foreground mounds, each with its corals.
 */
internal class OceanoReefScenery private constructor() {
    // Far reef: two hazy skylines with rounded bumps, and three rock mounds.
    private val farBackRidge = OceanoRidge(
        floatArrayOf(
            -40f, 696f, 50f, 664f, 146f, 635f, 242f, 624f, 338f, 612f, 434f, 588f, 530f, 572f,
            626f, 592f, 722f, 639f, 818f, 669f, 914f, 652f, 1010f, 608f, 1106f, 576f, 1202f, 564f,
            1298f, 552f, 1394f, 535f, 1490f, 537f, 1586f, 578f, 1682f, 637f, 1778f, 675f, 1874f, 680f,
            1980f, 675f,
        ),
    )
    private val farFrontRidge = OceanoRidge(
        floatArrayOf(
            -40f, 715f, 50f, 702f, 146f, 695f, 242f, 684f, 338f, 676f, 434f, 690f, 530f, 722f,
            626f, 747f, 722f, 741f, 818f, 706f, 914f, 666f, 1010f, 638f, 1106f, 628f, 1202f, 632f,
            1298f, 647f, 1394f, 672f, 1490f, 705f, 1586f, 737f, 1682f, 758f, 1778f, 762f, 1874f, 754f,
            1980f, 750f,
        ),
    )
    val farBack: Path = farBackRidge.silhouette(bottom = 1000f, step = 6f)
    val farFront: Path = farFrontRidge.silhouette(bottom = 1000f, step = 6f)

    /** Bumps along the skylines, packed as x, y, radius. */
    val farBackBumps: FloatArray = bumps(farBackRidge, seed = 11)
    val farFrontBumps: FloatArray = bumps(farFrontRidge, seed = 23)
    val farBommies: List<Path> = listOf(180f, 980f, 1560f).map { bommie(it) }

    // Sand floor, its ripples, and the mid reef band.
    val sandBrush: Brush = Brush.verticalGradient(
        listOf(OceanoPalette.SandTop, OceanoPalette.SandBottom),
        startY = 840f,
        endY = 1100f,
    )
    val rippleDark: Path = Path()
    val rippleLight: Path = Path()
    private val midRidge = OceanoRidge(
        floatArrayOf(
            -20f, 857f, 12f, 843f, 92f, 822f, 172f, 808f, 252f, 776f, 332f, 785f, 412f, 797f,
            492f, 811f, 572f, 844f, 652f, 836f, 732f, 832f, 812f, 815f, 892f, 769f, 972f, 751f,
            1052f, 723f, 1132f, 717f, 1212f, 747f, 1292f, 760f, 1372f, 795f, 1452f, 805f, 1532f, 787f,
            1612f, 792f, 1692f, 787f, 1772f, 812f, 1852f, 848f, 1932f, 851f, 1960f, 856f,
        ),
    )
    val midReef: Path = midRidge.silhouette(bottom = 905f, step = 8f)
    val midReefBrush: Brush = Brush.verticalGradient(
        listOf(OceanoPalette.MidReefTop, OceanoPalette.MidReefBottom),
        startY = 712f,
        endY = 905f,
    )
    val midDressing: OceanoReefDressing = OceanoReefDressing.along(
        midRidge,
        fromX = 0f,
        toX = 1900f,
        seed = 31,
        style = OceanoReefDressing.Style(
            scale = 0.45f,
            brainColors = listOf(
                OceanoPalette.MidBrainKhaki,
                OceanoPalette.MidBrainBrown,
                OceanoPalette.MidBrainMoss,
                OceanoPalette.MidBrainSteel,
            ),
            tableColors = listOf(
                OceanoReefDressing.TableColors(OceanoPalette.MidBrainKhaki, OceanoPalette.TableSandUnder, OceanoPalette.TableSandStem),
            ),
            tubeColors = listOf(
                OceanoReefDressing.TubeColors(OceanoPalette.MidTubeViolet, OceanoPalette.MidTubeVioletMouth, OceanoPalette.MidTubeVioletRim),
                OceanoReefDressing.TubeColors(OceanoPalette.MidTubeRust, OceanoPalette.MidTubeRustMouth, OceanoPalette.MidTubeRustRim),
            ),
            branchColors = listOf(OceanoPalette.MidBranchNavy, OceanoPalette.MidBranchPlum),
        ),
    )

    // Foreground mounds: left, a low middle one, and the tall right one.
    private val foreRidges = listOf(
        OceanoRidge(
            floatArrayOf(
                -20f, 949f, 20f, 898f, 68f, 837f, 116f, 791f, 164f, 770f, 212f, 782f, 260f, 820f,
                308f, 869f, 356f, 905f, 404f, 912f, 452f, 893f, 500f, 877f, 548f, 896f, 596f, 950f,
                644f, 1012f, 692f, 1056f, 740f, 1079f, 760f, 1090f,
            ),
        ),
        OceanoRidge(
            floatArrayOf(
                700f, 1083f, 740f, 1067f, 788f, 1033f, 836f, 1010f, 884f, 1027f, 932f, 1061f,
                980f, 1078f, 1028f, 1065f, 1076f, 1032f, 1124f, 1022f, 1172f, 1052f, 1220f, 1079f,
                1260f, 1090f,
            ),
        ),
        OceanoRidge(
            floatArrayOf(
                1180f, 1066f, 1220f, 1034f, 1268f, 964f, 1316f, 867f, 1364f, 778f, 1412f, 734f,
                1460f, 736f, 1508f, 747f, 1556f, 733f, 1604f, 694f, 1652f, 649f, 1700f, 621f,
                1748f, 619f, 1796f, 637f, 1844f, 660f, 1892f, 681f, 1940f, 718f, 1960f, 742f,
            ),
        ),
    )
    val foreMounds: List<Path> = foreRidges.map { it.silhouette(bottom = 1090f, step = 8f) }
    val foreBrushes: List<Brush> = listOf(770f, 1010f, 619f).map { top ->
        Brush.verticalGradient(
            listOf(OceanoPalette.ForeReefTop, OceanoPalette.ForeReefBottom),
            startY = top,
            endY = 1090f,
        )
    }
    private val foreStyle = OceanoReefDressing.Style(
        scale = 1f,
        brainColors = listOf(
            OceanoPalette.BrainGreen,
            OceanoPalette.BrainOlive,
            OceanoPalette.BrainOchre,
            OceanoPalette.BrainMauve,
            OceanoPalette.BrainBlue,
        ),
        tableColors = listOf(
            OceanoReefDressing.TableColors(OceanoPalette.TableSand, OceanoPalette.TableSandUnder, OceanoPalette.TableSandStem),
            OceanoReefDressing.TableColors(OceanoPalette.TableBlue, OceanoPalette.TableBlueUnder, OceanoPalette.TableBlueStem),
        ),
        tubeColors = listOf(
            OceanoReefDressing.TubeColors(OceanoPalette.TubePurple, OceanoPalette.TubePurpleMouth, OceanoPalette.TubePurpleRim),
            OceanoReefDressing.TubeColors(OceanoPalette.TubeOrange, OceanoPalette.TubeOrangeMouth, OceanoPalette.TubeOrangeRim),
        ),
        branchColors = listOf(OceanoPalette.BranchTan, OceanoPalette.BranchPlum, OceanoPalette.BranchBlue),
    )
    val foreDressings: List<OceanoReefDressing> = listOf(
        OceanoReefDressing.along(foreRidges[0], fromX = 20f, toX = 640f, seed = 41, style = foreStyle),
        OceanoReefDressing.along(foreRidges[1], fromX = 760f, toX = 1200f, seed = 43, style = foreStyle),
        OceanoReefDressing.along(foreRidges[2], fromX = 1250f, toX = 1950f, seed = 47, style = foreStyle),
    )

    /**
     * Night-only lights packed as x, y, radius, color index into [OceanoPalette.GlowColors],
     * `sw-tw` period, delay: 45 glowing coral tips on the foreground mounds, then 107 plankton
     * specks in open water.
     */
    val nightLights: FloatArray = run {
        val random = Random(13)
        val out = ArrayList<Float>()
        repeat(45) {
            val mound = if (random.nextFloat() < 0.4f) 0 else 2
            val ridge = foreRidges[mound]
            val x = if (mound == 0) 30f + random.nextFloat() * 540f else 1200f + random.nextFloat() * 700f
            out += x
            out += (ridge.y(x) + 15f + random.nextFloat() * 120f).coerceAtMost(1070f)
            out += 2f + random.nextInt(2) * 0.5f + random.nextFloat()
            out += random.nextInt(OceanoPalette.GlowColors.size).toFloat()
            out += 2f + random.nextFloat() * 3f
            out += random.nextFloat() * 5f
        }
        repeat(107) {
            out += random.nextFloat() * OceanoDesign.WIDTH
            out += 88f + random.nextFloat() * 943f
            out += if (random.nextFloat() < 0.66f) 1f else 1.5f
            out += random.nextInt(3).toFloat()
            out += 2f + random.nextFloat() * 3f
            out += random.nextFloat() * 5f
        }
        out.toFloatArray()
    }

    init {
        val random = Random(5)
        repeat(46) { i ->
            val y = 865f + random.nextFloat() * 205f
            val len = 90f + random.nextFloat() * 170f
            val x = -20f + random.nextFloat() * 1900f
            val target = if (i % 2 == 0) rippleDark else rippleLight
            target.moveTo(x, y)
            target.relativeQuadraticBezierTo(len / 2f, -(3.4f + (y - 865f) / 80f), len, 0f)
        }
    }

    private fun bumps(ridge: OceanoRidge, seed: Int): FloatArray {
        val random = Random(seed)
        val out = ArrayList<Float>()
        var x = ridge.startX + 10f
        while (x < ridge.endX) {
            out += x
            out += ridge.y(x) + 2f + random.nextFloat() * 3f
            out += 6f + random.nextFloat() * 10f
            x += 19f
        }
        return out.toFloatArray()
    }

    /** The artboard's rock mound, a rounded dome 125 px wide, centred on [cx]. */
    private fun bommie(cx: Float): Path = Path().apply {
        val x = cx - 180f
        moveTo(x + 182.5f, 780f)
        quadraticBezierTo(x + 120f, 780f, x + 125f, 710f)
        quadraticBezierTo(x + 130f, 640f, x + 145f, 620f)
        quadraticBezierTo(x + 160f, 600f, x + 180f, 605f)
        quadraticBezierTo(x + 200f, 610f, x + 215f, 630f)
        quadraticBezierTo(x + 230f, 650f, x + 237.5f, 715f)
        quadraticBezierTo(x + 245f, 780f, x + 182.5f, 780f)
        close()
    }

    companion object {
        fun build(): OceanoReefScenery = OceanoReefScenery()
    }
}
