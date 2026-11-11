package co.jp.tv.themes.oceano

import androidx.compose.ui.graphics.Color

/**
 * Fixed palette of the Océano home (design/project/ThemeOceano.dc.html). The only file of the
 * theme with literal colors. Day and night values that differ are named separately; the theme
 * crossfades between them in 2.5 s like the artboard's `opacity 2.5s ease` transitions.
 */
object OceanoPalette {
    // Water column, top to bottom (stops 0, .14, .36, .62, 1).
    val WaterSurface = Color(0xFF1597CF)
    val WaterShallow = Color(0xFF0A6AA8)
    val WaterMid = Color(0xFF074F86)
    val WaterDeep = Color(0xFF04355F)
    val WaterAbyss = Color(0xFF021D3A)

    val Text = Color(0xFFE6FBFF)
    val White = Color(0xFFFFFFFF)
    val TextShadow = Color(0x99031E3C)
    val InitialShadow = Color(0xB3031E3C)
    val LabelShadow = Color(0xE6010C1A)
    val LabelShadowSoft = Color(0xFF010C1A)

    // Sunlit window at the surface.
    val SunCore = Color(0xB3E1FAFF)
    val SunHalo = Color(0x40A0E6FF)
    val SunEdge = Color(0x00A0E6FF)

    // Caustics: the design's feColorMatrix output, rgb(0.85, 0.97, 1).
    val Caustic = Color(0xFFD9F7FF)

    // God rays.
    val RayTop = Color(0xFFBEF0FF)
    val RayMid = Color(0x0FA0E1FF)
    val RayEnd = Color(0x00CDF5FF)

    val Snow = Color(0xFFE1F8FF)
    val FrontParticle = Color(0x59DCFAFF)
    val FrontParticleEdge = Color(0x00DCFAFF)

    // Far reef in blue haze.
    val FarRidgeBack = Color(0xFF11609C)
    val FarRidgeFront = Color(0xFF0D5089)
    val FarBommie = Color(0xFF0F5892)
    val FarSnapper = Color(0xFF0D3A63)
    val FarShark = Color(0xFF082A4C)
    val FarManta = Color(0xFF0A2F55)
    val CrossSnapper = Color(0xFF123F66)

    // Sand floor and mid reef.
    val SandTop = Color(0xFF1F5F86)
    val SandBottom = Color(0xFF5F8F8A)
    val SandRippleDark = Color(0xFF163F5C)
    val SandRippleLight = Color(0xFFCFE6DC)
    val MidReefTop = Color(0xFF1C4361)
    val MidReefBottom = Color(0xFF0A263E)
    val ForeReefTop = Color(0xFF284558)
    val ForeReefBottom = Color(0xFF0B1B28)
    val ReefShadow = Color(0x59021A33)

    // Reef dressing (radial "rg*" and horizontal "hg*" gradients of the artboard).
    val BrainGreen = listOf(Color(0xFFAACB8E), Color(0xFF7CAF51), Color(0xFF3E5828))
    val BrainOlive = listOf(Color(0xFFA7B885), Color(0xFF789144), Color(0xFF3C4822))
    val BrainOchre = listOf(Color(0xFFC4AE85), Color(0xFFA58344), Color(0xFF524222))
    val BrainMauve = listOf(Color(0xFFB494B5), Color(0xFF8C5A8D), Color(0xFF462D46))
    val BrainBlue = listOf(Color(0xFF98C1D5), Color(0xFF619FBE), Color(0xFF30505F))
    val MidBrainKhaki = listOf(Color(0xFFA5A08C), Color(0xFF756D4E), Color(0xFF3A3627))
    val MidBrainBrown = listOf(Color(0xFFAC9F82), Color(0xFF7F6B3E), Color(0xFF40361F))
    val MidBrainMoss = listOf(Color(0xFF91A78C), Color(0xFF56774E), Color(0xFF2B3C27))
    val MidBrainSteel = listOf(Color(0xFF8AB0C5), Color(0xFF4B86A6), Color(0xFF264353))
    val TableSand = listOf(Color(0xFFD2BF92), Color(0xFFBA9D58), Color(0xFF5D4E2C))
    val TableSandUnder = Color(0xFF756130)
    val TableSandStem = Color(0xFF6C592C)
    val TableBlue = listOf(Color(0xFF98C1D5), Color(0xFF619FBE), Color(0xFF30505F))
    val TableBlueUnder = Color(0xFF366278)
    val TableBlueStem = Color(0xFF325B6E)
    val TubePurple = listOf(Color(0xFF340851), Color(0xFF8845B6), Color(0xFF6710A2), Color(0xFF2E0749))
    val TubePurpleMouth = Color(0xFF1D042D)
    val TubePurpleRim = Color(0xFF9558BE)
    val TubeOrange = listOf(Color(0xFF652802), Color(0xFFD6763B), Color(0xFFCA5004), Color(0xFF5B2402))
    val TubeOrangeMouth = Color(0xFF391601)
    val TubeOrangeRim = Color(0xFFDA844F)
    val MidTubeViolet = listOf(Color(0xFF250E48), Color(0xFF724FA8), Color(0xFF4A1D90), Color(0xFF210D41))
    val MidTubeVioletMouth = Color(0xFF150828)
    val MidTubeVioletRim = Color(0xFF8061B1)
    val MidTubeRust = listOf(Color(0xFF482510), Color(0xFFA87252), Color(0xFF8F4A21), Color(0xFF40210F))
    val MidTubeRustMouth = Color(0xFF281509)
    val MidTubeRustRim = Color(0xFFA87252)
    val BranchTan = listOf(Color(0xFF7E6B44), Color(0xFF907A4E), Color(0xFF9D8555), Color(0xFFA68D5A))
    val BranchPlum = listOf(Color(0xFF723C5A), Color(0xFF824466), Color(0xFF8E4A70), Color(0xFF974F77))
    val BranchBlue = listOf(Color(0xFF2A5A7F), Color(0xFF306791), Color(0xFF34709E), Color(0xFF3777A8))
    val MidBranchNavy = listOf(Color(0xFF1F4B6E), Color(0xFF23547C), Color(0xFF265B86), Color(0xFF296291))
    val MidBranchPlum = listOf(Color(0xFF513553), Color(0xFF5D3D5F), Color(0xFF644266), Color(0xFF6C476E))

    // Swaying gorgonian fans: branch color and mesh color.
    val FanViolet = Color(0xFF9A2FC0)
    val FanVioletMesh = Color(0xFFA94EC9)
    val FanRed = Color(0xFFE0302A)
    val FanRedMesh = Color(0xFFE54F4A)
    val FanPurple = Color(0xFFA92FD0)
    val FanPurpleMesh = Color(0xFFB64ED7)
    val FanCrimson = Color(0xFFE0283C)
    val FanCrimsonMesh = Color(0xFFE54859)

    // Anemones and seagrass.
    val AnemoneGreen = Color(0xFF7FE05A)
    val AnemoneGreenTip = Color(0xFFFF9CF0)
    val AnemoneGreenBase = Color(0xFF4C8636)
    val AnemonePink = Color(0xFFFF6FAE)
    val AnemonePinkTip = Color(0xFFFFF07A)
    val AnemonePinkBase = Color(0xFF994368)
    val Seagrass = Color(0xFF5C8F5A)

    // Fish (symbols fAnth, fAnthFar, fSerg, fAngel, fSilv, fYel).
    val AnthTop = Color(0xFFFF3D14)
    val AnthMid = Color(0xFFFF7414)
    val AnthBelly = Color(0xFFFFAE5C)
    val AnthTail = Color(0xFFFF6A4A)
    val AnthFin = Color(0xFFFF7A52)
    val AnthLine = Color(0x99FFD9B0)
    val AnthEye = Color(0xFF1A0C08)
    val AnthFarTail = Color(0xFFC8552C)
    val AnthFarBody = Color(0xFFE0683A)
    val SergTop = Color(0xFFD7D98A)
    val SergMid = Color(0xFFE9EEDB)
    val SergBelly = Color(0xFFF6F8F2)
    val SergTail = Color(0xFFD9DCB0)
    val SergFin = Color(0xFFCFD59A)
    val SergBar = Color(0xBF1B1E24)
    val FishEye = Color(0xFF111111)
    val AngelBlue = Color(0xFF1F47B8)
    val AngelYellow = Color(0xFFFFD23A)
    val AngelTail = Color(0xFFFFCC2E)
    val AngelFace = Color(0xFF0D1A3A)
    val AngelLine = Color(0xFF7FD0FF)
    val SilvTop = Color(0xFF3FA0E8)
    val SilvMid = Color(0xFFA8D8FF)
    val SilvBelly = Color(0xFFF0F8FF)
    val SilvGold = Color(0xFFFFD84A)
    val SilvEye = Color(0xFF0A1A2A)
    val YelTop = Color(0xFFFFB300)
    val YelMid = Color(0xFFFFD81A)
    val YelBelly = Color(0xFFFFF3A8)
    val YelFin = Color(0xFFFFC21A)
    val YelLine = Color(0x99FFFFFF)
    val YelEye = Color(0xFF1A1200)

    // Sea turtle.
    val TurtleSkinLight = Color(0xFF9A8A62)
    val TurtleSkinDark = Color(0xFF5A4C32)
    val TurtleShellLight = Color(0xFFA07A42)
    val TurtleShellMid = Color(0xFF6E5228)
    val TurtleShellDark = Color(0xFF3E2C14)
    val TurtleOutline = Color(0xFF2E2210)
    val TurtleScute = Color(0xE68A6634)
    val TurtleRim = Color(0x80C49A5A)
    val TurtleLines = Color(0x8C3E3420)
    val TurtleEye = Color(0xFF120C06)
    val TurtleSheen = Color(0x14FFFFFF)

    // Bubbles.
    val BubbleRim = Color(0xB3E1FAFF)
    val BubbleHighlight = Color(0xCCFFFFFF)
    val BubbleClear = Color(0x0FFFFFFF)

    // Day / night.
    /** Night darkness over the scene, top and bottom (day: not drawn). */
    val NightTintTop = Color(0x8C02081E)
    val NightTintBottom = Color(0xC7010410)

    /** Bioluminescent plankton and glowing coral tips, night only. */
    val GlowColors = listOf(
        Color(0xFF3DFFE0),
        Color(0xFF7AFFC4),
        Color(0xFF9CF2FF),
        Color(0xFF7FD8FF),
        Color(0xFFFF6FE8),
        Color(0xFFB17BFF),
        Color(0xFFD8FF3D),
    )

    const val SunAlphaDay = 1f
    const val SunAlphaNight = 0.28f
    const val CausticsAlphaDay = 1f
    const val CausticsAlphaNight = 0.3f
    const val RaysAlphaDay = 1f
    const val RaysAlphaNight = 0.28f
    const val ReefFishAlphaDay = 1f
    const val ReefFishAlphaNight = 0.5f
    const val SchoolAlphaDay = 1f
    const val SchoolAlphaNight = 0f
    const val JellyGlowBoostDay = 0f
    const val JellyGlowBoostNight = 0.2f

    // Edges.
    val BottomFadeTop = Color(0x00010C1A)
    val BottomFadeBottom = Color(0xBF010C1A)
    val VignetteClear = Color(0x00021226)
    val VignetteEdge = Color(0xBF010A18)

    // Scrubber.
    val ScrubLine = Color(0x8CE1FAFF)
    val ScrubLineClear = Color(0x00E1FAFF)
    val ScrubTick = Color(0x99EBFCFF)
    val ScrubPosition = Color(0xD9EBFCFF)
    val ControlFocus = Color(0x33FFFFFF)

    /** One tint per jelly, in the artboard's order; real apps take them by index modulo size. */
    val JellyTints = listOf(
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

    fun jellyTint(index: Int): Color = JellyTints[Math.floorMod(index, JellyTints.size)]
}
